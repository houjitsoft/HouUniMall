package com.houjiang.commons.util;

//import org.apache.commons.lang.StringUtils;


import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Iterator;
import java.util.Set;

/**
 * 1.1 字符串取值时进行延时操作 Redis 工具类 Created by Amos
 * @author Amos
 */
public class RedisUtil {

    /**
     * protected static ReentrantLock lockPool = new ReentrantLock();
     * protected static ReentrantLock lockJedis = new ReentrantLock();
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtil.class);

    /** 正式 */

    private static String IP;
    /** Redis的端口号 */
    private static Integer PORT;
    private static String PASSWORD;
    private static String DATABASES;

    /**
     * 可用连接实例的最大数目。控制进程内 Jedis 并发上限（不影响 Redis 服务端 maxclients）。
     * 200 对单节点 Tomcat 线程模型足够，既避免用户线程被连接池阻塞，
     * 又防止单实例损耗过多 Redis 服务端连接名额。旧值 1000 偏高，恢为合理容量。
     */
    private static final int MAX_ACTIVE = 200;

    /** 最大空闲连接数 */
    private static final int MAX_IDLE = 20;

    /** 最小空闲连接数，保证突发流量时无需重新三次握手 */
    private static final int MIN_IDLE = 5;

    /**
     * 等待可用连接的最大时间，单位毫秒。2000ms 内拿不到连接即快速失败，避免业务线程死等。
     * 旧值 -1 涵义为 “永不超时”，一旦 Redis 网络拖动或连接泄漏会导致 Tomcat 线程雪崩。
     */
    private static final int MAX_WAIT = 2000;

    /** 连接超时 + 读取超时（3000ms）。公网 Redis 场景下，过小会频繁报超时，过大会阻塞线程。 */
    private static final int TIMEOUT = 3000;

    /** 从连接池借出时不进行探活测试，降低每次调用的 RTT；探活交给后台 evictor 线程处理。 */
    private static final boolean TEST_ON_BORROW = false;

    /** 归还时不测试；由 evictor 周期性清理。 */
    private static final boolean TEST_ON_RETURN = false;

    /**
     * 空闲时探活：防止公网 NAT silent drop、Redis 服务端 timeout 踢掉长期空闲连接后，
     * 业务再拿到死连接导致读写报错。
     */
    private static final boolean TEST_WHILE_IDLE = true;

    /** Evictor 线程的调度周期——每 30s 扫一次空闲连接 */
    private static final long TIME_BETWEEN_EVICTION_RUNS_MS = 30_000L;

    /** 空闲连接超过 60s 且超过 minIdle 的部分将被丢弃；无用连接早回收，避免服务端累积。 */
    private static final long MIN_EVICTABLE_IDLE_TIME_MS = 60_000L;

    private static JedisPool jedisPool = null;

    /**
     * redis过期时间,以秒为单位
     */
    // 5分钟
    public final static int EXRP_FIVE_MIN = 5 * 60;
    // 一分钟
    public final static int EXRP_ONE_MIN = 60;
    // 10分钟
    public final static int EXRP_TEN_MIN = 10 * 60;
    // 一小时
    public final static int EXRP_HOUR = 60 * 60;
    // 一天
    public final static int EXRP_DAY = 60 * 60 * 24;
    // 二天
    public final static int EXRP_TWO_DAY = 60 * 60 * 24 * 2;
    // 一个月
    public final static int EXRP_MONTH = 60 * 60 * 24 * 30;

    /**
     * 旧版无参初始化（历史遗留）。
     *
     * 【假定失败】IP / PORT / PASSWORD / DATABASES 四个静态字段必须先由
     * {@link #initialPool2} 设置，否则直接调用本方法会因 {@code Integer.valueOf(null)}
     * 抛 NPE，jedisPool 始终为 null，造成整个 RedisUtil 静默失效。
     *
     * 新版统一由 {@code RedisUtilInitializer}（commons 中的 Spring Bean）在应用启动
     * 时调用 {@link #initialPool2}。本方法保留为“保险丝”：当所有上游都未初始化时
     * 只输出显著告警，不再尝试构造 JedisPool，避免暴露 "神秘" NPE。
     */
    private static void initialPool() {
        LOGGER.error("RedisUtil.initialPool() 无参版本已废弃，必须先通过 RedisUtilInitializer 或 initialPool2(...) 设置连接参数。当前 jedisPool={}, IP={}, PORT={}", jedisPool, IP, PORT);
    }
    /**
     * 初始化Redis连接池（推荐入口）。
     *
     * 【修复点】
     *   1. 幂等：若已初始化直接返回，避免多次设置覆盖 / 创建重复连接池；
     *   2. 工业级 JedisPoolConfig：增加 minIdle / testWhileIdle / evictor 周期，
     *      以及 blockWhenExhausted=true（取流用尽时等待而不是抛异常）；
     *   3. 使用 {@link Integer#parseInt}：避免 DATABASES 为 null 时 {@code Integer.valueOf(null)} 抛 NPE。
     */
    public static void initialPool2(String ip, Integer port, String password, String DATABASES) {
        if (jedisPool != null) {
            // 幂等：不重复创建，防止 RedisUtilInitializer 与历史 MyApplicationRunner 同时调用导致重复连接池。
            LOGGER.info("RedisUtil 已初始化，跳过重复构造 host={}, port={}", ip, port);
            return;
        }
        if (ip == null || ip.isEmpty() || port == null || port <= 0) {
            LOGGER.error("RedisUtil.initialPool2 参数不法 host={}, port={}，JedisPool 未创建", ip, port);
            return;
        }
        try {
            IP = ip;
            PORT = port;
            PASSWORD = password;
            RedisUtil.DATABASES = DATABASES;

            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMinIdle(MIN_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setBlockWhenExhausted(true);
            config.setTestOnBorrow(TEST_ON_BORROW);
            config.setTestOnReturn(TEST_ON_RETURN);
            // 空闲探活：不受公网 NAT silent drop / Redis 服务端 timeout 影响
            config.setTestWhileIdle(TEST_WHILE_IDLE);
            config.setTimeBetweenEvictionRunsMillis(TIME_BETWEEN_EVICTION_RUNS_MS);
            config.setMinEvictableIdleTimeMillis(MIN_EVICTABLE_IDLE_TIME_MS);
            // JMX 在多实例状态下会重复注册，关闭
            config.setJmxEnabled(false);

            int db = 0;
            if (DATABASES != null && !DATABASES.trim().isEmpty()) {
                try {
                    db = Integer.parseInt(DATABASES.trim());
                } catch (NumberFormatException nfe) {
                    LOGGER.warn("RedisUtil DATABASES 配置非法：{}，退化为 0", DATABASES);
                }
            }

            // 传入 password 后池内连接创建时会自动 AUTH，无需每次 getResource 后再次 AUTH。
            jedisPool = new JedisPool(config, ip, port, TIMEOUT, password, db);
            LOGGER.info("RedisUtil JedisPool 创建成功 host={}, port={}, database={}, maxTotal={}, maxIdle={}, minIdle={}, testWhileIdle={}",
                    ip, port, db, MAX_ACTIVE, MAX_IDLE, MIN_IDLE, TEST_WHILE_IDLE);
        } catch (Exception e) {
            LOGGER.error("First create JedisPool error : " + e.getMessage(), e);
        }
    }
    /**
     * 在多线程环境同步初始化。
     * 【修复点】不再在此处回退到无参 initialPool()（旧版会 NPE 静默失败）。
     *   自 v次 RedisUtilInitializer 接接管后，如果 jedisPool 此时仍为 null，说明
     *   spring.redis.* 配置未生效或上下文未加载，直接日志告警由调用方处理 null。
     */
    private static synchronized void poolInit() {
        if (null == jedisPool) {
            initialPool();
        }
    }

    /**
     * 从连接池获取一个 Jedis 实例。
     *
     * 【修复点】
     *   1. 移除 synchronized：JedisPool 本身线程安全，之前的全局锁会把全 JVM 的
     *      Redis 调用串行化，任一调用卡住都会连锎导致所有线程排队超时。
     *   2. 移除重复 AUTH：JedisPool 构造时已传入 password，池内连接在
     *      创建时已 AUTH 过；每次 getResource 后再手动 AUTH 只会多一次跨公网 RTT。
     *
     * @return Jedis，无连接或异常时返回 null，调用方需判空
     */
    public static Jedis getJedis() {
        poolInit();
        try {
            if (null != jedisPool) {
                return jedisPool.getResource();
            }
        } catch (Exception e) {
            LOGGER.error("Get jedis error : " + e.getMessage());
        }
        return null;
    }

    /**
     * 设置 String
     *
     * @param key
     * @param value
     */
    public static void set(String key, String value) {
        Jedis jedis = null;
        try {
            value = "".equals(value) ? "" : value;
            jedis = getJedis();
            jedis.set(key, value);
        } catch (Exception e) {
            LOGGER.error("key=" + key + "value=" + value);
            LOGGER.error("Set key error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 设置 byte[]
     *
     * @param key
     * @param value
     */
    public static void set(byte[] key, byte[] value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(key, value);
        } catch (Exception e) {
            LOGGER.error("key=" + key + "value=" + value);
            LOGGER.error("Set key error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 设置 String 过期时间
     *
     * @param key
     * @param value
     * @param seconds 以秒为单位
     */
    public static void set(String key, String value, int seconds) {
        Jedis jedis = null;
        try {
            // value = !"".equals(value) ? "" : value;
            jedis = getJedis();
            jedis.setex(key, seconds, value);
        } catch (Exception e) {
            LOGGER.error("key=" + key + "value=" + value);
            LOGGER.error("Set keyex error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 更新值 不重置过期时间
     * @param key
     * @param value
     */
    public static void setValueNoSetTime(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            Long ttl = jedis.ttl(key);
            jedis.setex(key, Math.toIntExact(ttl), value);
        } catch (Exception e) {
            LOGGER.error("key=" + key + "value=" + value);
            LOGGER.error("Set keyex error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 设置 byte[] 过期时间
     *
     * @param key
     * @param value
     * @param seconds 以秒为单位
     */
    public static void set(byte[] key, byte[] value, int seconds) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.set(key, value);
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LOGGER.error("key=" + key + "value=" + value);
            LOGGER.error("Set key error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 解包 FastJson 序列化出来的字符串字面量。
     *
     * <p>写侧 {@code RedisCache} 的 value 走 FastJsonRedisSerializer，普通 String 落库后会带上
     * 一层双引号（{@code "650000000000000001"}）；读侧这里用 Jedis 原生命令，拿到的就是带引号的原始串。
     * 直接当业务值用会一路错下去：登录态映射 {@code hou-user-token-userid_*} 取出的 userId 变成
     * {@code "650…"}，SQL 按 user_id 查不到人，表现是购物车恒空（queryShoppingCart 返回
     * numberOfValidProducts=0）、下单取不到用户、支付进不去——整条链路「列表有货、详情能开、
     * 一结算就断」。
     *
     * <p>只处理「首尾都是双引号」的纯字符串字面量；对象 / 数组 / 数字 / 裸字符串原样返回，
     * 对已有的非字符串缓存值无误伤。
     *
     * @param value Jedis 读回的原始值
     * @return 解包后的值；非 JSON 字符串字面量原样返回
     */
    private static String unwrapJsonString(String value) {
        if (value == null || value.length() < 2) {
            return value;
        }
        if (value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
            return value;
        }
        try {
            String unwrapped = JSON.parseObject(value, String.class);
            return unwrapped == null ? value : unwrapped;
        } catch (Exception ignore) {
            // 非法 JSON 就按原始值返回，保持旧行为
            return value;
        }
    }

    /**
     * 获取String值 ,如果不为null或者“”，就进行延时操作
     *
     * @param key
     * @return value
     */
    public static String get(String key) {
        Jedis jedis = null;
        String value = null;
        try {
            jedis = getJedis();
            if (null == jedis) {
                return null;
            }
            value = unwrapJsonString(jedis.get(key));
            if (null != value && !"".equals(value)) {
                jedis.expire(key, HouConfig.TOKEN_OUTTIME);
            }
        } catch (Exception e) {
            LOGGER.error("key=" + key + "value=" + value);
            LOGGER.error(e.getMessage());
        } finally {
            closeRedis(jedis);
        }
        return value;
    }
    /**
     * 获取String值 ,如果不为null或者“”，就进行延时操作
     *
     * @param key
     * @return value
     */
    public static String getNotExpire(String key) {
        Jedis jedis = null;
        String value = null;
        try {
            jedis = getJedis();
            if (null == jedis) {
                return null;
            }
            value = unwrapJsonString(jedis.get(key));
        } catch (Exception e) {
            LOGGER.error("key=" + key + "value=" + value);
            LOGGER.error(e.getMessage());
        } finally {
            closeRedis(jedis);
        }
        return value;
    }
    /**
     * 获取String值
     *
     * @param key
     * @return value
     */
    public static String getNoUpdateTime(String key) {
        Jedis jedis = null;
        String value = null;
        try {
            jedis = getJedis();
            if (null == jedis) {
                return null;
            }
            value = unwrapJsonString(jedis.get(key));
        } catch (Exception e) {
            LOGGER.error("key=" + key + "value=" + value);
            LOGGER.error(e.getMessage());
        } finally {
            closeRedis(jedis);
        }
        return value;
    }
    public static String getRedis(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (null == jedis) {
                return null;
            }
            return jedis.get(key);
        } catch (Exception e) {
            LOGGER.error("getRedis error key=" + key + ", msg=" + e.getMessage());
            return null;
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * @param key
     * @return value
     */
    public static String getNoExpire(String key) {
        Jedis jedis = getJedis();
        if (null == jedis) {
            return null;
        }
        String value = jedis.get(key);
        closeRedis(jedis);
        return value;
    }

    /**
     * 获取byte[]值
     *
     * @param key
     * @return value
     */
    public static byte[] get(byte[] key) {
        Jedis jedis = getJedis();
        if (null == jedis) {
            return null;
        }
        byte[] value = jedis.get(key);
        closeRedis(jedis);
        return value;
    }

    /**
     * 删除值（模糊匹配 key类的前缀）
     * 【修复】原实现在 try 块内手动 jedis.close()，一旦 keys/del 抛异常会泄漏连接，
     *   改用 finally + closeRedis 确保连接一定归还。
     *
     * @param key
     */
    public static void removeAll(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (null == jedis) {
                return;
            }
            Iterator<String> it = jedis.keys(key + "*").iterator();
            while (it.hasNext()) {
                String key1 = it.next();
                // 清空缓存中记录的数据
                jedis.del(key1);
            }
        } catch (Exception e) {
            LOGGER.error("Remove keyex error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 删除值
     *
     * @param key
     */
    public static void remove(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(key);
        } catch (Exception e) {
            LOGGER.error("key=" + key);
            LOGGER.error("Remove keyex error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 删除值
     *
     * @param key
     */
    public static void remove(byte[] key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(key);
        } catch (Exception e) {
            LOGGER.error("key=" + key);
            LOGGER.error("Remove keyex error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * lpush
     *
     * @param key
     * @param key
     */
    public static void lpush(String key, String... strings) {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            jedis.lpush(key, strings);
        } catch (Exception e) {
            LOGGER.error("key=" + key);
            LOGGER.error("lpush error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    public static String rpop(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis == null ? null : jedis.rpop(key);
        } catch (Exception e) {
            LOGGER.error("rpop error, key=" + key, e);
            return null;
        } finally {
            closeRedis(jedis);
        }
    }

    public static long llen(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis == null ? 0L : jedis.llen(key);
        } catch (Exception e) {
            LOGGER.error("llen error, key=" + key, e);
            return 0L;
        } finally {
            closeRedis(jedis);
        }
    }

    public static java.util.List<String> lrange(String key, long start, long end) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis == null ? java.util.Collections.emptyList() : jedis.lrange(key, start, end);
        } catch (Exception e) {
            LOGGER.error("lrange error, key=" + key, e);
            return java.util.Collections.emptyList();
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * lpush
     *
     * @param key
     * @param key
     */
    public static void lpush(String key, Integer seconds, String... strings) {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            jedis.lpush(key, strings);
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LOGGER.error("key=" + key);
            LOGGER.error("lpush error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * lrem
     *
     * @param key
     * @param count
     * @param value
     */
    public static void lrem(String key, long count, String value) {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            jedis.lrem(key, count, value);
        } catch (Exception e) {
            LOGGER.error("key=" + key);
            LOGGER.error("lpush error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * sadd
     *
     * @param key
     * @param value
     * @param seconds
     */
    public static void sadd(String key, String value, int seconds) {
        Jedis jedis = null;
        try {
            jedis = RedisUtil.getJedis();
            jedis.sadd(key, value);
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LOGGER.error("key=" + key);
            LOGGER.error("sadd error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * incr
     *
     * @param key
     * @return value
     */
    public static Long incr(String key) {
        Jedis jedis = getJedis();
        if (null == jedis) {
            return null;
        }
        long value = jedis.incr(key);
        closeRedis(jedis);
        return value;
    }

    /**
     * decr
     *
     * @param key
     * @return value
     */
    public static Long decr(String key) {
        Jedis jedis = getJedis();
        if (null == jedis) {
            return null;
        }
        long value = jedis.decr(key);
        closeRedis(jedis);
        return value;
    }

    /**
     * expire
     *
     * @param key,seconds
     * @return
     */
    public static void expire(String key, int seconds) {
        Jedis jedis = getJedis();
        try {
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LOGGER.error("key=" + key);
            LOGGER.error("Set keyex error : " + e);
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * exists
     *
     * @param key
     * @return
     */
    public static boolean exists(String key) {
        Jedis jedis = null;
        boolean value = false;
        try {
            jedis = getJedis();
            value = jedis.exists(key);

        } catch (Exception e) {
            LOGGER.error("Jedis-exists异常" + e.getMessage());
        } finally {
            closeRedis(jedis);
        }
        return value;
    }


    /**
     * 关闭jedis连接池
     *
     * @param jedis
     */
    public static void closeRedis(Jedis jedis) {
        if (jedis != null) {
            try {
                jedis.close();
            } catch (Exception e) {
                LOGGER.error("Jedis关闭异常" + e.getMessage());
            }
        }
    }

    /**
     * 查询 key 的生命周期
     * 【修复】原实现只拿连接不关闭，每次调用都泄漏一个连接。
     *
     * @param key
     * @return TTL（秒）；-2 表示 key 不存在，-1 表示无过期时间
     */
    public static long getKeyExpire(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            if (null == jedis) {
                return -2L;
            }
            return jedis.ttl(key);
        } catch (Exception e) {
            LOGGER.error("getKeyExpire error key=" + key + ", msg=" + e.getMessage());
            return -2L;
        } finally {
            closeRedis(jedis);
        }
    }

    /**
     * 使用redis模糊清除缓存
     */
    public static void fuzzyDeletion(String key) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            // 模糊匹配所有以keyword:开头的所有key值
            Set<String> keys = jedis.keys(key + "*");
            for (String k : keys) {
                jedis.del(k);
            }
        } catch (Exception e) {
            LOGGER.error("key=" + key);
            LOGGER.error("Remove keyex error : " + e);
        } finally {
            closeRedis(jedis);
        }

    }

    /**
     * 请求获取redis锁
     * @param key
     * @param seconds
     * @return
     */
    public static boolean getLock(String key,int seconds){
        // 1.判断key数据是否存在
        if(RedisUtil.exists(key)){
            // 2.存在返回false
            return false;
        }else{
            // 3.不存在返回成功并将key数据加入缓存
            RedisUtil.set(key,"lock",seconds);
            return true;
        }
    }
}
