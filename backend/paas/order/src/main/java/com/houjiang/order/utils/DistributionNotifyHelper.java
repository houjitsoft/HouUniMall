package com.houjiang.order.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.entity.OrderGoodsEntity;
import com.houjiang.order.feign.MallDistributionClient;
import com.houjiang.order.mapper.OrderGoodsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单侧 → 商城分销内部事件通知辅助。
 *
 * <p>旁路通知：异常一律吞掉只记日志，不影响主交易链路。
 */
public final class DistributionNotifyHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DistributionNotifyHelper.class);

    private DistributionNotifyHelper() {
    }

    public static List<Map<String, Object>> buildGoodsLines(OrderGoodsMapper orderGoodsMapper, String orderId) {
        List<Map<String, Object>> lines = new ArrayList<>();
        if (orderGoodsMapper == null || orderId == null) {
            return lines;
        }
        try {
            List<OrderGoodsEntity> goodsList = orderGoodsMapper.selectList(new LambdaQueryWrapper<OrderGoodsEntity>()
                    .eq(OrderGoodsEntity::getOrderId, orderId));
            if (goodsList == null) {
                return lines;
            }
            for (OrderGoodsEntity g : goodsList) {
                if (g == null) {
                    continue;
                }
                BigDecimal amount = g.getOrderGoodsPaymentAmount();
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Map<String, Object> line = new HashMap<>(8);
                line.put("goodsId", g.getGoodsId());
                line.put("categoryId", g.getGoodsClassId());
                line.put("goodsName", g.getGoodsName());
                line.put("amount", amount);
                lines.add(line);
            }
        } catch (Exception e) {
            LOG.warn("构建分销商品行失败: orderId={}, err={}", orderId, e.getMessage());
        }
        return lines;
    }

    public static void notifyCreated(MallDistributionClient client, String token,
                                     OrderGoodsMapper orderGoodsMapper, OrderBaseEntity order) {
        if (client == null || order == null || order.getOrderId() == null) {
            return;
        }
        try {
            Map<String, Object> body = baseBody(order);
            body.put("goodsLines", buildGoodsLines(orderGoodsMapper, order.getOrderId()));
            Map<String, Object> res = client.orderCreate(body, token == null ? "" : token);
            LOG.info("分销订单登记通知: orderId={}, result={}", order.getOrderId(), res);
        } catch (Exception e) {
            LOG.error("分销订单登记通知失败（不影响下单）: orderId={}, err={}",
                    order.getOrderId(), e.getMessage());
        }
    }

    public static void notifyFinished(MallDistributionClient client, String token,
                                      OrderGoodsMapper orderGoodsMapper, OrderBaseEntity order) {
        if (client == null || order == null || order.getOrderId() == null) {
            return;
        }
        try {
            Map<String, Object> body = baseBody(order);
            body.put("goodsLines", buildGoodsLines(orderGoodsMapper, order.getOrderId()));
            Map<String, Object> res = client.orderFinish(body, token == null ? "" : token);
            LOG.info("分销订单完成通知: orderId={}, result={}", order.getOrderId(), res);
        } catch (Exception e) {
            LOG.error("分销订单完成通知失败（不影响收货）: orderId={}, err={}",
                    order.getOrderId(), e.getMessage());
        }
    }

    /**
     * @param refundAmount   本次退款金额；整单取消可传订单实付或 null + fullInvalidate
     * @param fullInvalidate 强制整单作废（取消未付款订单等）
     */
    public static void notifyCancel(MallDistributionClient client, String token,
                                    Object orderId, BigDecimal refundAmount, boolean fullInvalidate) {
        if (client == null || orderId == null) {
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>(8);
            body.put("orderId", orderId);
            if (refundAmount != null) {
                body.put("refundAmount", refundAmount);
            }
            if (fullInvalidate) {
                body.put("fullInvalidate", true);
            }
            Map<String, Object> res = client.orderCancel(body, token == null ? "" : token);
            LOG.info("分销订单退款/取消通知: orderId={}, refundAmount={}, full={}, result={}",
                    orderId, refundAmount, fullInvalidate, res);
        } catch (Exception e) {
            LOG.error("分销订单退款/取消通知失败（不影响主流程）: orderId={}, err={}",
                    orderId, e.getMessage());
        }
    }

    private static Map<String, Object> baseBody(OrderBaseEntity order) {
        Map<String, Object> body = new HashMap<>(8);
        body.put("orderId", order.getOrderId());
        body.put("orderNo", order.getOrderId());
        body.put("shopId", order.getShopId());
        body.put("buyerId", order.getBuyerUserId());
        body.put("shareUserId", order.getVkShareUserId());
        body.put("orderAmount", order.getOrderPaymentAmount());
        return body;
    }
}
