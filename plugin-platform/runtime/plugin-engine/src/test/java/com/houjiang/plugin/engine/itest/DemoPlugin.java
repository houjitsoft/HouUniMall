package com.houjiang.plugin.engine.itest;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/** 最小 PF4J 插件入口（仅用于闭环测试，对应 MANIFEST 的 Plugin-Class）。 */
public class DemoPlugin extends Plugin {
    public DemoPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
}
