package com.houjiang.plugin.api.extension;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Noop 齐全性与语义核对（R4.3）。
 *
 * <p><b>事实修正</b>：17 个扩展点契约接口<b>均已内嵌 {@code class Noop implements Xxx}</b>
 * （{@code grep -rl "class Noop" backend/contracts/plugin-api} = 17）。因此 R4.3 不是「从零补 17 个文件」，
 * 而是核对内嵌 Noop 是否<b>齐全且语义正确</b>。本测试用反射对全部 17 个接口逐一核对：
 * <ol>
 *   <li>每个接口都声明了内嵌 {@code Noop}，且 {@code Noop} 实现该接口；</li>
 *   <li>空上下文（{@code execute(null)}）不抛异常；</li>
 *   <li>真实上下文调用不抛异常、不 {@code abort}（Noop 永不中断）；</li>
 *   <li>只读类不篡改入参、写回类不越界——Noop 对上下文公开字段零写入。</li>
 * </ol>
 *
 * @author Amos
 */
class NoopSmokeTest {

    /** 17 个扩展点契约接口（与《扩展点清单》一致），顺序无关紧要。 */
    private static final List<Class<?>> POINTS = List.of(
            com.houjiang.plugin.api.extension.auth.LoginAfterExtension.class,
            com.houjiang.plugin.api.extension.goods.PriceCalcExtension.class,
            com.houjiang.plugin.api.extension.goods.StockChangeExtension.class,
            com.houjiang.plugin.api.extension.goods.StockLockExtension.class,
            com.houjiang.plugin.api.extension.mall.DashboardWidgetExtension.class,
            com.houjiang.plugin.api.extension.order.AfterSaleCreateExtension.class,
            com.houjiang.plugin.api.extension.order.CartCalcExtension.class,
            com.houjiang.plugin.api.extension.order.CartValidateExtension.class,
            com.houjiang.plugin.api.extension.order.FreightCalcExtension.class,
            com.houjiang.plugin.api.extension.order.OrderCreateAfterExtension.class,
            com.houjiang.plugin.api.extension.order.OrderCreateBeforeExtension.class,
            com.houjiang.plugin.api.extension.order.PromotionCalcExtension.class,
            com.houjiang.plugin.api.extension.payment.PayCallbackExtension.class,
            com.houjiang.plugin.api.extension.payment.PromotionPaySettleExtension.class,
            com.houjiang.plugin.api.extension.payment.RefundCreateExtension.class,
            com.houjiang.plugin.api.extension.permission.MenuContributionExtension.class,
            com.houjiang.plugin.api.extension.user.RedpacketQueryExtension.class);

    @Test
    void 扩展点总数为17() {
        assertEquals(17, POINTS.size(), "扩展点契约应为 17 个");
    }

    @Test
    void 每个扩展点都内嵌齐全的Noop实现() {
        List<String> missing = new ArrayList<>();
        for (Class<?> point : POINTS) {
            Class<?> noop = findNested(point, "Noop");
            if (noop == null || !point.isAssignableFrom(noop)) {
                missing.add(point.getName());
            }
        }
        assertTrue(missing.isEmpty(), "缺少内嵌 Noop 的扩展点: " + missing);
    }

    @Test
    void 空上下文调用不抛异常() {
        for (Class<?> point : POINTS) {
            Object noop = newInstance(findNested(point, "Noop"));
            assertDoesNotThrow(() -> invoke(noop, null),
                    "Noop.execute(null) 不得抛异常: " + point.getName());
        }
    }

    @Test
    void 真实上下文不抛异常不中断且零写入() throws Exception {
        for (Class<?> point : POINTS) {
            Class<?> noopClass = findNested(point, "Noop");
            Class<?> ctxClass = findNested(point, "Context");
            assertNotNull(noopClass, "缺少 Noop: " + point.getName());
            assertNotNull(ctxClass, "缺少 Context: " + point.getName());

            Object noop = newInstance(noopClass);
            Object ctx = newInstance(ctxClass);
            assertTrue(ctx instanceof ExtensionContext, "Context 必须继承 ExtensionContext: " + ctxClass.getName());

            Map<String, Object> before = snapshot(ctxClass, ctx);

            assertDoesNotThrow(() -> invoke(noop, ctx),
                    "Noop.execute(ctx) 不得抛异常: " + point.getName());

            ExtensionContext base = (ExtensionContext) ctx;
            assertFalse(base.isAborted(), "Noop 不得中断后续处理: " + point.getName());
            assertEquals(null, base.getAbortReason(), "Noop 不得设置中断原因: " + point.getName());

            Map<String, Object> after = snapshot(ctxClass, ctx);
            assertEquals(before, after,
                    "Noop 不得改写上下文入参/越界写回（只读类不篡改、写回类不越界）: " + point.getName());
        }
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** 查找名为 {@code simpleName} 的直接内嵌类，找不到返回 {@code null}。 */
    private static Class<?> findNested(Class<?> owner, String simpleName) {
        for (Class<?> nested : owner.getDeclaredClasses()) {
            if (nested.getSimpleName().equals(simpleName)) {
                return nested;
            }
        }
        return null;
    }

    private static Object newInstance(Class<?> type) {
        assertNotNull(type, "待实例化的类型不存在");
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("无法实例化 " + type.getName(), e);
        }
    }

    /** 以本扩展点的 Context 类型解析并调用 {@code execute}；ctx 允许为 {@code null}。 */
    private static void invoke(Object noop, Object ctx) {
        Class<?> ctxType = resolveContextType(noop);
        try {
            var method = noop.getClass().getMethod("execute", ctxType);
            method.setAccessible(true);
            method.invoke(noop, ctx);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException("Noop.execute 反射调用失败: " + e.getMessage(), e);
        }
    }

    private static Class<?> resolveContextType(Object noop) {
        Class<?> point = noop.getClass().getInterfaces()[0];
        Class<?> ctx = findNested(point, "Context");
        assertNotNull(ctx, "缺少 Context: " + point.getName());
        return ctx;
    }

    /** 快照上下文的公开字段：集合/映射记长度，其余记引用，用于检测任何写入。 */
    private static Map<String, Object> snapshot(Class<?> ctxClass, Object ctx) throws IllegalAccessException {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        for (Field field : ctxClass.getFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            snapshot.put(field.getName(), measure(field.get(ctx)));
        }
        return snapshot;
    }

    private static Object measure(Object value) {
        if (value instanceof Collection<?> collection) {
            return "collection#" + collection.size();
        }
        if (value instanceof Map<?, ?> map) {
            return "map#" + map.size();
        }
        return value;
    }
}
