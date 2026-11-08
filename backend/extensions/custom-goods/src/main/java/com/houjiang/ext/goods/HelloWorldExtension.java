package com.houjiang.ext.goods;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.sdk.annotation.HouExtension;

/** 构建期二开：必须是 Spring Bean，code 必填；APPEND 不接管官方定价。 */
@HouExtension(point = PriceCalcExtension.class, code = "custom-goods", owner = "custom", priority = 100)
public class HelloWorldExtension implements PriceCalcExtension {
    @Override public boolean supports(Context ctx) { return ctx != null; }
    @Override public void execute(Context ctx) {
        ctx.extra.put("hello-world.custom", "Hello World");
    }
}
