package com.houjiang.plugin.engine.itest;

import com.houjiang.plugin.sdk.annotation.HouRoute;

/** 演示插件自有接口：验证路由扫描与注册。 */
public class DemoRouteController {

    @HouRoute(path = "/calc", method = "POST", permission = "demo:calc")
    public Object calc() {
        return "ok";
    }
}
