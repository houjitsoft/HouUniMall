package com.houjiang.plugin.engine.config;

import com.houjiang.plugin.api.extension.auth.LoginAfterExtension;
import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.api.extension.mall.DashboardWidgetExtension;
import com.houjiang.plugin.api.extension.order.FreightCalcExtension;
import com.houjiang.plugin.api.extension.order.PromotionCalcExtension;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 降级策略表单测（T01 / R3.2）。
 *
 * <p>验证：内置 17 点默认表；未知点默认 ABORT；锁定集识别；配置覆盖；锁定集改 IGNORE 启动
 * fail-fast；键大小写 / kebab 容错。
 *
 * @author HouCloud
 */
class ExtensionPolicyRegistryTest {

    /** 未登记扩展点：应回落到 fail-closed 的 ABORT。 */
    interface UnknownExtension extends ExtensionPoint<ExtensionContext> {
    }

    @Test
    void builtInTableCoversSeventeenPoints() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();
        assertEquals(17, registry.snapshot().size(), "内置默认表应覆盖全部 17 个扩展点");
    }

    @Test
    void defaultsMatchDesignedTiers() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();

        assertEquals(ExtensionDegradePolicy.ABORT, registry.policyOf(PriceCalcExtension.class));
        assertEquals(ExtensionDegradePolicy.FALLBACK, registry.policyOf(PromotionCalcExtension.class));
        assertEquals(ExtensionDegradePolicy.FALLBACK, registry.policyOf(FreightCalcExtension.class));
        assertEquals(ExtensionDegradePolicy.IGNORE, registry.policyOf(LoginAfterExtension.class));
        assertEquals(ExtensionDegradePolicy.IGNORE, registry.policyOf(DashboardWidgetExtension.class));
    }

    @Test
    void unknownPointDefaultsToAbort() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();
        assertEquals(ExtensionDegradePolicy.ABORT, registry.policyOf(UnknownExtension.class),
                "未知扩展点必须 fail-closed，默认 ABORT");
    }

    @Test
    void lockedSetIsRecognized() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();
        assertTrue(registry.isLocked(PriceCalcExtension.class));
        assertFalse(registry.isLocked(LoginAfterExtension.class));
        assertFalse(registry.isLocked(UnknownExtension.class));
    }

    @Test
    void overridesWinOverDefaults() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();
        registry.applyOverrides(Map.of("PromotionCalc", "IGNORE"));

        assertEquals(ExtensionDegradePolicy.IGNORE, registry.policyOf(PromotionCalcExtension.class));
    }

    @Test
    void lockedPointIgnoredOverrideFailsFast() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> registry.applyOverrides(Map.of("PriceCalc", "IGNORE")),
                "锁定集改 IGNORE 必须 fail-fast");
        assertTrue(error.getMessage().contains("PriceCalc"), "错误信息应指明违规扩展点");
    }

    @Test
    void lockedPointAbortOverrideIsAllowed() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();
        registry.applyOverrides(Map.of("PriceCalc", "ABORT"));
        assertEquals(ExtensionDegradePolicy.ABORT, registry.policyOf(PriceCalcExtension.class));
    }

    @Test
    void kebabCaseAndCaseInsensitiveKeysMatch() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();
        registry.applyOverrides(Map.of("promotion-calc", "ignore"));
        assertEquals(ExtensionDegradePolicy.IGNORE, registry.policyOf(PromotionCalcExtension.class));

        ExtensionPolicyRegistry registry2 = new ExtensionPolicyRegistry();
        assertThrows(IllegalStateException.class,
                () -> registry2.applyOverrides(Map.of("price_calc", "IGNORE")),
                "kebab/snake 均须能命中锁定集");
    }

    @Test
    void invalidPolicyValueFailsFast() {
        ExtensionPolicyRegistry registry = new ExtensionPolicyRegistry();
        assertThrows(IllegalArgumentException.class,
                () -> registry.applyOverrides(Map.of("PromotionCalc", "NOT_A_POLICY")));
    }
}
