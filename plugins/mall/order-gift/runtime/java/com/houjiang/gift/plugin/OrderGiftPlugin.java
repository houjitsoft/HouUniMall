package com.houjiang.gift.plugin;

import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 满赠插件的 PF4J 入口。
 *
 * <p>宿主：order。只做「规则配置 + 可赠判定 + 赠出登记」，
 * 不修改订单金额与订单明细表结构。
 */
@Extension
public class OrderGiftPlugin extends Plugin {

    private static final Logger log = LoggerFactory.getLogger(OrderGiftPlugin.class);

    public OrderGiftPlugin() {
        log.info("满赠插件已加载，宿主：order");
    }

    @Override
    public void start() {
        log.info("满赠插件启动");
    }

    @Override
    public void stop() {
        log.info("满赠插件停止");
    }

    @Override
    public void delete() {
        log.info("满赠插件卸载");
    }
}
