package com.houjiang.plugin.api.extension.activity;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 活动详情查询扩展点。
 *
 * <p>供「按活动 ID 查详情并附带商品条目」的场景使用。典型调用方是拼团等需要
 * 对比其他活动（限时折扣、限时秒杀、满减）价格与商品明细的下单编排逻辑。
 *
 * <p>原实现由拼团插件直接注入各活动的 Mapper 查表（ActivityDiscountBaseMapper、
 * ActivitySeckillBaseMapper…），使一个插件反向依赖多个活动插件的数据层；
 * 插件化后插件间既不能互相 import，也不应共享数据层，故改为扩展点：
 * 各活动插件自行提供详情查询，调用方只消费 {@link Context#result}。
 *
 * <p>约定：宿主按 {@link #activityType()} 路由，同类型只生效一个实现；
 * 无对应实现时返回「不支持的活动类型」，不得静默返回空数据误导调用方。
 *
 * @since 1.0
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface ActivityDetailExtension extends ExtensionPoint<ActivityDetailExtension.Context> {

    /** 活动类型，如 zhekou（限时折扣）、miaosha（限时秒杀）、manjian（满减）。 */
    String activityType();

    /** 活动详情查询上下文。 */
    final class Context extends ExtensionContext {
        /** 店铺 ID。 */
        public Integer shopId;
        /** 活动类型，与 {@link #activityType()} 一致。 */
        public String activityType;
        /** 待查询的活动 ID 列表。 */
        public List<Integer> activityIds = new ArrayList<>();
        /** 每个活动一条：活动基础信息 + items（商品条目）。 */
        public List<Map<String, Object>> result = new ArrayList<>();
    }

    class Noop implements ActivityDetailExtension {
        @Override
        public String activityType() {
            return "";
        }

        @Override
        public void execute(Context ctx) {
            /* 默认不提供详情查询 */
        }
    }
}
