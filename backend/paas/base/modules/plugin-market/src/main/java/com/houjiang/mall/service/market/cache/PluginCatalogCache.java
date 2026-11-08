package com.houjiang.mall.service.market.cache;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 插件目录索引缓存。
 *
 * <p>解决三个真实问题：
 * <ol>
 *   <li><b>目录页每次都回源</b>：市场列表要 JOIN 安装态与授权，插件一多就是慢查询。
 *       缓存<b>公开基线</b>（{@code status=published} 的商品元数据），租户维度不进缓存，
 *       避免缓存按 instanceId 指数膨胀；</li>
 *   <li><b>失效不能逐个删 key</b>：分页 + 关键字组合的 key 数量没有上界，
 *       {@code KEYS/DEL} 扫一遍会造成雪崩与 key 爆炸。改为<b>全局 revision 键</b>：
 *       发布 / 上架 / 下架 / 定价变更时 {@link #bumpRevision()} 一次 INCR，
 *       revision 参与 key 拼接，旧 key 自然失效并由 TTL 回收；</li>
 *   <li><b>Redis 挂了不能连累接口</b>：全链路 fail-open——读不到就回源，
 *       写不进就下次再写，最坏情况是退化成「没有缓存」。缓存是加速手段，不是数据源。</li>
 * </ol>
 *
 * <p><b>两段缓存的分工</b>：
 * <ul>
 *   <li>{@code hou:idx:catalog:{rev}:{hash}} —— 分页列表，revision 整体失效；</li>
 *   <li>{@code hou:idx:latest:{code}} —— 单插件最新版本，<b>不进分页缓存</b>，
 *       由 {@link #invalidateLatest} 单键删除（数量有上界，等于插件数，不会爆炸）。</li>
 * </ul>
 *
 * @author Amos
 * @since M5
 */
@Slf4j
@Component
public class PluginCatalogCache {

    /**
     * 全局 revision 键：INCR 它即可让所有分页缓存整体失效。
     */
    public static final String REVISION_KEY = "hou:idx:catalog:rev";

    /**
     * 分页列表命名空间。
     */
    public static final String NAMESPACE_CATALOG = "catalog";

    /**
     * 单插件最新版本命名空间。
     */
    public static final String NAMESPACE_LATEST = "latest";

    /**
     * TTL 兜底（秒）：revision 失效失败时的最后一道防线。
     * 300s 是「商品元数据变更不频繁」与「脏数据最多存在 5 分钟」的折中。
     */
    public static final int DEFAULT_TTL_SECONDS = 300;

    /**
     * revision 不可用时使用的兜底值（独立命名空间内保持一致即可）。
     */
    private static final String FALLBACK_REVISION = "0";

    /**
     * 「查过但没有已发布版本」的哨兵值：避免每次都为离线插件回源。
     */
    private static final String EMPTY_SENTINEL = "";

    private final CatalogCacheStore store;

    private final int ttlSeconds;

    private final boolean enabled;

    private final AtomicLong hitCount = new AtomicLong();

    private final AtomicLong missCount = new AtomicLong();

    private final AtomicLong degradeCount = new AtomicLong();

    /**
     * 未装配存储后端的构造（缓存<b>关闭</b>）：单测与不需要缓存的场景使用。
     * 关闭后所有 read-through 直接回源，行为与无缓存一致。
     */
    public PluginCatalogCache() {
        this(null, DEFAULT_TTL_SECONDS, false);
    }

    /**
     * Spring 装配。
     *
     * @param store        存储后端
     * @param ttlSeconds   TTL（秒）
     * @param enabled      总开关，运维可在不上线代码的前提下整体关掉缓存
     */
    @Autowired
    public PluginCatalogCache(CatalogCacheStore store,
                              @Value("${houcloud.plugin.catalog-cache.ttl-seconds:300}") int ttlSeconds,
                              @Value("${houcloud.plugin.catalog-cache.enabled:true}") boolean enabled) {
        this.store = store;
        this.ttlSeconds = ttlSeconds > 0 ? ttlSeconds : DEFAULT_TTL_SECONDS;
        this.enabled = enabled && store != null;
    }

    /**
     * 缓存是否启用（未装配后端或开关关闭时返回 false）。
     *
     * @return true 表示启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 读穿透：命中直接返回，未命中回源并回填。
     *
     * <p>revision 参与 key 拼接是<b>失效策略的核心</b>：{@link #bumpRevision()} 之后
     * 拼出来的 key 与之前不同，旧数据自然读不到，无需任何删除动作。
     *
     * @param namespace 命名空间（{@link #NAMESPACE_CATALOG} / {@link #NAMESPACE_LATEST}）
     * @param key       业务键（如 {@code hash(keyword|category|page)}）
     * @param type      目标类型
     * @param loader    回源逻辑
     * @param <T>       缓存值类型
     * @return 缓存值或回源值
     */
    public <T> T readThrough(String namespace, String key, TypeReference<T> type, Supplier<T> loader) {
        if (!enabled) {
            return loader.get();
        }
        String cacheKey = "hou:idx:" + namespace + ":" + revision() + ":" + key;
        try {
            String cached = store.get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                T parsed = JSON.parseObject(cached, type);
                if (parsed != null) {
                    hitCount.incrementAndGet();
                    return parsed;
                }
                log.warn("目录缓存反序列化失败，回源: key={}", cacheKey);
            }
        } catch (RuntimeException e) {
            degradeCount.incrementAndGet();
            log.warn("目录缓存读取失败，回源: key={} msg={}", cacheKey, e.getMessage());
        }
        missCount.incrementAndGet();
        T loaded = loader.get();
        writeBack(cacheKey, loaded);
        return loaded;
    }

    /**
     * 读穿透（Class 版，用于不含泛型的类型）。
     *
     * @param namespace 命名空间
     * @param key       业务键
     * @param type      目标类型
     * @param loader    回源逻辑
     * @param <T>       缓存值类型
     * @return 缓存值或回源值
     */
    public <T> T readThrough(String namespace, String key, Class<T> type, Supplier<T> loader) {
        if (!enabled) {
            return loader.get();
        }
        String cacheKey = "hou:idx:" + namespace + ":" + revision() + ":" + key;
        try {
            String cached = store.get(cacheKey);
            if (cached != null && !cached.isBlank()) {
                T parsed = JSON.parseObject(cached, type);
                if (parsed != null) {
                    hitCount.incrementAndGet();
                    return parsed;
                }
                log.warn("目录缓存反序列化失败，回源: key={}", cacheKey);
            }
        } catch (RuntimeException e) {
            degradeCount.incrementAndGet();
            log.warn("目录缓存读取失败，回源: key={} msg={}", cacheKey, e.getMessage());
        }
        missCount.incrementAndGet();
        T loaded = loader.get();
        writeBack(cacheKey, loaded);
        return loaded;
    }

    /**
     * 单插件最新版本缓存（{@code hou:idx:latest:{code}}）。
     *
     * <p><b>为什么不在 key 里带 revision</b>：它是按 code 精确定位的单键，
     * 数量等于插件数、有上界；整体 INCR 会把它一起打掉（无谓回源），
     * 因此单独用 {@link #invalidateLatest} 精确失效。
     *
     * @param pluginCode 插件编码
     * @param loader     回源逻辑（查已发布版本取 SemVer 最大值）
     * @return 最新版本号；没有已发布版本时返回 null
     */
    public String latestVersion(String pluginCode, Supplier<String> loader) {
        if (!enabled) {
            return loader.get();
        }
        String cacheKey = "hou:idx:" + NAMESPACE_LATEST + ":" + pluginCode;
        try {
            String cached = store.get(cacheKey);
            if (cached != null) {
                hitCount.incrementAndGet();
                return EMPTY_SENTINEL.equals(cached) ? null : cached;
            }
        } catch (RuntimeException e) {
            degradeCount.incrementAndGet();
            log.warn("最新版本缓存读取失败，回源: code={} msg={}", pluginCode, e.getMessage());
        }
        missCount.incrementAndGet();
        String loaded = loader.get();
        // 版本号是纯字符串：直接写原文，不能走 JSON 序列化（否则会带上引号，读回来变成 "1.10.0"）
        String stored = loaded == null ? EMPTY_SENTINEL : loaded.trim();
        try {
            store.set(cacheKey, stored, ttlSeconds);
        } catch (RuntimeException e) {
            degradeCount.incrementAndGet();
            log.warn("最新版本缓存回填失败，下次请求继续回源: code={} msg={}", pluginCode, e.getMessage());
        }
        return loaded;
    }

    /**
     * 让某一插件的「最新版本」缓存失效（上架 / 下架 / 审核通过后调用）。
     *
     * @param pluginCode 插件编码
     */
    public void invalidateLatest(String pluginCode) {
        if (!enabled || pluginCode == null || pluginCode.isBlank()) {
            return;
        }
        try {
            store.del("hou:idx:" + NAMESPACE_LATEST + ":" + pluginCode.trim());
        } catch (RuntimeException e) {
            degradeCount.incrementAndGet();
            log.warn("最新版本缓存失效失败，依赖 TTL 兜底: code={} msg={}", pluginCode, e.getMessage());
        }
    }

    /**
     * bump 全局 revision：上架 / 下架 / 定价变更 / 审核通过后调用，
     * 一次 INCR 让所有分页缓存整体失效（不是逐个删 key）。
     *
     * <p>失败不影响业务：退化为「本次不失效」，由 TTL 300s 兜底。
     */
    public void bumpRevision() {
        if (!enabled) {
            return;
        }
        try {
            Long revision = store.incr(REVISION_KEY);
            if (revision == null) {
                degradeCount.incrementAndGet();
                log.warn("目录缓存 revision 自增返回空，本次退化为不失效，依赖 TTL 兜底");
            }
        } catch (RuntimeException e) {
            degradeCount.incrementAndGet();
            log.warn("目录缓存 revision 自增失败，依赖 TTL 兜底: msg={}", e.getMessage());
        }
    }

    /**
     * 当前 revision（用于观测与排障）。
     *
     * @return revision；不可用返回 {@code "0"}
     */
    public String currentRevision() {
        return revision();
    }

    /**
     * 缓存命中次数（观测用）。
     *
     * @return 命中次数
     */
    public long hitCount() {
        return hitCount.get();
    }

    /**
     * 缓存未命中次数（观测用）。
     *
     * @return 未命中次数
     */
    public long missCount() {
        return missCount.get();
    }

    /**
     * 降级（缓存不可用而回源）次数（观测用）。
     *
     * @return 降级次数
     */
    public long degradeCount() {
        return degradeCount.get();
    }

    private String revision() {
        try {
            String revision = store.get(REVISION_KEY);
            return revision == null || revision.isBlank() ? FALLBACK_REVISION : revision.trim();
        } catch (RuntimeException e) {
            degradeCount.incrementAndGet();
            return FALLBACK_REVISION;
        }
    }

    /**
     * 把回源结果写回缓存。
     *
     * <p><b>为什么用 fastjson2 原生 API（{@code com.alibaba.fastjson2}）而不是
     * {@code com.alibaba.fastjson} 兼容层</b>：兼容层默认开启「循环引用检测」，
     * 遇到<b>同一个对象实例出现两次</b>时会写出 {@code {"$ref":"$.items[0].dependencies"}}——
     * 而 {@code List.of()} 返回的空列表是全局共享实例，目录项的
     * tags / terminals / dependencies 三个空列表会被写成引用，反序列化直接报
     * {@code illegal fieldName input{}，结果是缓存永远读不回来（每请求都回源，等于没缓存）。
     * fastjson2 原生 API 默认不开启引用检测，输出可安全回读。
     *
     * @param cacheKey 完整缓存键（含 revision）
     * @param value    待写入的值，为 null 时不写
     */
    private void writeBack(String cacheKey, Object value) {
        if (value == null) {
            return;
        }
        try {
            store.set(cacheKey, JSON.toJSONString(value), ttlSeconds);
        } catch (RuntimeException e) {
            degradeCount.incrementAndGet();
            log.warn("目录缓存回填失败，下次请求继续回源: key={} msg={}", cacheKey, e.getMessage());
        }
    }
}
