package com.houjiang.commons.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 统一 Redis 连接配置。
 *
 * 【修复背景】旧实现仅用 {@code new LettuceConnectionFactory(standalone)} 构造工厂，
 * 未传入 {@link LettuceClientConfiguration}，实际退化为：
 *   1) Command timeout 为 Lettuce 默认 60s，公网抖动即阻塞 Tomcat 线程；
 *   2) 无 TCP keepalive，跨公网 NAT 会话 silent drop；
 *   3) 断连期间命令继续堆积，故障恢复慢。
 * 表现为：服务重启瞬时、或公网短抖动后，认证链路 {@code JwtAuthenticationTokenFilter}
 * 抛出 Redis 异常并返回 10012（"认证服务暂时不可用"）。
 *
 * 【修复策略】显式构造非池化的 {@link LettuceClientConfiguration}（shareNativeConnection=true，
 * 不引入 commons-pool2 以避开 Spring Boot 3.2.4 nested jar loader 的已知副作用）：
 *   - commandTimeout ← spring.redis.timeout
 *   - shutdownTimeout ← spring.redis.lettuce.shutdown-timeout
 *   - TCP 层启用 keepAlive + connectTimeout（防止公网 NAT silent drop）
 *   - ClientOptions 开启 autoReconnect + REJECT_COMMANDS_IF_NOT_CONNECTED：
 *     断连时立即抛 RedisException 而不是排队阻塞，避免 Tomcat 线程池雪崩
 *   - TimeoutOptions 确保命令级超时真正生效
 * @author Amos
 */
@Configuration
public class RedisConfig {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConfig.class);

    /**
     * 统一的 Redis 连接工厂。
     * 从 Environment 读取 spring.redis.* 配置（Nacos 下发的 redis.yaml），
     * 完整应用 Lettuce 客户端参数，确保跨公网连接的稳定性。
     *
     * @param environment Spring 环境
     * @return LettuceConnectionFactory（带池化、超时、keepalive）
     */
    @Bean
    @Primary
    public RedisConnectionFactory primaryRedisConnectionFactory(Environment environment) {
        String host = environment.getProperty("spring.redis.host");
        Integer port = environment.getProperty("spring.redis.port", Integer.class);
        String password = environment.getProperty("spring.redis.password");
        Integer database = environment.getProperty("spring.redis.database", Integer.class);
        if (host == null || port == null || database == null) {
            throw new IllegalStateException("缺少必要的spring.redis.*配置（host/port/database），请检查Nacos的redis.yaml是否正确加载");
        }

        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(host, port);
        standalone.setDatabase(database);
        if (password != null && !password.isEmpty()) {
            standalone.setPassword(password);
        }

        // 命令执行超时（对应 spring.redis.timeout），默认 3s 避免阻塞 Tomcat 线程
        // 注意：Nacos redis.yaml 中配置可能带单位（如 "3000ms" / "3s"），必须用 Duration.class 读取，
        // 否则 Spring 会因无法把 "2000ms" 转 Long 直接抛 ConversionFailedException 导致启动失败。
        Duration commandTimeout = environment.getProperty(
                "spring.redis.timeout", Duration.class, Duration.ofMillis(3000));
        long commandTimeoutMs = commandTimeout.toMillis();

        // 关闭超时（优雅关闭 Lettuce 客户端，防止关机时阻塞）
        Duration shutdownTimeout = environment.getProperty(
                "spring.redis.lettuce.shutdown-timeout", Duration.class, Duration.ofMillis(2000));

        // TCP 层：connectTimeout 3s 快速失败；激进 keepAlive（30s 空闲→开始探测）防止公网 NAT silent drop
        // 注意：idle/interval/count 在 Linux(epoll) 上完全生效；
        //       Windows 上 Netty NIO 仅支持 SO_KEEPALIVE=1（间隔由 OS 默认 2h），
        //       因此配合后台心跳保活 Bean（30s PING）来兜底 Windows 环境。
        SocketOptions.KeepAliveOptions keepAliveOptions = SocketOptions.KeepAliveOptions.builder()
                .enable()
                .idle(Duration.ofSeconds(30))
                .interval(Duration.ofSeconds(10))
                .count(3)
                .build();

        SocketOptions socketOptions = SocketOptions.builder()
                .connectTimeout(Duration.ofSeconds(3))
                .keepAlive(keepAliveOptions)
                .tcpNoDelay(true)
                .build();

        // 客户端行为：自动重连 + 断连快速失败 + 显式 command timeout
        ClientOptions clientOptions = ClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .socketOptions(socketOptions)
                .timeoutOptions(TimeoutOptions.enabled(commandTimeout))
                .build();

        // 非池化配置（避开 commons-pool2 引入带来的 nested jar 加载副作用）
        // shareNativeConnection=true + 后台心跳保活机制，确保单连接始终活跃
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(commandTimeout)
                .shutdownTimeout(shutdownTimeout)
                .clientOptions(clientOptions)
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, clientConfig);
        factory.setShareNativeConnection(true);
        factory.afterPropertiesSet();

        LOG.info("Redis连接工厂初始化完成(非池化) - Host: {}, Port: {}, Database: {}, commandTimeout: {}ms, shutdownTimeout: {}ms, keepAlive=true, autoReconnect=true, disconnectedBehavior=REJECT_COMMANDS",
                host, port, database, commandTimeoutMs, shutdownTimeout.toMillis());
        return factory;
    }


    /**
     * Redis 后台心跳保活调度器。
     * 每 30 秒向 Redis 服务端发送 PING 命令，确保 Lettuce 底层连接始终活跃。
     * 当连接因公网 NAT/防火墙 silent drop 而处于 "TCP 半死" 状态时，
     * PING 会立即触发超时异常 → Lettuce ConnectionWatchdog 自动重连，
     * 避免业务请求在抖动窗口期内全部返回 10012。
     *
     * @param connectionFactory 由 primaryRedisConnectionFactory 提供
     * @return ScheduledExecutorService（daemon 线程，随 JVM 关闭而终止）
     */
    @Bean(destroyMethod = "shutdownNow")
    public ScheduledExecutorService redisKeepAliveScheduler(RedisConnectionFactory connectionFactory) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "redis-keepalive");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                connectionFactory.getConnection().ping();
            } catch (Exception e) {
                LOG.warn("Redis心跳保活失败(将触发autoReconnect): {}", e.getMessage());
            }
        }, 30, 30, TimeUnit.SECONDS);
        LOG.info("Redis心跳保活调度已启动 - 间隔30s，确保连接不被公网NAT/防火墙静默断开");
        return scheduler;
    }

    /**
     * 创建统一序列化配置的 RedisTemplate。
     * 绑定统一的连接工厂，Key 与 HashKey 使用 StringRedisSerializer，
     * Value 与 HashValue 使用 FastJson 序列化。
     *
     * @param connectionFactory 由 primaryRedisConnectionFactory 创建并统一管理的连接工厂
     * @return RedisTemplate<String, Object> 统一模板实例
     */
    @Bean
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJsonRedisSerializer serializer = new FastJsonRedisSerializer(Object.class);

        // Key 与 HashKey 使用 StringRedisSerializer，保证 key 可读
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }
}
