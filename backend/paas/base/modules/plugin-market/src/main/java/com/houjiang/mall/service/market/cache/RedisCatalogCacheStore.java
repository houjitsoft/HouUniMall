package com.houjiang.mall.service.market.cache;

import com.houjiang.commons.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于 {@link RedisUtil}（Jedis）的目录缓存存储后端。
 *
 * <p><b>为什么用 {@code RedisUtil.getRedis} 而不是 {@code RedisUtil.get}</b>：
 * {@code get} 读到值后会顺手 {@code EXPIRE key TOKEN_OUTTIME(86400)}——那是给登录令牌
 * 做的「滑动续期」，用在缓存上会让 TTL 兜底彻底失效：只要还有人访问，
 * 脏数据就永远不过期，与「TTL 300s 兜底」的设计直接冲突。
 * {@code getRedis} 是纯粹的 GET，不做任何 TTL 变更，才是缓存该用的那个。
 *
 * <p><b>失败处理</b>：{@code RedisUtil} 内部已经吞掉了绝大多数异常，
 * 唯一例外是 {@code incr}（它不 try/catch，拿到连接后直接自增），
 * 这里补一层，保证「Redis 抖动不会把目录接口打挂」。
 *
 * @author Amos
 * @since M5
 */
@Slf4j
@Component
public class RedisCatalogCacheStore implements CatalogCacheStore {

    /**
     * 构造存储后端。
     */
    public RedisCatalogCacheStore() {
        // 无态实现：连接由 RedisUtil 的静态池统一持有。
    }

    /**
     * 读取缓存值（纯 GET，不触碰 TTL）。
     *
     * @param key 缓存键
     * @return 缓存值；未命中或 Redis 不可用返回 null
     */
    @Override
    public String get(String key) {
        return RedisUtil.getRedis(key);
    }

    /**
     * 写入缓存值并指定存活时间（SETEX）。
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param ttlSeconds 存活时间（秒）
     */
    @Override
    public void set(String key, String value, int ttlSeconds) {
        RedisUtil.set(key, value, ttlSeconds);
    }

    /**
     * 自增全局 revision。
     *
     * @param key 键
     * @return 自增后的值；Redis 不可用返回 null
     */
    @Override
    public Long incr(String key) {
        try {
            return RedisUtil.incr(key);
        } catch (RuntimeException e) {
            log.warn("目录缓存 revision 自增失败，本次退化为「不失效」，依赖 TTL 兜底: key={} msg={}",
                    key, e.getMessage());
            return null;
        }
    }

    /**
     * 删除键。
     *
     * @param key 键
     */
    @Override
    public void del(String key) {
        RedisUtil.remove(key);
    }
}
