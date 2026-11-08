package com.houjiang.commons.version;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 依赖声明解析测试（M5b）。
 *
 * <p>这里钉死两件事：
 * <ol>
 *   <li><b>语义拆分</b>：{@code code} / {@code code@minVersion} 两种写法，
 *       以及历史实现里「{@code @} 在首位不拆」这个必须保持一致的行为；</li>
 *   <li><b>弱校验</b>：缺声明、非数组、非法 JSON 一律返回空列表——
 *       声明写错不该让已上架的插件整体升不了级。</li>
 * </ol>
 *
 * @author Amos
 * @since M5b
 */
class PluginDependencyParserTest {

    @Test
    void parsesBareCodes() {
        List<PluginDependencyExpression> deps = PluginDependencyParser.parse("[\"erp\",\"oa\"]");

        assertEquals(2, deps.size());
        assertEquals("erp", deps.get(0).code());
        assertNull(deps.get(0).minVersion());
        assertFalse(deps.get(0).requiresMin());
        assertEquals("oa", deps.get(1).code());
    }

    @Test
    void parsesCodeWithMinVersion() {
        List<PluginDependencyExpression> deps = PluginDependencyParser.parse("[\"erp@1.2.0\"]");

        assertEquals(1, deps.size());
        assertEquals("erp", deps.get(0).code());
        assertEquals("1.2.0", deps.get(0).minVersion());
        assertTrue(deps.get(0).requiresMin());
    }

    @Test
    void trimsWhitespace() {
        List<PluginDependencyExpression> deps = PluginDependencyParser.parse("[\"  erp  @  1.2.0  \"]");

        assertEquals("erp", deps.get(0).code());
        assertEquals("1.2.0", deps.get(0).minVersion());
    }

    @Test
    void acceptsSingleStringInsteadOfArray() {
        List<PluginDependencyExpression> deps = PluginDependencyParser.parse("\"erp@2.0.0\"");

        assertEquals(1, deps.size());
        assertEquals("erp", deps.get(0).code());
        assertEquals("2.0.0", deps.get(0).minVersion());
    }

    /**
     * 分隔符在首位时<b>不拆分</b>，整体当编码用——这是下沉前预检的既有行为，
     * 刻意保留（改掉会翻转线上脏数据的判定结果）。代价是这种脏声明会被判成「缺依赖」。
     */
    @Test
    void separatorAtHeadIsNotSplit() {
        List<PluginDependencyExpression> deps = PluginDependencyParser.parse("[\"@1.0.0\"]");

        assertEquals(1, deps.size());
        assertEquals("@1.0.0", deps.get(0).code());
        assertNull(deps.get(0).minVersion());
    }

    @Test
    void blankEntriesAreSkipped() {
        assertTrue(PluginDependencyParser.parse("[\"\",\"  \",null]").isEmpty());
    }

    @Test
    void emptyOrInvalidDeclarationsYieldEmptyList() {
        assertTrue(PluginDependencyParser.parse(null).isEmpty(), "未声明按无依赖处理");
        assertTrue(PluginDependencyParser.parse("").isEmpty());
        assertTrue(PluginDependencyParser.parse("   ").isEmpty());
        assertTrue(PluginDependencyParser.parse("{不是数组").isEmpty(), "非法 JSON 只 warn，不抛异常");
        assertTrue(PluginDependencyParser.parse("{\"a\":1}").isEmpty(), "非数组非字符串按无依赖处理");
        assertTrue(PluginDependencyParser.parse("123").isEmpty());
        assertTrue(PluginDependencyParser.parse("[]").isEmpty());
    }

    @Test
    void satisfiedByUsesLenientVersionComparison() {
        PluginDependencyExpression atLeast12 = PluginDependencyParser.parse("[\"erp@1.2.0\"]").get(0);

        assertTrue(atLeast12.satisfiedBy("1.2.0"), "相等即满足");
        assertTrue(atLeast12.satisfiedBy("1.10.0"), "宽松比较：1.10 > 1.2，不能按字典序判成不满足");
        assertTrue(atLeast12.satisfiedBy("2.0"), "两段历史版本号也要能比较");
        assertFalse(atLeast12.satisfiedBy("1.1.9"));
    }

    @Test
    void satisfiedByIsFailClosedOnUnknownVersion() {
        PluginDependencyExpression atLeast12 = PluginDependencyParser.parse("[\"erp@1.2.0\"]").get(0);

        assertFalse(atLeast12.satisfiedBy(null), "版本未知按不满足处理，不能放行");
        assertFalse(atLeast12.satisfiedBy(""));
        assertFalse(atLeast12.satisfiedBy("   "));
    }

    @Test
    void satisfiedByIsAlwaysTrueWhenNoMinVersionDeclared() {
        PluginDependencyExpression any = PluginDependencyParser.parse("[\"erp\"]").get(0);

        assertFalse(any.requiresMin());
        assertTrue(any.satisfiedBy(null), "没声明版本要求时不臆测版本门槛");
        assertTrue(any.satisfiedBy("0.0.1"));
    }

    @Test
    void toStringRoundTripsTheDeclaration() {
        assertEquals("erp", PluginDependencyParser.parse("[\"erp\"]").get(0).toString());
        assertEquals("erp@1.2.0", PluginDependencyParser.parse("[\"erp@1.2.0\"]").get(0).toString());
    }
}
