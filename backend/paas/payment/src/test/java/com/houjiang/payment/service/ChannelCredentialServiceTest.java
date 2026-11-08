package com.houjiang.payment.service;

import com.houjiang.commons.util.PayCredentialCryptoUtil;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.dto.channel.ChannelSaveRequest;
import com.houjiang.payment.mapper.PayChannelMapper;
import com.houjiang.payment.service.api.impl.ChannelServiceImpl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChannelCredentialServiceTest {

    private PayChannelMapper mapper;
    private ChannelServiceImpl service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "ChannelCredentialServiceTest"),
                PayChannelEntity.class);
        PayCredentialCryptoUtil.setKeyIv("0123456789abcdef", "abcdef0123456789");
        mapper = mock(PayChannelMapper.class);
        service = new ChannelServiceImpl(mapper);
    }

    @Test
    @SuppressWarnings("unchecked")
    void credentialDetailNeverReturnsSecretPlaintext() {
        PayChannelEntity entity = new PayChannelEntity();
        entity.setId(1L);
        entity.setChannelCode("WECHAT_V3");
        Map<String, Object> config = new HashMap<>();
        config.put("appId", "wx-app-id");
        config.put("apiV3Key", "real-api-v3-secret");
        entity.setConfigJson(PayCredentialCryptoUtil.encryptConfigJson(config));
        when(mapper.selectById(1L)).thenReturn(entity);

        Map<String, Object> detail = service.getCredentialDecrypted(1L);
        Map<String, Object> returnedConfig = (Map<String, Object>) detail.get("configMap");

        assertEquals("wx-app-id", returnedConfig.get("appId"));
        assertEquals("***", returnedConfig.get("apiV3Key"));
        assertNotEquals("real-api-v3-secret", returnedConfig.get("apiV3Key"));
    }

    @Test
    void blankSecretOnUpdateKeepsStoredCredential() {
        PayChannelEntity entity = new PayChannelEntity();
        entity.setId(1L);
        Map<String, Object> stored = new HashMap<>();
        stored.put("appId", "old-app-id");
        stored.put("apiV3Key", "stored-secret");
        entity.setConfigJson(PayCredentialCryptoUtil.encryptConfigJson(stored));
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.update(org.mockito.ArgumentMatchers.isNull(), org.mockito.ArgumentMatchers.any())).thenReturn(1);

        ChannelSaveRequest request = new ChannelSaveRequest();
        request.setId(1L);
        Map<String, Object> update = new HashMap<>();
        update.put("appId", "new-app-id");
        update.put("apiV3Key", "");

        service.saveCredential(request, update, "admin");

        Map<String, Object> saved = PayCredentialCryptoUtil.decryptConfigJson(request.getConfigJson());
        assertEquals("new-app-id", saved.get("appId"));
        assertEquals("stored-secret", saved.get("apiV3Key"));
    }

    @Test
    void standardAlipayConnectionCheckRequiresSigningKeys() {
        PayChannelEntity entity = new PayChannelEntity();
        entity.setId(2L);
        entity.setChannelCode("ALIPAY");
        entity.setConfigJson("{\"appId\":\"202607100001\"}");
        when(mapper.selectById(2L)).thenReturn(entity);

        Map<String, Object> result = service.testConnect(2L);

        assertEquals(false, result.get("success"));
        assertEquals("CHANNEL_NOT_CONFIGURED", result.get("errorCode"));
    }

    @Test
    void wechatV3ConnectionCheckRequiresMerchantPrivateKey() {
        PayChannelEntity entity = new PayChannelEntity();
        entity.setId(3L);
        entity.setChannelCode("WECHAT_V3");
        entity.setConfigJson("{\"appId\":\"wx1\",\"mchId\":\"m1\","
                + "\"apiV3Key\":\"key\",\"certSerialNo\":\"serial\"}");
        when(mapper.selectById(3L)).thenReturn(entity);

        Map<String, Object> result = service.testConnect(3L);

        assertEquals(false, result.get("success"));
        assertEquals("CHANNEL_NOT_CONFIGURED", result.get("errorCode"));
    }
}
