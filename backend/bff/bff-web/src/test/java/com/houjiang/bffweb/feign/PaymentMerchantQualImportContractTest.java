package com.houjiang.bffweb.feign;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class PaymentMerchantQualImportContractTest {

    @Test
    void intelligentImportUsesDedicatedPaymentDomainEndpoint() throws Exception {
        Method method = PaymentServiceFeign.class.getMethod("merchantQualImport", Map.class);
        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertArrayEquals(new String[]{"/api/payment/admin/merchantQual/import"}, mapping.value());
    }
}
