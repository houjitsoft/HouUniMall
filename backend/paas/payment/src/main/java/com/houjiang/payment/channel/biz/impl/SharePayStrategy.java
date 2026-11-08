package com.houjiang.payment.channel.biz.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.payment.channel.biz.ChannelPayStrategy;
import com.houjiang.payment.constant.PayMethodEnum;
import com.houjiang.payment.entity.PayDivisionReceiverEntity;
import com.houjiang.payment.mapper.PayDivisionReceiverMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分账支付 SHARE_PAY 渠道策略（W4 P0 任务 T8 骨架）。
 * <p>
 * 与外部支付渠道不同，SHARE_PAY 不直连厂商 SDK，而是基于已收款的主单
 * 联动 {@code hou_pay_division_receiver} 按万分比拆分到各接收方。
 * <p>
 * 骨架阶段：createPay 计算拆分明细 + 落库流水 mock；
 * TODO 真实接入：与微信/支付宝分账接口（profitsharing/v3）联动。
 *
 * @author Amos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SharePayStrategy implements ChannelPayStrategy {

    private final PayDivisionReceiverMapper receiverMapper;

    @Override
    public String getChannel() {
        return PayMethodEnum.SHARE_PAY.getCode();
    }

    @Override
    public Map<String, Object> createPay(Map<String, Object> req) {
        String mchNo = (String) req.get("mchNo");
        String outTradeNo = (String) req.get("outTradeNo");
        BigDecimal amount = (BigDecimal) req.get("amount");
        log.info("[SHARE_PAY/createPay] mchNo={} outTradeNo={} amount={}", mchNo, outTradeNo, amount);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", 2);
            err.put("errMsg", "金额非法");
            return err;
        }

        // 拉取该商户启用的分账接收方（status=1）按 priority asc
        List<PayDivisionReceiverEntity> list = receiverMapper.selectList(
                new LambdaQueryWrapper<PayDivisionReceiverEntity>()
                        .eq(PayDivisionReceiverEntity::getMchNo, mchNo)
                        .eq(PayDivisionReceiverEntity::getStatus, 1)
                        .orderByAsc(PayDivisionReceiverEntity::getPriority));

        List<Map<String, Object>> details = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO;
        for (int i = 0; i < list.size(); i++) {
            PayDivisionReceiverEntity r = list.get(i);
            BigDecimal share;
            if (i == list.size() - 1) {
                // 最后一位接收方拿余数避免万分比丢分
                share = amount.subtract(allocated);
            } else {
                BigDecimal ratio = r.getShareRatio() == null ? BigDecimal.ZERO : new BigDecimal(r.getShareRatio());
                share = amount.multiply(ratio).divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);
                allocated = allocated.add(share);
            }
            Map<String, Object> item = new HashMap<>();
            item.put("receiverNo", r.getReceiverNo());
            item.put("receiverName", r.getReceiverName());
            item.put("ratio", r.getShareRatio());
            item.put("amount", share);
            details.add(item);
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("channelOrderNo", "SHARE_MOCK_" + System.currentTimeMillis());
        resp.put("totalAmount", amount);
        resp.put("receiverCount", list.size());
        resp.put("details", details);
        // TODO 真实接入：调用微信 v3 profitsharing/orders 或 支付宝 alipay.trade.order.settle
        resp.put("raw", "MOCK_PENDING_REAL_SDK");
        return resp;
    }

    @Override
    public Map<String, Object> queryStatus(String channelOrderNo) {
        log.info("[SHARE_PAY/queryStatus] channelOrderNo={}", channelOrderNo);
        // TODO 真实接入：profitsharing/orders/{out_order_no}
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", 0);
        resp.put("raw", "MOCK_PENDING_REAL_SDK");
        return resp;
    }

    @Override
    public Map<String, Object> refund(Map<String, Object> req) {
        String channelOrderNo = (String) req.get("channelOrderNo");
        BigDecimal refundAmount = (BigDecimal) req.get("refundAmount");
        log.info("[SHARE_PAY/refund] channelOrderNo={} amount={}", channelOrderNo, refundAmount);
        // TODO 真实接入：profitsharing/return-orders（分账回退）
        Map<String, Object> resp = new HashMap<>();
        resp.put("refundOrderNo", "SHARE_REF_" + System.currentTimeMillis());
        resp.put("status", 0);
        resp.put("requestTime", new Date());
        resp.put("raw", "MOCK_PENDING_REAL_SDK");
        return resp;
    }
}
