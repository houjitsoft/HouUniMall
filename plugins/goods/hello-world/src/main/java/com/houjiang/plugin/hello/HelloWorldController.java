package com.houjiang.plugin.hello;

import com.houjiang.plugin.sdk.annotation.HouRoute;
import java.util.Map;

/** 装载至 goods 后由插件分发器处理；需登录并具备 hello-world:hello 权限。 */
public class HelloWorldController {
    @HouRoute(path = "/hello", method = "POST", permission = "hello-world:hello")
    public Object hello() {
        return Map.of("message", "Hello World", "mode", "plugin");
    }
}
