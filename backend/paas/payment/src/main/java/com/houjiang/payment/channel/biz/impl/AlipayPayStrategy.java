package com.houjiang.payment.channel.biz.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.houjiang.payment.channel.biz.AbstractChannelPayStrategy;
import com.houjiang.payment.channel.client.AlipayClientFactory;
import com.houjiang.payment.constant.PayMethodEnum;
import com.houjiang.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Component
public class AlipayPayStrategy extends AbstractChannelPayStrategy {

    private final AlipayClientFactory clientFactory;

    @Override
    public String getChannel() {
        return PayMethodEnum.ALIPAY.getCode();
    }

    @Override
    protected String[] requiredFields() {
        return new String[]{"appId", "privateKey", "alipayPublicKey"};
    }

    @Override
    public Map<String, Object> createPay(Map<String, Object> request) {
        String outTradeNo = requiredString(request, "outTradeNo");
        BigDecimal amount = requiredAmount(request, "amount");
        String subject = requiredString(request, "subject");
        String terminal = requiredString(request, "terminal").toUpperCase();
        Map<String, Object> config = loadCredential(stringValue(request.get("mchNo")));
        AlipayClient client = clientFactory.create(config);
        try {
            return switch (terminal) {
                case "PC" -> pagePay(client, request, outTradeNo, amount, subject);
                case "WAP", "H5" -> wapPay(client, request, outTradeNo, amount, subject);
                case "APP" -> appPay(client, request, outTradeNo, amount, subject);
                default -> throw PaymentException.bizError("Unsupported Alipay terminal: " + terminal);
            };
        } catch (AlipayApiException e) {
            throw PaymentException.bizError("Alipay create payment failed: " + e.getErrMsg());
        }
    }

    @Override
    public Map<String, Object> queryStatus(String channelOrderNo) {
        Map<String, Object> config = loadCredential(null);
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(channelOrderNo);
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizModel(model);
        try {
            AlipayTradeQueryResponse response = clientFactory.create(config).execute(request);
            requireSuccess(response.isSuccess(), response.getSubCode(), response.getSubMsg());
            Map<String, Object> result = new HashMap<>();
            result.put("status", mapTradeStatus(response.getTradeStatus()));
            result.put("channelOrderNo", response.getTradeNo());
            result.put("paidTime", response.getSendPayDate());
            result.put("tradeStatus", response.getTradeStatus());
            return result;
        } catch (AlipayApiException e) {
            throw PaymentException.bizError("Alipay query failed: " + e.getErrMsg());
        }
    }

    @Override
    public Map<String, Object> refund(Map<String, Object> requestData) {
        String outTradeNo = requiredString(requestData, "channelOrderNo");
        String refundNo = requiredString(requestData, "refundNo");
        BigDecimal amount = requiredAmount(requestData, "refundAmount");
        Map<String, Object> config = loadCredential(stringValue(requestData.get("mchNo")));

        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(outTradeNo);
        model.setOutRequestNo(refundNo);
        model.setRefundAmount(amount.toPlainString());
        model.setRefundReason(stringValue(requestData.get("refundReason")));
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);
        try {
            AlipayTradeRefundResponse response = clientFactory.create(config).execute(request);
            requireSuccess(response.isSuccess(), response.getSubCode(), response.getSubMsg());
            Map<String, Object> result = new HashMap<>();
            result.put("refundOrderNo", refundNo);
            result.put("channelOrderNo", response.getTradeNo());
            result.put("status", "Y".equalsIgnoreCase(response.getFundChange()) ? 1 : 0);
            result.put("fundChange", response.getFundChange());
            result.put("successTime", response.getGmtRefundPay());
            return result;
        } catch (AlipayApiException e) {
            throw PaymentException.bizError("Alipay refund failed: " + e.getErrMsg());
        }
    }

    private Map<String, Object> pagePay(AlipayClient client, Map<String, Object> data,
                                        String outTradeNo, BigDecimal amount, String subject) throws AlipayApiException {
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        fillPageModel(model, outTradeNo, amount, subject, stringValue(data.get("body")));
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(stringValue(data.get("notifyUrl")));
        request.setReturnUrl(stringValue(data.get("returnUrl")));
        AlipayTradePagePayResponse response = client.pageExecute(request);
        return createPayload("ALIPAY_PAGE", "formHtml", response.getBody(), outTradeNo);
    }

    private Map<String, Object> wapPay(AlipayClient client, Map<String, Object> data,
                                       String outTradeNo, BigDecimal amount, String subject) throws AlipayApiException {
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(amount.toPlainString());
        model.setSubject(subject);
        model.setBody(stringValue(data.get("body")));
        model.setProductCode("QUICK_WAP_WAY");
        model.setQuitUrl(stringValue(data.get("returnUrl")));
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(stringValue(data.get("notifyUrl")));
        request.setReturnUrl(stringValue(data.get("returnUrl")));
        AlipayTradeWapPayResponse response = client.pageExecute(request);
        return createPayload("ALIPAY_WAP", "formHtml", response.getBody(), outTradeNo);
    }

    private Map<String, Object> appPay(AlipayClient client, Map<String, Object> data,
                                       String outTradeNo, BigDecimal amount, String subject) throws AlipayApiException {
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(amount.toPlainString());
        model.setSubject(subject);
        model.setBody(stringValue(data.get("body")));
        model.setProductCode("QUICK_MSECURITY_PAY");
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(stringValue(data.get("notifyUrl")));
        AlipayTradeAppPayResponse response = client.sdkExecute(request);
        return createPayload("ALIPAY_APP", "orderString", response.getBody(), outTradeNo);
    }

    private void fillPageModel(AlipayTradePagePayModel model, String outTradeNo,
                               BigDecimal amount, String subject, String body) {
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(amount.toPlainString());
        model.setSubject(subject);
        model.setBody(body);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
    }

    private Map<String, Object> createPayload(String channel, String payloadKey,
                                               String payload, String outTradeNo) {
        if (payload == null || payload.isBlank()) {
            throw PaymentException.bizError("Alipay returned an empty payment payload");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("channel", channel);
        result.put(payloadKey, payload);
        result.put("channelOrderNo", outTradeNo);
        result.put("status", 0);
        return result;
    }

    private int mapTradeStatus(String tradeStatus) {
        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) return 1;
        if ("TRADE_CLOSED".equals(tradeStatus)) return 2;
        return 0;
    }

    private void requireSuccess(boolean success, String code, String message) {
        if (!success) throw PaymentException.bizError("Alipay rejected request: " + code + " " + message);
    }

    private String requiredString(Map<String, Object> data, String key) {
        String value = stringValue(data.get(key));
        if (value == null || value.isBlank()) throw PaymentException.bizError(key + " is required");
        return value;
    }

    private BigDecimal requiredAmount(Map<String, Object> data, String key) {
        Object value = data.get(key);
        BigDecimal amount = value instanceof BigDecimal decimal ? decimal :
                value == null ? null : new BigDecimal(String.valueOf(value));
        if (amount == null || amount.signum() <= 0) throw PaymentException.bizError(key + " must be positive");
        return amount;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
