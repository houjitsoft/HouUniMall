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
 * 云闪付 YSF_PAY 渠道策略（W4 骨架 + W5 凭证加载）。
 * <p>
 * 真实 SDK：cn.unionpay.acp（中国银联开放平台 / 云闪付开放 API），
 * 接入文档 https://open.unionpay.com / https://open.95516.com。
 * <p>
 * 凭证到位后只需提供 merchId / signKey，在 createPay 里以 cfg 走真实 SDK。
 * @author Amos
 */
@Slf4j
@Component
public class YsfPayStrategy extends AbstractChannelPayStrategy {

    @Override
    public String getChannel() {
        return PayMethodEnum.YSF_PAY.getCode();
    }

    @Override
    protected String[] requiredFields() {
        return new String[]{"merchId", "signKey"};
    }

    @Override
    public Map<String, Object> createPay(Map<String, Object> req) {
        String mchNo = (String) req.get("mchNo");
        String outTradeNo = (String) req.get("outTradeNo");
        Object amount = req.get("amount");
        log.info("[YSF_PAY/createPay] mchNo={} outTradeNo={} amount={}", mchNo, outTradeNo, amount);
        Map<String, Object> cfg = tryLoadCredential(mchNo);
        if (cfg == null) {
            // 凭证未到位，返回 mock 以保证上游流程不受阻
            return buildMock("YSF_MOCK_", outTradeNo, "https://gateway.95516.com/gateway/mock?outTradeNo=" + outTradeNo, null);
        }
        // TODO 真实接入：AcpService.sign + post 至 https://gateway.95516.com/gateway/api/frontTransReq.do
        log.info("[YSF_PAY/createPay] credential ready, merchId={}, configKeys={}", cfg.get("merchId"), cfg.keySet());
        return buildMock("YSF_PENDING_SDK_", outTradeNo, "https://gateway.95516.com/gateway/mock?outTradeNo=" + outTradeNo, cfg);
    }

    @Override
    public Map<String, Object> queryStatus(String channelOrderNo) {
        log.info("[YSF_PAY/queryStatus] channelOrderNo={}", channelOrderNo);
        // TODO 真实接入：AcpService.signQueryRequest -> /gateway/api/queryTrans.do
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
        log.info("[YSF_PAY/refund] channelOrderNo={} amount={}", channelOrderNo, refundAmount);
        // TODO 真实接入：/gateway/api/backTransReq.do
        Map<String, Object> resp = new HashMap<>();
        resp.put("refundOrderNo", "YSF_REF_" + System.currentTimeMillis());
        resp.put("status", 0);
        resp.put("requestTime", new Date());
        resp.put("raw", "MOCK_PENDING_REAL_SDK");
        return resp;
    }

    /**
     * 带容错的凭证加载：未配置则返回 null（不阻断业务）
     */
    private Map<String, Object> tryLoadCredential(String mchNo) {
        try {
            return loadCredential(mchNo);
        } catch (Exception e) {
            log.warn("[{}] {}", getChannel(), e.getMessage());
            return null;
        }
    }

    private Map<String, Object> buildMock(String prefix, String outTradeNo, String payUrl, Map<String, Object> cfgUsed) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("channelOrderNo", prefix + System.currentTimeMillis());
        resp.put("payUrl", payUrl);
        resp.put("raw", cfgUsed == null ? "MOCK_NO_CREDENTIAL" : "MOCK_CREDENTIAL_READY");
        resp.put("configReady", cfgUsed != null);
        return resp;
    }
}
