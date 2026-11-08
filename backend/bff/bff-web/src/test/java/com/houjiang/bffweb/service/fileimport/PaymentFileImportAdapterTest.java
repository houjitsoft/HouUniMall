package com.houjiang.bffweb.service.fileimport;

import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.service.FileImportAdapterResult;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentFileImportAdapterTest {

    @Test
    void supportsOnlyTheTwoControlledPaymentObjects() throws ReflectiveOperationException {
        FileImportBusinessAdapter adapter = adapter(mock(PaymentServiceFeign.class));

        assertEquals(Set.of("payment.merchantQual", "payment.riskBlacklist"), adapter.keys());
        assertEquals(422, adapter.importItem("payment.unknown", Map.of(), attachment()).code());
    }

    @Test
    void savesMerchantQualificationDisabledAndDropsEverySensitiveOrActivationField() throws ReflectiveOperationException {
        PaymentServiceFeign payment = mock(PaymentServiceFeign.class);
        when(payment.merchantQualImport(any())).thenReturn(ResponseResult.success(39L));
        FileImportBusinessAdapter adapter = adapter(payment);
        Map<String, Object> recognized = new LinkedHashMap<>();
        recognized.put("terminal", "WX_MINI");
        recognized.put("channel", "WECHAT");
        recognized.put("qualName", "商城小程序资质");
        recognized.put("env", "S");
        recognized.put("appId", "wx-public-id");
        recognized.put("mchId", "1900000109");
        recognized.put("notifyUrl", "https://example.com/payment/notify");
        recognized.put("returnUrl", "https://example.com/payment/return");
        recognized.put("remark", "等待管理员补充凭证并启用");
        recognized.put("status", 1);
        recognized.put("appSecret", "secret");
        recognized.put("apiKey", "key");
        recognized.put("apiPublicKey", "public-key");
        recognized.put("certs", Set.of(Map.of("certPath", "/secret/cert.pem")));
        recognized.put("extConfig", "{\"autoEnable\":true}");
        recognized.put("tenantId", 999L);
        recognized.put("orgId", 999L);

        FileImportAdapterResult result = adapter.importItem("payment.merchantQual", recognized, attachment());

        assertEquals(1, result.code());
        assertEquals("39", result.businessRecordId());
        assertEquals("/payment/merchant-qual", result.detailUrl());
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(payment).merchantQualImport(payload.capture());
        assertEquals(0, payload.getValue().get("status"));
        assertEquals(Set.of("terminal", "channel", "qualName", "env", "appId", "mchId",
                "notifyUrl", "returnUrl", "remark", "status"), payload.getValue().keySet());
        assertFalse(payload.getValue().containsKey("appSecret"));
        assertFalse(payload.getValue().containsKey("apiKey"));
        assertFalse(payload.getValue().containsKey("certs"));
        assertFalse(payload.getValue().containsKey("tenantId"));
        assertFalse(payload.getValue().containsKey("orgId"));
    }

    @Test
    void refusesRiskBlacklistBecauseTheDomainApiWouldImmediatelyActivateIt() throws ReflectiveOperationException {
        PaymentServiceFeign payment = mock(PaymentServiceFeign.class);
        FileImportBusinessAdapter adapter = adapter(payment);

        FileImportAdapterResult result = adapter.importItem("payment.riskBlacklist", Map.of(
                "listType", 1, "targetType", 5, "targetValue", "13800000000"), attachment());

        assertEquals(422, result.code());
        assertEquals("该对象需要人工在支付中心新增", result.message());
        verifyNoInteractions(payment);
    }

    @Test
    void rejectsUnknownMerchantEnumsBeforeCallingPayment() throws ReflectiveOperationException {
        for (Map<String, Object> fields : Set.of(
                Map.<String, Object>of("terminal", "微信小程序", "channel", "WECHAT", "qualName", "q", "env", "S"),
                Map.<String, Object>of("terminal", "WX_MINI", "channel", "微信支付", "qualName", "q", "env", "S"),
                Map.<String, Object>of("terminal", "WX_MINI", "channel", "WECHAT", "qualName", "q", "env", "TEST"))) {
            PaymentServiceFeign payment = mock(PaymentServiceFeign.class);
            FileImportAdapterResult result = adapter(payment).importItem("payment.merchantQual", fields, attachment());
            assertEquals(422, result.code());
            verifyNoInteractions(payment);
        }
    }

    private FileImportBusinessAdapter adapter(PaymentServiceFeign payment) throws ReflectiveOperationException {
        Class<?> type = Class.forName("com.houjiang.bffweb.service.fileimport.PaymentFileImportAdapter");
        return (FileImportBusinessAdapter) type.getConstructor(PaymentServiceFeign.class).newInstance(payment);
    }

    private Map<String, Object> attachment() {
        return Map.of("fileName", "source.pdf", "fileUrl", "https://files/source.pdf",
                "fileSize", 123L, "contentType", "application/pdf");
    }
}
