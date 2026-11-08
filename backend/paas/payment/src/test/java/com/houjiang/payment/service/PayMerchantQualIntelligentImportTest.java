package com.houjiang.payment.service;

import com.houjiang.payment.dto.merchantQual.MerchantQualSaveRequest;
import com.houjiang.payment.entity.PayMerchantQualEntity;
import com.houjiang.payment.mapper.PayMerchantQualCertMapper;
import com.houjiang.payment.mapper.PayMerchantQualMapper;
import com.houjiang.payment.service.impl.PayMerchantQualServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class PayMerchantQualIntelligentImportTest {

    @Test
    void trustedTenantOverridesAllClientControlledSecurityFieldsAndReturnsInsertedId() {
        PayMerchantQualMapper mapper = mock(PayMerchantQualMapper.class);
        doAnswer(invocation -> {
            PayMerchantQualEntity entity = invocation.getArgument(0);
            entity.setId(73L);
            return 1;
        }).when(mapper).insert(org.mockito.ArgumentMatchers.any(PayMerchantQualEntity.class));
        PayMerchantQualService service =
                new PayMerchantQualServiceImpl(mapper, mock(PayMerchantQualCertMapper.class));
        MerchantQualSaveRequest client = new MerchantQualSaveRequest();
        client.setId(88L);
        client.setTerminal("WX_MINI");
        client.setChannel("WECHAT");
        client.setQualName("智能入库资质");
        client.setEnv("S");
        client.setStatus(1);
        client.setAppSecret("client-secret");
        client.setApiKey("client-key");

        Long id = service.importDisabled(client, 2L, "account-b");

        assertEquals(73L, id);
        ArgumentCaptor<PayMerchantQualEntity> saved = ArgumentCaptor.forClass(PayMerchantQualEntity.class);
        org.mockito.Mockito.verify(mapper).insert(saved.capture());
        assertEquals(2L, saved.getValue().getTenantId());
        assertEquals(0, saved.getValue().getStatus());
        assertEquals("account-b", saved.getValue().getCreateBy());
        assertNull(saved.getValue().getAppSecret());
        assertNull(saved.getValue().getApiKey());
    }
}
