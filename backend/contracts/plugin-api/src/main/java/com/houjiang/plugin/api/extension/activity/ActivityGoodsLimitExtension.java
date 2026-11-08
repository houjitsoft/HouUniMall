package com.houjiang.plugin.api.extension.activity;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.ExtensionStability;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;

import java.math.BigDecimal;

/**
 * 活动商品限购校验扩展点。
 *
 * <p>下单/加购前的「活动商品限购」检查，由各活动插件自行提供查询与判定：
 * 限时折扣、限时秒杀、拼团限购等各自只暴露结果，不向调用方泄漏自己的数据层。
 *
 * <p>此前各活动的限购校验集中在 goods 侧一个 service 里，直接注入各活动的 Mapper
 * （{@code ActivityDiscountGoodsMapper}、{@code ActivitySeckillGoodsMapper} …），
 * 造成 goods 宿主反向依赖多个活动插件的数据层；插件化后插件间不允许互相 import，
 * 且多个插件在同一进程争同一批表难以演进。本扩展点把这段查询能力下放给各活动插件。
 *
 * <p>约定：宿主 goods 侧按 {@link #kind()} 路由，同kind 只生效一个实现；
 * 无对应实现时宿主按「该活动无限购」放行，不得因缺少实现而中断下单。
 *
 * @since 1.0
 */
@ExtensionStability(ExtensionStability.Stability.EXPERIMENTAL)
public interface ActivityGoodsLimitExtension extends ExtensionPoint<ActivityGoodsLimitExtension.Context> {

    /** 活动种类，如 seckill / discount / pintuan。 */
    String kind();

    /** 单个商品的限购判定结果。 */
    final class Context extends ExtensionContext {
        /** 商品 ID。 */
        public Integer commonId;
        /** 商品名，用于错误提示。 */
        public String goodsName;
        /** 该活动的购买上限，0 或负数表示不限。 */
        public Integer upperLimit = 0;
        /** 限制时间窗（天），0 表示不限。 */
        public Integer upperTime = 0;
        /** 用户在该活动下已下单的件数，由宿主查询后写入。 */
        public Integer boughtCount = 0;
        /** 本次请求要购买的件数。 */
        public Integer requestCount = 0;
        /** 活动价，可空。 */
        public BigDecimal activityPrice;
        /** 商品是否参与该活动；false 时宿主跳过该 kind。 */
        public boolean involved = false;
        /** 命中限购时的提示文案；为空表示未超限。 */
        public String exceededMessage;
    }

    class Noop implements ActivityGoodsLimitExtension {
        @Override
        public String kind() {
            return "";
        }

        @Override
        public void execute(Context ctx) {
            /* 默认不参与任何限购校验 */
        }
    }
}
