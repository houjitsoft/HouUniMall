package com.houjiang.order.plugin;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.feign.ActivityServiceClient;
import com.houjiang.plugin.api.extension.order.PromotionCalcExtension;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 遗留适配器：保证切换到扩展点算价后，与原 {@code @FeignClient("activity")} 行为一致。
 */
class LegacyActivityPromotionCalcExtensionTest {

    private static final BigDecimal NORMAL = new BigDecimal("100.00");
    private static final BigDecimal ACTIVITY = new BigDecimal("80.00");
    private static final BigDecimal QUANTITY = new BigDecimal("2");

    private static PromotionCalcExtension.Context ctx(String promotionType) {
        PromotionCalcExtension.Context ctx = new PromotionCalcExtension.Context();
        ctx.promotionType = promotionType;
        ctx.baseAmount = NORMAL.multiply(QUANTITY);
        ctx.extra.put(LegacyActivityPromotionCalcExtension.EXTRA_TOKEN, "tk");
        ctx.extra.put(LegacyActivityPromotionCalcExtension.EXTRA_GOODS_ID, 1);
        ctx.extra.put(LegacyActivityPromotionCalcExtension.EXTRA_NORMAL_PRICE, NORMAL);
        ctx.extra.put(LegacyActivityPromotionCalcExtension.EXTRA_ACTIVITY_PRICE, ACTIVITY);
        ctx.extra.put(LegacyActivityPromotionCalcExtension.EXTRA_QUANTITY, QUANTITY);
        return ctx;
    }

    @SuppressWarnings("unchecked")
    private static ResponseResult<Object> result(int code) {
        return (ResponseResult<Object>) ResponseResult.success(code, "ok");
    }

    @Test
    void 秒杀命中活动价时按活动价计优惠() {
        ActivityServiceClient client = mock(ActivityServiceClient.class);
        when(client.seckillCheckLimit(any())).thenReturn(result(1));

        PromotionCalcExtension.Context ctx = ctx(LegacyActivityPromotionCalcExtension.SECKILL);
        new LegacyActivityPromotionCalcExtension(client).execute(ctx);

        assertEquals(0, new BigDecimal("40.00").compareTo(ctx.discountAmount),
                "(100-80)*2 = 40");
        assertEquals(ACTIVITY, ctx.promotionDetail.get(LegacyActivityPromotionCalcExtension.DETAIL_UNIT_PRICE));
        assertEquals(Boolean.TRUE, ctx.promotionDetail.get(LegacyActivityPromotionCalcExtension.DETAIL_HIT));
        assertFalse(ctx.isAborted());
    }

    @Test
    void 未命中活动时用原价且无优惠() {
        ActivityServiceClient client = mock(ActivityServiceClient.class);
        when(client.seckillCheckLimit(any())).thenReturn(result(2));

        PromotionCalcExtension.Context ctx = ctx(LegacyActivityPromotionCalcExtension.SECKILL);
        new LegacyActivityPromotionCalcExtension(client).execute(ctx);

        assertEquals(0, BigDecimal.ZERO.compareTo(ctx.discountAmount));
        assertEquals(NORMAL, ctx.promotionDetail.get(LegacyActivityPromotionCalcExtension.DETAIL_UNIT_PRICE));
        assertEquals(Boolean.FALSE, ctx.promotionDetail.get(LegacyActivityPromotionCalcExtension.DETAIL_HIT));
    }

    @Test
    void 限时折扣走折扣校验接口() {
        ActivityServiceClient client = mock(ActivityServiceClient.class);
        when(client.discountCheckLimit(any())).thenReturn(result(1));

        PromotionCalcExtension.Context ctx = ctx(LegacyActivityPromotionCalcExtension.DISCOUNT);
        ctx.extra.put(LegacyActivityPromotionCalcExtension.EXTRA_COMMON_ID, 9);
        new LegacyActivityPromotionCalcExtension(client).execute(ctx);

        verify(client).discountCheckLimit(any());
        verify(client, never()).seckillCheckLimit(any());
        assertEquals(0, new BigDecimal("40.00").compareTo(ctx.discountAmount));
    }

    @Test
    void 返回其他状态码时中断并提示重新登录() {
        ActivityServiceClient client = mock(ActivityServiceClient.class);
        when(client.seckillCheckLimit(any())).thenReturn(result(0));

        PromotionCalcExtension.Context ctx = ctx(LegacyActivityPromotionCalcExtension.SECKILL);
        new LegacyActivityPromotionCalcExtension(client).execute(ctx);

        assertTrue(ctx.isAborted());
        assertEquals("token失效，请重新登录", ctx.getAbortReason());
    }

    @Test
    void 促销服务异常时中断而不是静默按原价成交() {
        ActivityServiceClient client = mock(ActivityServiceClient.class);
        when(client.seckillCheckLimit(any())).thenThrow(new IllegalStateException("连接失败"));

        PromotionCalcExtension.Context ctx = ctx(LegacyActivityPromotionCalcExtension.SECKILL);
        new LegacyActivityPromotionCalcExtension(client).execute(ctx);

        assertTrue(ctx.isAborted(), "促销服务不可用必须中断，否则用户会静默按原价付款");
        assertEquals("促销服务暂时不可用，请稍后重试", ctx.getAbortReason());
    }

    @Test
    void 未迁移的活动类型不调用Feign也不改动结果() {
        ActivityServiceClient client = mock(ActivityServiceClient.class);

        PromotionCalcExtension.Context ctx = ctx("COUPON");
        new LegacyActivityPromotionCalcExtension(client).execute(ctx);

        verify(client, never()).seckillCheckLimit(any());
        verify(client, never()).discountCheckLimit(any());
        assertEquals(0, BigDecimal.ZERO.compareTo(ctx.discountAmount));
        assertNull(ctx.promotionDetail.get(LegacyActivityPromotionCalcExtension.DETAIL_UNIT_PRICE));
        assertFalse(ctx.isAborted());
    }

    @Test
    void 活动价低于原价时优惠不会为负() {
        ActivityServiceClient client = mock(ActivityServiceClient.class);
        when(client.seckillCheckLimit(any())).thenReturn(result(1));

        PromotionCalcExtension.Context ctx = ctx(LegacyActivityPromotionCalcExtension.SECKILL);
        ctx.extra.put(LegacyActivityPromotionCalcExtension.EXTRA_ACTIVITY_PRICE, new BigDecimal("120.00"));
        new LegacyActivityPromotionCalcExtension(client).execute(ctx);

        assertEquals(0, BigDecimal.ZERO.compareTo(ctx.discountAmount));
        assertEquals(new BigDecimal("120.00"),
                ctx.promotionDetail.get(LegacyActivityPromotionCalcExtension.DETAIL_UNIT_PRICE));
    }
}
