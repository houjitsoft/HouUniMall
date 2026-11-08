package com.houjiang.mall.service.market.cache;

/**
 * 目录索引缓存的存储后端抽象。
 *
 * <p><b>为什么要有这层抽象</b>：
 * <ol>
 *   <li><b>可测</b>：目录缓存的正确性（revision 失效、TTL 兜底、Redis 挂了要回源）
 *       必须用单测钉死，而 {@code RedisUtil} 是静态方法、连接真实 Redis，
 *       直接依赖它就没法在单测里模拟「Redis 不可用」；</li>
 *   <li><b>可换</b>：目前只有 Redis 实现，将来换 Caffeine 本地缓存只需再加一个实现类，
 *       {@link PluginCatalogCache} 的失效逻辑一行都不用动。</li>
 * </ol>
 *
 * <p><b>契约</b>：所有方法<b>允许失败</b>——实现类必须吞掉异常（读返回 null、写静默放弃），
 * 由 {@link PluginCatalogCache} 统一降级回源。缓存是加速手段，不是数据源，
 * 缓存抖动导致接口 500 是本末倒置。
 *
 * @author Amos
 * @since M5
 */
public interface CatalogCacheStore {

    /**
     * 读取缓存值。
     *
     * @param key 缓存键
     * @return 缓存值；未命中或后端不可用返回 null
     */
    String get(String key);

    /**
     * 写入缓存值并指定存活时间。
     *
     * @param key        缓存键
     * @param value      缓存值
     * @param ttlSeconds 存活时间（秒）
     */
    void set(String key, String value, int ttlSeconds);

    /**
     * 自增计数器（用于全局 revision）。
     *
     * @param key 键
     * @return 自增后的值；后端不可用返回 null
     */
    Long incr(String key);

    /**
     * 删除键（用于单键失效，如 {@code hou:idx:latest:{code}}）。
     *
     * @param key 键
     */
    void del(String key);
}
