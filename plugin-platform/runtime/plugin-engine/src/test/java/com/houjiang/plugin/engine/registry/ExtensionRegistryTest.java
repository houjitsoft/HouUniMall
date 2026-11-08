package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionMode;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 扩展点快照语义单测：验证 §7.4.1「排序移到注册/移除阶段」与 §7.4.2 两条 P0
 * 「请求期无复制排序」「快照切换原子」的验收门槛。
 *
 * @author HouCloud
 */
class ExtensionRegistryTest {

    /** 被测扩展点类型（一）：只用于给注册表提供 Class 键 */
    private interface AlphaExtension extends ExtensionPoint<ExtensionContext> {
    }

    /** 被测扩展点类型（二）：验证按类型分片，互不干扰 */
    private interface BetaExtension extends ExtensionPoint<ExtensionContext> {
    }

    private static final class Ext implements AlphaExtension, BetaExtension {

        private final String name;
        private final int priority;

        Ext(String name, int priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public void execute(ExtensionContext context) {
            // 注册表不关心实现逻辑
        }
    }

    private static List<String> names(List<AlphaExtension> extensions) {
        List<String> names = new ArrayList<>(extensions.size());
        for (AlphaExtension extension : extensions) {
            names.add(((Ext) extension).name);
        }
        return names;
    }

    @Test
    void repeatedReadsReturnSameImmutableSnapshotWithoutCopying() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("high", 200), "plugin-a", 200);
        registry.register(AlphaExtension.class, new Ext("low", 1), "plugin-b", 1);

        List<AlphaExtension> first = registry.getExtensions(AlphaExtension.class);
        List<AlphaExtension> second = registry.getExtensions(AlphaExtension.class);

        // 同一次注册结果被反复读取时必须命中同一实例：请求期没有 new ArrayList + sort
        assertSame(first, second, "同一快照必须被复用，不能在读取时重新复制排序");
        assertThrows(UnsupportedOperationException.class, () -> first.add(new Ext("mutate", 5)),
                "快照必须是不可变列表，禁止调用方修改共享状态");
        assertEquals(List.of("low", "high"), names(first), "应按 priority 升序");
    }

    @Test
    void ordersByPriorityAndKeepsRegistrationOrderForTies() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("c-100", 100), "plugin-a", 100);
        registry.register(AlphaExtension.class, new Ext("a-10", 10), "plugin-a", 10);
        registry.register(AlphaExtension.class, new Ext("b-100", 100), "plugin-b", 100);

        List<AlphaExtension> extensions = registry.getExtensions(AlphaExtension.class);

        // 稳定排序：同级实现保持注册先后，避免「按加载顺序碰运气」之外再引入随机序
        assertEquals(List.of("a-10", "c-100", "b-100"), names(extensions));
    }

    @Test
    void returnsEmptyListForUnknownExtensionPoint() {
        ExtensionRegistry registry = new ExtensionRegistry();

        List<BetaExtension> extensions = registry.getExtensions(BetaExtension.class);

        assertNotNull(extensions);
        assertTrue(extensions.isEmpty(), "无实现时必须返回空列表，保证 ExtensionInvoker 零开销");
    }

    @Test
    void registerOnlyRepublishesAffectedExtensionPoint() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("alpha", 1), "plugin-a", 1);
        List<AlphaExtension> alphaBefore = registry.getExtensions(AlphaExtension.class);

        registry.register(BetaExtension.class, new Ext("beta", 1), "plugin-a", 1);

        // 未受影响的扩展点沿用旧快照片段，不做无谓重建
        assertSame(alphaBefore, registry.getExtensions(AlphaExtension.class));
        assertEquals(1, registry.getExtensions(BetaExtension.class).size());
    }

    @Test
    void previouslyReturnedSnapshotIsNotMutatedInPlace() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("first", 1), "plugin-a", 1);
        List<AlphaExtension> captured = registry.getExtensions(AlphaExtension.class);

        registry.register(AlphaExtension.class, new Ext("second", 2), "plugin-b", 2);

        // 已发出的引用保持原样，证明是「整体替换快照」而非「就地修改共享列表」
        assertEquals(List.of("first"), names(captured), "旧快照不得被就地追加");
        assertEquals(List.of("first", "second"), names(registry.getExtensions(AlphaExtension.class)));
        assertEquals(1, captured.size(), "旧快照长度不变");
    }

    @Test
    void unregisterByPluginRebuildsSnapshotAndClearsEmptySlice() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("a1", 1), "plugin-a", 1);
        registry.register(AlphaExtension.class, new Ext("b1", 2), "plugin-b", 2);
        registry.register(AlphaExtension.class, new Ext("a2", 3), "plugin-a", 3);
        registry.register(BetaExtension.class, new Ext("b-beta", 1), "plugin-b", 1);
        List<AlphaExtension> captured = registry.getExtensions(AlphaExtension.class);

        registry.unregisterByPlugin("plugin-a");

        assertEquals(List.of("b1"), names(registry.getExtensions(AlphaExtension.class)));
        assertEquals(List.of("a1", "b1", "a2"), names(captured), "卸载不得就地修改卸载前已发出的快照");
        assertEquals(1, registry.getExtensions(BetaExtension.class).size(), "其他插件不受影响");

        registry.unregisterByPlugin("plugin-b");
        assertTrue(registry.getExtensions(AlphaExtension.class).isEmpty(), "无实现的类型应回到空列表");
        assertTrue(registry.getExtensions(BetaExtension.class).isEmpty());
    }

    @Test
    void unregisterUnknownPluginIsNoopAndKeepsSnapshotInstance() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("a", 1), "plugin-a", 1);
        List<AlphaExtension> before = registry.getExtensions(AlphaExtension.class);

        registry.unregisterByPlugin("not-installed");

        assertSame(before, registry.getExtensions(AlphaExtension.class), "无匹配插件时不应重建快照");
    }

    @Test
    void concurrentRegistrationNeverExposesHalfAppliedSnapshot() throws InterruptedException {
        ExtensionRegistry registry = new ExtensionRegistry();
        int writers = 8;
        int perWriter = 50;
        int total = writers * perWriter;
        registry.register(AlphaExtension.class, new Ext("seed", 0), "plugin-seed", 0);

        AtomicBoolean writing = new AtomicBoolean(true);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        List<Thread> readers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Thread reader = new Thread(() -> {
                int lastSize = 0;
                try {
                    while (writing.get()) {
                        List<AlphaExtension> snapshot = registry.getExtensions(AlphaExtension.class);
                        if (snapshot.size() < lastSize) {
                            throw new IllegalStateException("快照大小回退：" + lastSize + " -> " + snapshot.size());
                        }
                        lastSize = snapshot.size();
                        int previous = Integer.MIN_VALUE;
                        for (AlphaExtension extension : snapshot) {
                            int priority = ((Ext) extension).priority;
                            if (priority < previous) {
                                throw new IllegalStateException("读到未排序的半套快照");
                            }
                            previous = priority;
                        }
                        Thread.onSpinWait();
                    }
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            });
            readers.add(reader);
            reader.start();
        }

        List<Thread> writerThreads = new ArrayList<>();
        for (int w = 0; w < writers; w++) {
            final int base = w * perWriter;
            Thread writer = new Thread(() -> {
                try {
                    for (int i = 0; i < perWriter; i++) {
                        int priority = base + i + 1;
                        registry.register(AlphaExtension.class, new Ext("w" + base + "-" + i, priority),
                                "plugin-" + base, priority);
                    }
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            });
            writerThreads.add(writer);
            writer.start();
        }

        for (Thread writer : writerThreads) {
            writer.join();
        }
        writing.set(false);
        for (Thread reader : readers) {
            reader.join();
        }

        assertNull(failure.get(), "并发注册/读取期间不应出现异常或半套快照");
        List<AlphaExtension> finalSnapshot = registry.getExtensions(AlphaExtension.class);
        assertEquals(total + 1, finalSnapshot.size(), "并发注册的实现不得丢失");
        int previous = Integer.MIN_VALUE;
        for (AlphaExtension extension : finalSnapshot) {
            int priority = ((Ext) extension).priority;
            assertTrue(priority >= previous, "最终快照必须整体有序");
            previous = priority;
        }
    }

    // ===== §7.4.2 P0「二开实现冲突」 =====

    /** 与 Ext 不同的实现类，用于构造「同一扩展点的两个不同实现」 */
    private static final class OtherExt implements AlphaExtension {
        @Override
        public void execute(ExtensionContext context) {
        }
    }

    @Test
    void samePriorityCustomImplementationsConflict() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("custom-1", 100), "plugin-a", 100,
                ExtensionRegistry.OWNER_CUSTOM);

        // 同扩展点、同 priority、同为 custom、不同实现类 → 注册阶段直接失败
        assertThrows(IllegalStateException.class, () -> registry.register(
                AlphaExtension.class, new OtherExt(), "plugin-b", 100, ExtensionRegistry.OWNER_CUSTOM));

        // 冲突后注册表保持不变：Core 不进入半生效状态
        assertEquals(List.of("custom-1"), names(registry.getExtensions(AlphaExtension.class)),
                "冲突时不得写入任何一方，快照须保持冲突前状态");
    }

    @Test
    void conflictMessageNamesBothImplementations() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("custom-1", 5), "plugin-a", 5,
                ExtensionRegistry.OWNER_CUSTOM);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> registry.register(
                AlphaExtension.class, new OtherExt(), "plugin-b", 5, ExtensionRegistry.OWNER_CUSTOM));

        String message = error.getMessage();
        assertTrue(message.contains("priority=5"), "错误信息应含优先级，实际=" + message);
        assertTrue(message.contains("plugin-a") && message.contains("plugin-b"),
                "错误信息应含双方来源插件，实际=" + message);
        assertTrue(message.contains(OtherExt.class.getName()) || message.contains("OtherExt"),
                "错误信息应含新注册实现类，实际=" + message);
    }

    @Test
    void officialAndCustomWithSamePriorityDoNotConflict() {
        ExtensionRegistry registry = new ExtensionRegistry();
        // 官方与二开同级不算冲突：二开应覆盖官方，由 priority/owner 解析决定
        registry.register(AlphaExtension.class, new Ext("official", 100), "core", 100,
                ExtensionRegistry.OWNER_OFFICIAL);
        registry.register(AlphaExtension.class, new OtherExt(), "plugin-custom", 100,
                ExtensionRegistry.OWNER_CUSTOM);

        assertEquals(2, registry.getExtensions(AlphaExtension.class).size());
    }

    @Test
    void customImplementationsWithDifferentPriorityDoNotConflict() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("custom-1", 100), "plugin-a", 100,
                ExtensionRegistry.OWNER_CUSTOM);
        registry.register(AlphaExtension.class, new OtherExt(), "plugin-b", 200,
                ExtensionRegistry.OWNER_CUSTOM);

        assertEquals(2, registry.getExtensions(AlphaExtension.class).size(),
                "优先级不同即有意覆盖关系，不算冲突");
    }

    @Test
    void sameOwnerSamePrioritySameClassFromDifferentPluginsDoesNotConflict() {
        ExtensionRegistry registry = new ExtensionRegistry();
        // 同一实现类被两个插件各自注册（同一份能力包被两处声明）：按方案以类为判定单位，不视为冲突
        registry.register(AlphaExtension.class, new OtherExt(), "plugin-a", 7,
                ExtensionRegistry.OWNER_CUSTOM);
        registry.register(AlphaExtension.class, new OtherExt(), "plugin-b", 7,
                ExtensionRegistry.OWNER_CUSTOM);

        assertEquals(2, registry.getExtensions(AlphaExtension.class).size());
    }

    @Test
    void customReplacementSuppressesOfficialAndUnregisterRestoresIt() {
        ExtensionRegistry registry = new ExtensionRegistry();
        Ext official = new Ext("official", 0);
        OtherExt replacement = new OtherExt();

        registry.register(AlphaExtension.class, official, "core", 0,
                ExtensionRegistry.OWNER_OFFICIAL);
        registry.register(AlphaExtension.class, replacement, "custom-goods", 100,
                ExtensionRegistry.OWNER_CUSTOM, ExtensionMode.REPLACE);

        assertEquals(1, registry.getExtensions(AlphaExtension.class).size());
        assertSame(replacement, registry.getExtensions(AlphaExtension.class).get(0));

        registry.unregisterByPlugin("custom-goods");
        assertEquals(List.of("official"), names(registry.getExtensions(AlphaExtension.class)));
    }

    @Test
    void twoCustomReplacementsConflictEvenWhenPriorityDiffers() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new OtherExt(), "custom-a", 100,
                ExtensionRegistry.OWNER_CUSTOM, ExtensionMode.REPLACE);

        assertThrows(IllegalStateException.class, () -> registry.register(
                AlphaExtension.class, new Ext("replacement", 200), "custom-b", 200,
                ExtensionRegistry.OWNER_CUSTOM, ExtensionMode.REPLACE));
        assertEquals(1, registry.getExtensions(AlphaExtension.class).size());
    }

    @Test
    void repeatedRegistrationByStableSourceAndClassIsIdempotent() {
        ExtensionRegistry registry = new ExtensionRegistry();
        Ext first = new Ext("first", 3);
        Ext second = new Ext("second", 3);

        registry.register(AlphaExtension.class, first, "custom-goods", 3,
                ExtensionRegistry.OWNER_CUSTOM);
        registry.register(AlphaExtension.class, second, "custom-goods", 3,
                ExtensionRegistry.OWNER_CUSTOM);

        assertEquals(1, registry.getExtensions(AlphaExtension.class).size());
        assertSame(first, registry.getExtensions(AlphaExtension.class).get(0));
    }

    @Test
    void exposesImmutableRegistrationMetadataWithoutImplementationInstances() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(AlphaExtension.class, new Ext("official", 0), "core", 0,
                ExtensionRegistry.OWNER_OFFICIAL);
        registry.register(AlphaExtension.class, new OtherExt(), "custom-goods", 100,
                ExtensionRegistry.OWNER_CUSTOM, ExtensionMode.REPLACE);

        List<ExtensionRegistry.Registration> registrations = registry.getRegistrations(AlphaExtension.class);

        assertEquals(1, registrations.size(), "REPLACE 快照不应暴露被替换的官方实现");
        assertEquals("custom-goods", registrations.get(0).sourceCode());
        assertEquals(ExtensionRegistry.OWNER_CUSTOM, registrations.get(0).owner());
        assertEquals(ExtensionMode.REPLACE, registrations.get(0).mode());
        assertEquals(OtherExt.class.getName(), registrations.get(0).implementationClass());
        assertThrows(UnsupportedOperationException.class,
                () -> registrations.add(new ExtensionRegistry.Registration("x", "x", "custom", 1,
                        ExtensionMode.APPEND)));
    }

    @Test
    void replacementIsReservedForBuildTimeCustomizations() {
        ExtensionRegistry registry = new ExtensionRegistry();

        assertThrows(IllegalArgumentException.class, () -> registry.register(
                AlphaExtension.class, new Ext("third-party", 10), "demo-coupon", 10,
                ExtensionRegistry.OWNER_THIRDPARTY, ExtensionMode.REPLACE));
    }

    @Test
    void repeatedRegistrationOfSameInstanceIsIdempotent() {
        ExtensionRegistry registry = new ExtensionRegistry();
        Ext instance = new Ext("custom-1", 3);
        registry.register(AlphaExtension.class, instance, "plugin-a", 3, ExtensionRegistry.OWNER_CUSTOM);
        // 停用后再启用会重复注册，必须幂等
        registry.register(AlphaExtension.class, instance, "plugin-a", 3, ExtensionRegistry.OWNER_CUSTOM);

        assertEquals(1, registry.getExtensions(AlphaExtension.class).size(), "同一实例重复注册不得产生重复项");
    }
}
