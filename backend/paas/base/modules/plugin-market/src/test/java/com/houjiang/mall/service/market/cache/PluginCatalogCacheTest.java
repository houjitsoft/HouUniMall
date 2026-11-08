package com.houjiang.mall.service.market.cache;

import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 目录索引缓存测试。
 *
 * <p>钉死四件容易写错的事：
 * <ol>
 *   <li><b>revision 失效</b>：INCR 之后必须读到新数据，而不是靠 TTL 等 5 分钟；</li>
 *   <li><b>失效是整体失效、不是逐个删 key</b>：分页 key 数量无上界，逐个删会造成雪崩；</li>
 *   <li><b>Redis 挂了要回源</b>：缓存是加速手段，读失败不能让接口报错；</li>
 *   <li><b>latest 是独立缓存</b>：revision 不参与它的 key，必须靠单键失效。</li>
 * </ol>
 *
 * @author Amos
 * @since M5
 */
class PluginCatalogCacheTest {

    /**
     * 内存版存储后端：既能验证命中行为，也能通过开关模拟「Redis 不可用」。
     */
    private static final class MemoryStore implements CatalogCacheStore {

        private final Map<String, String> data = new HashMap<>();

        private boolean broken = false;

        private boolean failOnWrite = false;

        private int incrCount = 0;

        @Override
        public String get(String key) {
            if (broken) {
                throw new IllegalStateException("模拟 Redis 不可用");
            }
            return data.get(key);
        }

        @Override
        public void set(String key, String value, int ttlSeconds) {
            if (broken || failOnWrite) {
                throw new IllegalStateException("模拟 Redis 写入失败");
            }
            data.put(key, value);
        }

        @Override
        public Long incr(String key) {
            if (broken) {
                throw new IllegalStateException("模拟 Redis 不可用");
            }
            incrCount++;
            long next = Long.parseLong(data.getOrDefault(key, "0")) + 1L;
            data.put(key, String.valueOf(next));
            return next;
        }

        @Override
        public void del(String key) {
            if (broken) {
                throw new IllegalStateException("模拟 Redis 不可用");
            }
            data.remove(key);
        }
    }

    @Test
    void readThroughServesFromCacheUntilRevisionBumps() {
        MemoryStore store = new MemoryStore();
        PluginCatalogCache cache = new PluginCatalogCache(store, 300, true);
        AtomicInteger loaderCalls = new AtomicInteger();

        String first = cache.readThrough(PluginCatalogCache.NAMESPACE_CATALOG, "page1",
                new TypeReference<String>() {
                }, () -> {
                    loaderCalls.incrementAndGet();
                    return "v1";
                });
        String second = cache.readThrough(PluginCatalogCache.NAMESPACE_CATALOG, "page1",
                new TypeReference<String>() {
                }, () -> {
                    loaderCalls.incrementAndGet();
                    return "v2";
                });

        assertEquals("v1", first);
        assertEquals("v1", second, "第二次必须命中缓存，而不是重新回源");
        assertEquals(1, loaderCalls.get());
        assertEquals(1L, cache.hitCount());
        assertEquals(1L, cache.missCount());

        // ★整体失效：一次 INCR 让所有分页 key 全部落空（不是逐个删 key）
        cache.bumpRevision();
        String third = cache.readThrough(PluginCatalogCache.NAMESPACE_CATALOG, "page1",
                new TypeReference<String>() {
                }, () -> {
                    loaderCalls.incrementAndGet();
                    return "v3";
                });
        assertEquals("v3", third, "bumpRevision 之后必须读到新数据");
        assertEquals(2, loaderCalls.get());
        assertEquals(1, store.incrCount, "失效只应产生一次 INCR，不是每个 key 一次");
    }

    @Test
    void disabledCacheAlwaysGoesToSource() {
        PluginCatalogCache cache = new PluginCatalogCache();
        assertFalse(cache.isEnabled());
        AtomicInteger loaderCalls = new AtomicInteger();
        for (int i = 0; i < 3; i++) {
            cache.readThrough(PluginCatalogCache.NAMESPACE_CATALOG, "page1", String.class,
                    () -> {
                        loaderCalls.incrementAndGet();
                        return "value";
                    });
        }
        assertEquals(3, loaderCalls.get(), "缓存关闭时必须每次回源");
    }

    @Test
    void degradesToSourceWhenStoreIsBroken() {
        MemoryStore store = new MemoryStore();
        store.broken = true;
        PluginCatalogCache cache = new PluginCatalogCache(store, 300, true);

        String value = cache.readThrough(PluginCatalogCache.NAMESPACE_CATALOG, "page1", String.class,
                () -> "from-source");

        assertEquals("from-source", value, "Redis 不可用必须回源，而不是抛异常");
        assertTrue(cache.degradeCount() > 0, "降级次数应被记下，便于排障");

        // 连 revision 与失效都不能因异常冒泡
        assertEquals("0", cache.currentRevision());
        cache.bumpRevision();
        cache.invalidateLatest("mall-live");
    }

    @Test
    void writeBackFailureDoesNotBreakRead() {
        MemoryStore store = new MemoryStore();
        store.failOnWrite = true;
        PluginCatalogCache cache = new PluginCatalogCache(store, 300, true);

        String value = cache.readThrough(PluginCatalogCache.NAMESPACE_CATALOG, "page1", String.class,
                () -> "from-source");

        assertEquals("from-source", value, "回填失败只影响下次命中率，不能影响本次结果");
        assertEquals(1L, cache.degradeCount());
    }

    @Test
    void latestVersionIsCachedUntilInvalidated() {
        MemoryStore store = new MemoryStore();
        PluginCatalogCache cache = new PluginCatalogCache(store, 300, true);
        AtomicInteger loaderCalls = new AtomicInteger();

        assertEquals("1.10.0", cache.latestVersion("mall-live", () -> {
            loaderCalls.incrementAndGet();
            return "1.10.0";
        }));
        assertEquals("1.10.0", cache.latestVersion("mall-live", () -> {
            loaderCalls.incrementAndGet();
            return "9.9.9";
        }));
        assertEquals(1, loaderCalls.get(), "未失效前必须一直命中缓存");

        // 上架新版本后必须显式失效，否则「有更新」判定会一直用旧值
        cache.invalidateLatest("mall-live");
        assertEquals("9.9.9", cache.latestVersion("mall-live", () -> {
            loaderCalls.incrementAndGet();
            return "9.9.9";
        }));
        assertEquals(2, loaderCalls.get());
    }

    @Test
    void latestVersionCachesNegativeResult() {
        MemoryStore store = new MemoryStore();
        PluginCatalogCache cache = new PluginCatalogCache(store, 300, true);
        AtomicInteger loaderCalls = new AtomicInteger();

        assertNull(cache.latestVersion("offline-plugin", () -> {
            loaderCalls.incrementAndGet();
            return null;
        }));
        assertNull(cache.latestVersion("offline-plugin", () -> {
            loaderCalls.incrementAndGet();
            return null;
        }), "「没有已发布版本」也要缓存，否则离线插件每次都回源");
        assertEquals(1, loaderCalls.get());
    }

    @Test
    void readThroughSupportsComplexTypes() {
        MemoryStore store = new MemoryStore();
        PluginCatalogCache cache = new PluginCatalogCache(store, 300, true);
        List<String> expected = List.of("a", "b");

        List<String> first = cache.readThrough(PluginCatalogCache.NAMESPACE_CATALOG, "list",
                new TypeReference<List<String>>() {
                }, () -> expected);
        List<String> second = cache.readThrough(PluginCatalogCache.NAMESPACE_CATALOG, "list",
                new TypeReference<List<String>>() {
                }, () -> List.of("changed"));

        assertEquals(expected, first);
        assertEquals(expected, second, "泛型类型必须能被正确反序列化并命中");
    }
}
