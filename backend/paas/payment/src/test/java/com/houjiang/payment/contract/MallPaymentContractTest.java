package com.houjiang.payment.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.dto.payment.MallCashierCreateRequest;
import com.houjiang.commons.dto.payment.MallCashierCreateResponse;
import com.houjiang.commons.dto.payment.MallPaymentNotifyRequest;
import com.houjiang.commons.dto.payment.MallRefundNotifyRequest;
import com.houjiang.commons.dto.payment.MallRefundRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MallPaymentContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createRequestKeepsMallOrderAndIdempotencyFields() throws Exception {
        MallCashierCreateRequest request = new MallCashierCreateRequest();
        request.setMallOrderNo("MO202607100001");
        request.setRequestNo("PAY-MO202607100001-1");
        request.setAmount(new BigDecimal("99.00"));

        String json = objectMapper.writeValueAsString(request);

        assertTrue(json.contains("mallOrderNo"));
        assertTrue(json.contains("requestNo"));
        assertTrue(json.contains("99.00"));
        assertEquals("CNY", request.getCurrency());
    }

    @Test
    void responseAndNotificationsKeepCrossServiceIdentifiers() throws Exception {
        MallCashierCreateResponse response = new MallCashierCreateResponse();
        response.setCashierNo("CS202607100001");
        response.setMallOrderNo("MO202607100001");
        response.setRequestNo("PAY-MO202607100001-1");
        response.setPayData(Map.of("codeUrl", "weixin://pay/mock"));

        MallPaymentNotifyRequest paymentNotify = new MallPaymentNotifyRequest();
        paymentNotify.setCashierNo(response.getCashierNo());
        paymentNotify.setMallOrderNo(response.getMallOrderNo());
        paymentNotify.setRequestNo(response.getRequestNo());

        MallRefundRequest refund = new MallRefundRequest();
        refund.setRefundNo("RF202607100001");
        refund.setCashierNo(response.getCashierNo());
        refund.setMallOrderNo(response.getMallOrderNo());

        MallRefundNotifyRequest refundNotify = new MallRefundNotifyRequest();
        refundNotify.setRefundNo(refund.getRefundNo());
        refundNotify.setCashierNo(refund.getCashierNo());
        refundNotify.setMallOrderNo(refund.getMallOrderNo());

        String json = objectMapper.writeValueAsString(
                Map.of("cashier", response, "payment", paymentNotify,
                        "refund", refund, "refundNotify", refundNotify));

        assertTrue(json.contains("CS202607100001"));
        assertTrue(json.contains("MO202607100001"));
        assertTrue(json.contains("PAY-MO202607100001-1"));
        assertTrue(json.contains("RF202607100001"));
        assertTrue(json.contains("codeUrl"));
    }
}
