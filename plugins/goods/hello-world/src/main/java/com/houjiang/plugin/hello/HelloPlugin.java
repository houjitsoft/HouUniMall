package com.houjiang.plugin.hello;

import com.houjiang.plugin.sdk.annotation.HouPlugin;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/** PF4J 从 MANIFEST 的 Plugin-Class 装载；业务接口由插件引擎扫描。 */
@HouPlugin(code = "hello-world", name = "Hello World 插件", version = "1.0.0")
public class HelloPlugin extends Plugin {
    public HelloPlugin(PluginWrapper wrapper) { super(wrapper); }
}
