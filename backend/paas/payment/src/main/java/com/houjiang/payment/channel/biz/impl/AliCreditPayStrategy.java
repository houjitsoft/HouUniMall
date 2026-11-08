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
 * 支付宝花呗信用支付 ALI_CREDIT 渠道策略（W4 骨架 + W5 凭证加载）。
 * <p>
 * 真实 SDK：com.alipay.sdk-java AlipayTradeCreatePayModel + tradePayMethod = creditPay，
 * 文档 https://opendocs.alipay.com/open/00axni。
 * @author Amos
 */
@Slf4j
@Component
public class AliCreditPayStrategy extends AbstractChannelPayStrategy {

    @Override
    public String getChannel() {
        return PayMethodEnum.ALI_CREDIT.getCode();
    }

    @Override
    protected String[] requiredFields() {
        return new String[]{"appId", "privateKey", "alipayPublicKey"};
    }

    @Override
    public Map<String, Object> createPay(Map<String, Object> req) {
        String mchNo = (String) req.get("mchNo");
        String outTradeNo = (String) req.get("outTradeNo");
        Object amount = req.get("amount");
        log.info("[ALI_CREDIT/createPay] mchNo={} outTradeNo={} amount={}", mchNo, outTradeNo, amount);
        Map<String, Object> cfg = tryLoadCredential(mchNo);
        if (cfg == null) {
            return buildMock("ALI_HUABEI_MOCK_", outTradeNo, null);
        }
        // TODO 真实接入：DefaultAlipayClient.execute(AlipayTradeCreateRequest) 走花呗 productCode=CREDIT_PAY
        log.info("[ALI_CREDIT/createPay] credential ready, appId={}, configKeys={}", cfg.get("appId"), cfg.keySet());
        return buildMock("ALI_HUABEI_PENDING_SDK_", outTradeNo, cfg);
    }

    @Override
    public Map<String, Object> queryStatus(String channelOrderNo) {
        log.info("[ALI_CREDIT/queryStatus] channelOrderNo={}", channelOrderNo);
        // TODO 真实接入：alipay.trade.query
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
        log.info("[ALI_CREDIT/refund] channelOrderNo={} amount={}", channelOrderNo, refundAmount);
        // TODO 真实接入：alipay.trade.refund
        Map<String, Object> resp = new HashMap<>();
        resp.put("refundOrderNo", "ALI_HUABEI_REF_" + System.currentTimeMillis());
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
        resp.put("prepayInfo", "alipays://platformapi/startapp?mock=" + outTradeNo);
        resp.put("raw", cfgUsed == null ? "MOCK_NO_CREDENTIAL" : "MOCK_CREDENTIAL_READY");
        resp.put("configReady", cfgUsed != null);
        return resp;
    }
}
