package com.houjiang.merchant.common.validation;

import com.houjiang.merchant.common.exception.OperateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertTest {

    @Test
    void notNullRaisesTrustedBusinessErrorOnlyForNullValues() {
        assertDoesNotThrow(() -> Assert.notNull("商品", "商品不存在"));

        OperateException exception = assertThrows(
                OperateException.class,
                () -> Assert.notNull(null, "商品不存在"));

        assertEquals("商品不存在", exception.getMsg());
    }

    @Test
    void isNullRaisesTrustedBusinessErrorOnlyForPresentValues() {
        assertDoesNotThrow(() -> Assert.isNull(null, "商品名称已存在"));

        OperateException exception = assertThrows(
                OperateException.class,
                () -> Assert.isNull("已有商品", "商品名称已存在"));

        assertEquals("商品名称已存在", exception.getMsg());
    }

    @Test
    void isTrueRaisesTrustedBusinessErrorOnlyForFalseConditions() {
        assertDoesNotThrow(() -> Assert.isTrue(true, "库存不足"));

        OperateException exception = assertThrows(
                OperateException.class,
                () -> Assert.isTrue(false, "库存不足"));

        assertEquals("库存不足", exception.getMsg());
    }

    @Test
    void isFalseRaisesTrustedBusinessErrorOnlyForTrueConditions() {
        assertDoesNotThrow(() -> Assert.isFalse(false, "顶级部门不能删除"));

        OperateException exception = assertThrows(
                OperateException.class,
                () -> Assert.isFalse(true, "顶级部门不能删除"));

        assertEquals("顶级部门不能删除", exception.getMsg());
    }
}
