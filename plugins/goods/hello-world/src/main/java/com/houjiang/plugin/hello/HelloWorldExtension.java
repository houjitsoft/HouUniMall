package com.houjiang.plugin.hello;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.sdk.annotation.HouExtension;

/** 最小扩展点示例，只写入演示标记，不更改价格、不写数据库。 */
@HouExtension(point = PriceCalcExtension.class, priority = 100, owner = "thirdparty")
public class HelloWorldExtension implements PriceCalcExtension {
    @Override public boolean supports(Context ctx) { return ctx != null; }
    @Override public void execute(Context ctx) {
        ctx.extra.put("hello-world.plugin", "Hello World");
    }
}
