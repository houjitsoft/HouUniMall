package com.houjiang.commons.dto.cache;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统缓存设置（管理后台展示 + 持久化）
 * @author Amos
 */
@Data
public class CacheSettingsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Redis 默认过期（秒），运行时来自 HouConfig.TOKEN_OUTTIME */
    private Integer redisExpire;
    /** Redis 最大内存（MB），运行时尝试读取 Redis INFO */
    private Integer redisMaxMemory;
    /** Redis 连接池大小，运行时来自 spring.redis.lettuce.pool.max-active */
    private Integer redisPoolSize;

    private Integer appExpire;
    private Integer appMaxSize;
    private Integer staticExpire;
    private Boolean staticCompression;
    private Boolean preloadEnabled;
    private Boolean compressionEnabled;
    private Integer hitRateThreshold;
    private String cleanupStrategy;

    /** 运行时：Redis 主机 */
    private String redisHost;
    /** 运行时：Redis 端口 */
    private Integer redisPort;
    /** 运行时：Redis 库 */
    private Integer redisDatabase;
    /** 运行时：命令超时（毫秒） */
    private Integer redisCommandTimeoutMs;
    /** 运行时：已用内存 MB */
    private Integer redisMemoryUsedMb;
    /** 运行时：Token 缓存 TTL（秒，HouConfig） */
    private Integer runtimeTokenExpire;
    /** 运行时：Redis 是否连通 */
    private Boolean redisConnected;
}
