package com.houjiang.commons.config;

import com.houjiang.commons.util.RedisUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * RedisUtil（基于 Jedis 的静态工具类）统一初始化器。
 *
 * 【修复背景】
 *   各业务模块（oa/auth/base/business/order/payment/workflow 等）原先依赖各自的
 *   MyApplicationRunner 手动调用 {@code RedisUtil.initialPool2(...)}。历史迭代中
 *   这行代码被部分模块人为注释（"// Deleted: ..."），导致这些模块的 RedisUtil
 *   连接池永远为 null，所有 {@code RedisUtil.get/set/exists/...} 静默失效：
 *     - set 看起来成功，实际没写进 Redis；
 *     - get 永远返回 null；
 *     - exists 永远返回 false；
 *     - 验证码、防刷、Token 缓存等功能全部异常。
 *   线上表现为 "Redis 超时 / 短信验证码对不上 / Token 校验不稳定"，排查极其困难。
 *
 * 【修复策略】
 *   在 commons 层提供统一、幂等、由 Spring 管理的初始化入口：
 *     - 被各模块的 @ComponentScan(com.houjiang) 自动扫描到；
 *     - @PostConstruct 在 Spring 容器启动完成时执行，保证在任何业务代码触发
 *       RedisUtil 之前 JedisPool 已就绪；
 *     - 从 Environment 读取 spring.redis.* 配置（Nacos redis.yaml 下发的），
 *       任何模块都不必再手动初始化；
 *     - 内置 host/port 空值防御，配置缺失时快速失败而不是等到运行期空指针。
 *
 * 【与 RedisTemplate 的关系】
 *   本项目实际存在两套 Redis 客户端：
 *     - Lettuce（Spring 管理的 RedisTemplate）：由 RedisConfig 接管；
 *     - Jedis（静态的 RedisUtil）：由本类接管。
 *   两套客户端各自维护连接，互不冲突；RedisUtil 仅服务于历史遗留业务代码。
 * @author Amos
 */
@Configuration
public class RedisUtilInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(RedisUtilInitializer.class);

    @Value("${spring.redis.host:}")
    private String redisHost;

    @Value("${spring.redis.port:0}")
    private Integer redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    /**
     * Redis 数据库编号。使用 String 类型以兼容 RedisUtil.initialPool2 的历史签名。
     */
    @Value("${spring.redis.database:0}")
    private String redisDatabase;

    /**
     * 容器加载完成后初始化 RedisUtil 的 JedisPool。
     * 幂等：若已初始化则跳过；配置缺失则仅告警不抛异常（避免拖挂整个应用启动）。
     */
    @PostConstruct
    public void initRedisUtil() {
        if (redisHost == null || redisHost.isEmpty() || redisPort == null || redisPort <= 0) {
            LOG.error("【RedisUtilInitializer】spring.redis.host/port 配置缺失或非法，host={}, port={}；" +
                            "RedisUtil（Jedis）将无法工作，请检查 Nacos 的 redis.yaml 配置是否被当前应用引入。",
                    redisHost, redisPort);
            return;
        }
        try {
            RedisUtil.initialPool2(redisHost, redisPort, redisPassword, redisDatabase);
            LOG.info("【RedisUtilInitializer】RedisUtil 初始化完成 host={}, port={}, database={}",
                    redisHost, redisPort, redisDatabase);
        } catch (Exception e) {
            LOG.error("【RedisUtilInitializer】RedisUtil 初始化异常 host=" + redisHost + ", port=" + redisPort, e);
        }
    }
}

