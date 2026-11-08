package com.houjiang.payment.channel.biz.impl;

import com.houjiang.payment.channel.biz.AbstractChannelPayStrategy;
import com.houjiang.payment.constant.PayMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * FME 职工金融服务支付 FME_PAY 渠道策略（W4 骨架 + W5 凭证加载）。
 * <p>
 * 真实 SDK：FME 厂商接口（待接入合作方 API），通常通过 RESTful + RSA 签名发起。
 * @author Amos
 */
@Slf4j
@Component
public class FmePayStrategy extends AbstractChannelPayStrategy {

    @Override
    public String getChannel() {
        return PayMethodEnum.FME_PAY.getCode();
    }

    @Override
    protected String[] requiredFields() {
        return new String[]{"partnerId", "appKey", "appSecret"};
    }

    @Override
    public Map<String, Object> createPay(Map<String, Object> req) {
        String mchNo = (String) req.get("mchNo");
        String outTradeNo = (String) req.get("outTradeNo");
        Object amount = req.get("amount");
        log.info("[FME_PAY/createPay] mchNo={} outTradeNo={} amount={}", mchNo, outTradeNo, amount);
        Map<String, Object> cfg = tryLoadCredential(mchNo);
        if (cfg == null) {
            return buildMock("FME_MOCK_", outTradeNo, null);
        }
        // TODO 真实接入：HttpClient.post(FME_GATEWAY + "/pay/create") + RSA2 签名
        log.info("[FME_PAY/createPay] credential ready, partnerId={}, configKeys={}", cfg.get("partnerId"), cfg.keySet());
        return buildMock("FME_PENDING_SDK_", outTradeNo, cfg);
    }

    @Override
    public Map<String, Object> queryStatus(String channelOrderNo) {
        log.info("[FME_PAY/queryStatus] channelOrderNo={}", channelOrderNo);
        // TODO 真实接入：HttpClient.post(FME_GATEWAY + "/pay/query")
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", 0);
        resp.put("paidTime", null);
        resp.put("raw", "MOCK_PENDING_REAL_SDK");
        return resp;
    }

    @Override
    public Map<String, Object> refund(Map<String, Object> req) {
        String channelOrderNo = (String) req.get("channelOrderNo");
        BigDecimal refundAmount = (BigDecimal) req.get("refundAmount");
        log.info("[FME_PAY/refund] channelOrderNo={} amount={}", channelOrderNo, refundAmount);
        // TODO 真实接入：HttpClient.post(FME_GATEWAY + "/pay/refund")
        Map<String, Object> resp = new HashMap<>();
        resp.put("refundOrderNo", "FME_REF_" + System.currentTimeMillis());
        resp.put("status", 0);
        resp.put("requestTime", new Date());
        resp.put("raw", "MOCK_PENDING_REAL_SDK");
        return resp;
    }

    private Map<String, Object> tryLoadCredential(String mchNo) {
        try {
            return loadCredential(mchNo);
        } catch (Exception e) {
            log.warn("[{}] {}", getChannel(), e.getMessage());
            return null;
        }
    }

    private Map<String, Object> buildMock(String prefix, String outTradeNo, Map<String, Object> cfgUsed) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("channelOrderNo", prefix + System.currentTimeMillis());
        resp.put("payUrl", "https://fme-gateway.mock/pay?outTradeNo=" + outTradeNo);
        resp.put("raw", cfgUsed == null ? "MOCK_NO_CREDENTIAL" : "MOCK_CREDENTIAL_READY");
        resp.put("configReady", cfgUsed != null);
        return resp;
    }
}
