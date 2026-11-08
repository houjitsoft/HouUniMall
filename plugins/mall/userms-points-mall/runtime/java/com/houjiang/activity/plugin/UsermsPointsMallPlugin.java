package com.houjiang.activity.plugin;

import com.houjiang.activity.support.ActivitySupport;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 积分商城插件的 PF4J 入口。
 *
 * <p>本插件由 activity 大插件按业务域拆分而来（userms 宿主的积分商城）。
 * 业务实现全部在本插件内部，不与其他活动插件共享数据层；
 * 跨插件/跨服务交互一律走 plugin-api 扩展点或 Feign。
 */
@Extension
public class UsermsPointsMallPlugin extends Plugin {

    private static final Logger log = LoggerFactory.getLogger(UsermsPointsMallPlugin.class);

    public UsermsPointsMallPlugin() {
        log.info("积分商城插件已加载，宿主：userms");
    }

    @Override
    public void start() {
        log.info("积分商城插件启动");
    }

    @Override
    public void stop() {
        log.info("积分商城插件停止");
    }

    @Override
    public void delete() {
        log.info("积分商城插件卸载");
    }
}
