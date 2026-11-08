package com.houjiang.plugin.api.ext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExtensionTableNaming} 单测（R2.1）：合法 / 非法 / 边界三类用例。
 *
 * @author Amos
 */
class ExtensionTableNamingTest {

    // ------------------------------------------------------------------
    // 合法
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "hou_ext_order",
            "hou_ext_order_item",
            "hou_ext_a",
            "hou_ext_x1",
            "hou_ext_a_b_c",
            "hou_ext_2024_ledger",
            "hou_ext_migration"
    })
    void 合法表名通过校验(String tableName) {
        assertTrue(ExtensionTableNaming.isValid(tableName), "应判为合法: " + tableName);
        assertDoesNotThrow(() -> ExtensionTableNaming.validate(tableName));
    }

    // ------------------------------------------------------------------
    // 非法
    // ------------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "order",                 // 无前缀
            "t_order",               // 宿主表
            "hou_ext",               // 前缀缺少结尾下划线
            "hou_extorder",          // 前缀后缺下划线
            "hou_ext-Order",         // '-' 非法字符
            "ext_order",             // 错误前缀
            "hou_erp_order",         // 其它插件前缀
            "hou_ext_Order",         // 大写后缀
            "HOU_EXT_order",         // 大写前缀
            "hou_ext_a b",           // 含空格
            "`hou_ext_order`",       // 含反引号
            "hou_ext._x",            // 后缀以非标识符字符开头
            "hou_ext_订单",           // 非 ASCII
            "hou_ext_other.hou_ext_a" // 库名限定
    })
    void 非法表名被拒绝(String tableName) {
        assertFalse(ExtensionTableNaming.isValid(tableName), "应判为非法: " + tableName);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ExtensionTableNaming.validate(tableName));
        assertTrue(ex.getMessage().contains(ExtensionTableNaming.PREFIX),
                "错误信息应点名约定前缀: " + ex.getMessage());
    }

    // ------------------------------------------------------------------
    // 边界
    // ------------------------------------------------------------------

    @Test
    void null与空白被拒绝() {
        assertFalse(ExtensionTableNaming.isValid(null));
        assertFalse(ExtensionTableNaming.isValid(""));
        assertFalse(ExtensionTableNaming.isValid("   "));
        assertFalse(ExtensionTableNaming.isValid("\t"));
        assertThrows(IllegalStateException.class, () -> ExtensionTableNaming.validate(null));
        assertThrows(IllegalStateException.class, () -> ExtensionTableNaming.validate(""));
        assertThrows(IllegalStateException.class, () -> ExtensionTableNaming.validate("   "));
    }

    @Test
    void 裸前缀无后缀被拒绝() {
        // "hou_ext_" 恰好等于前缀但没有业务后缀，属典型边界：不得视为合法表名
        assertEquals(ExtensionTableNaming.PREFIX, "hou_ext_");
        assertFalse(ExtensionTableNaming.isValid(ExtensionTableNaming.PREFIX));
        assertThrows(IllegalStateException.class, () -> ExtensionTableNaming.validate(ExtensionTableNaming.PREFIX));
    }

    @Test
    void 后缀不含分隔下划线之外的前缀拼接被拒绝() {
        // "hou_ext_" + "" = 裸前缀 → 非法
        assertThrows(IllegalStateException.class, () -> ExtensionTableNaming.of(""));
        // "hou_ext_" + "Order" 大写 → 非法
        assertThrows(IllegalStateException.class, () -> ExtensionTableNaming.of("Order"));
        // 合法后缀可正常拼装
        assertEquals("hou_ext_order_item", ExtensionTableNaming.of("order_item"));
    }

    @Test
    void 幂等且大小写敏感() {
        // 稳定不变：同一入参重复判定结果一致
        assertTrue(ExtensionTableNaming.isValid("hou_ext_order"));
        assertTrue(ExtensionTableNaming.isValid("hou_ext_order"));
        // 大小写敏感
        assertFalse(ExtensionTableNaming.isValid("HOU_EXT_ORDER"));
        assertFalse(ExtensionTableNaming.isValid("Hou_Ext_Order"));
    }
}
