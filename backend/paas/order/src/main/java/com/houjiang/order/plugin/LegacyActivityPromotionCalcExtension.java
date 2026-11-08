package com.houjiang.order.plugin;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.feign.ActivityServiceClient;
import com.houjiang.plugin.api.extension.order.PromotionCalcExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 遗留促销适配器：把现有 {@code @FeignClient("activity")} 调用包装成
 * {@link PromotionCalcExtension}，使 order 可以先切到扩展点算价而<b>行为完全不变</b>。
 *
 * <p><b>为什么需要它</b>：activity 下每个活动都会成为独立插件，届时不存在可寻址的单一
 * activity 服务。但当前<b>尚无任何活动插件实现该扩展点</b>——若直接切换，所有促销优惠会
 * 归零、用户按原价付款。本适配器作为过渡实现接管现有 Feign 调用，等活动逐个插件化后
 * 由对应插件的实现替换，届时删除本类。
 *
 * <p><b>故障语义（关键）</b>：Feign 调用失败时必须 {@code abort}，不能静默当作「无优惠」。
 * 因为 {@code ExtensionInvoker} 会吞掉插件异常，若此处放任异常，结果就是促销服务挂掉时
 * 用户<b>静默按原价付款</b>——比下单失败更糟，且难以发现。中断即等价于原来的下单报错。
 *
 * <p>命中活动价时同时写入 {@code promotionDetail.unitPrice}：订单侧需要的是<b>单价</b>
 * 而非优惠额，直接由优惠额反除会引入精度误差，故由适配器给出精确单价。
 *
 * @author Amos
 */
public class LegacyActivityPromotionCalcExtension implements PromotionCalcExtension {

    private static final Logger log = LoggerFactory.getLogger(LegacyActivityPromotionCalcExtension.class);

    /** 促销类型：秒杀。 */
    public static final String SECKILL = "SECKILL";
    /** 促销类型：限时折扣。 */
    public static final String DISCOUNT = "DISCOUNT";

    /** 透传参数：登录 token。 */
    public static final String EXTRA_TOKEN = "token";
    /** 透传参数：商品 ID。 */
    public static final String EXTRA_GOODS_ID = "goodsId";
    /** 透传参数：common ID（限时折扣校验需要）。 */
    public static final String EXTRA_COMMON_ID = "commonId";
    /** 透传参数：官方原价单价。 */
    public static final String EXTRA_NORMAL_PRICE = "normalPrice";
    /** 透传参数：活动价单价。 */
    public static final String EXTRA_ACTIVITY_PRICE = "activityPrice";
    /** 透传参数：购买数量。 */
    public static final String EXTRA_QUANTITY = "quantity";

    /** 结果明细：是否命中活动。 */
    public static final String DETAIL_HIT = "hit";
    /** 结果明细：应当采用的单价。 */
    public static final String DETAIL_UNIT_PRICE = "unitPrice";

    /** activity 返回码：命中活动价。 */
    private static final int CODE_HIT = 1;
    /** activity 返回码：未命中，用原价。 */
    private static final int CODE_MISS = 2;

    private final ActivityServiceClient activityServiceClient;

    public LegacyActivityPromotionCalcExtension(ActivityServiceClient activityServiceClient) {
        this.activityServiceClient = activityServiceClient;
    }

    @Override
    public void execute(Context ctx) {
        if (ctx == null) {
            return;
        }
        boolean seckill = SECKILL.equalsIgnoreCase(ctx.promotionType);
        boolean discount = DISCOUNT.equalsIgnoreCase(ctx.promotionType);
        if (!seckill && !discount) {
            return; // 满减/优惠券等活动尚未迁移，交由原有逻辑处理
        }

        Map<String, Object> param = new HashMap<>();
        param.put(EXTRA_TOKEN, ctx.extra.get(EXTRA_TOKEN));
        param.put(EXTRA_GOODS_ID, ctx.extra.get(EXTRA_GOODS_ID));
        if (discount) {
            param.put(EXTRA_COMMON_ID, ctx.extra.get(EXTRA_COMMON_ID));
        }

        int code;
        try {
            ResponseResult resp = seckill
                    ? activityServiceClient.seckillCheckLimit(param)
                    : activityServiceClient.discountCheckLimit(param);
            code = resp == null ? -1 : resp.getCode();
        } catch (Exception e) {
            // 促销服务不可用：中断下单，绝不静默按原价成交
            log.error("[order] 促销服务调用失败，中断下单: promotionType={}", ctx.promotionType, e);
            ctx.abort("促销服务暂时不可用，请稍后重试");
            return;
        }

        BigDecimal normalPrice = toBigDecimal(ctx.extra.get(EXTRA_NORMAL_PRICE));
        BigDecimal activityPrice = toBigDecimal(ctx.extra.get(EXTRA_ACTIVITY_PRICE));
        BigDecimal quantity = toBigDecimal(ctx.extra.get(EXTRA_QUANTITY));

        if (code == CODE_HIT && activityPrice != null) {
            ctx.discountAmount = normalPrice.subtract(activityPrice)
                    .max(BigDecimal.ZERO)
                    .multiply(quantity);
            ctx.promotionDetail.put(DETAIL_HIT, Boolean.TRUE);
            ctx.promotionDetail.put(DETAIL_UNIT_PRICE, activityPrice);
        } else if (code == CODE_MISS) {
            ctx.discountAmount = BigDecimal.ZERO;
            ctx.promotionDetail.put(DETAIL_HIT, Boolean.FALSE);
            ctx.promotionDetail.put(DETAIL_UNIT_PRICE, normalPrice);
        } else if (code == CODE_HIT) {
            // 命中活动价但缺少活动价参数：按原价成交并告警，避免算出错误金额
            log.warn("[order] 命中活动价但缺少活动价参数，按原价成交: promotionType={}", ctx.promotionType);
            ctx.discountAmount = BigDecimal.ZERO;
            ctx.promotionDetail.put(DETAIL_HIT, Boolean.FALSE);
            ctx.promotionDetail.put(DETAIL_UNIT_PRICE, normalPrice);
        } else {
            ctx.abort("token失效，请重新登录");
        }
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
