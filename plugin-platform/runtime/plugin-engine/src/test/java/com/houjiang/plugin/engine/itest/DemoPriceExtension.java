package com.houjiang.plugin.engine.itest;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.sdk.annotation.HouExtension;

import java.math.BigDecimal;

/** 演示扩展点实现：价格打 9 折。验证「装载 → 注册 → 调用」闭环。 */
@HouExtension(point = PriceCalcExtension.class, priority = 10, owner = "thirdparty")
public class DemoPriceExtension implements PriceCalcExtension {
    @Override
    public void execute(Context ctx) {
        ctx.finalPrice = ctx.basePrice.multiply(new BigDecimal("0.9"));
    }
}
