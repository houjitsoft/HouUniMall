package com.houjiang.base.service.impl;

import com.houjiang.base.dto.BaseAiConfigDTO;
import com.houjiang.base.dto.BaseAiProviderCredentialDTO;
import com.houjiang.base.entity.BaseAiConfigEntity;
import com.houjiang.base.mapper.BaseAiConfigMapper;
import com.houjiang.base.vo.BaseAiConfigVO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseAiConfigServiceImplTest {

    @Test
    void reusesProcessCacheForRepeatedListQueries() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        when(mapper.selectList(ArgumentMatchers.any())).thenReturn(List.of(entity(1, "qwen-plus")));
        BaseAiConfigServiceImpl service = serviceWith(mapper);
        BaseAiConfigDTO request = request();

        ResponseResult<PageUtils<BaseAiConfigVO>> first = service.queryAiConfigList(request);
        ResponseResult<PageUtils<BaseAiConfigVO>> second = service.queryAiConfigList(request);

        assertEquals(1, first.getCode());
        assertEquals(1, second.getCode());
        assertEquals("qwen-plus", first.getData().getList().get(0).getModelName());
        assertEquals("qwen-plus", second.getData().getList().get(0).getModelName());
        verify(mapper, times(1)).selectList(ArgumentMatchers.any());
    }

    @Test
    void rebuildsProcessCacheAfterSuccessfulSave() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        when(mapper.selectList(ArgumentMatchers.any()))
            .thenReturn(List.of(entity(1, "old-model")))
            .thenReturn(List.of(entity(1, "new-model")));
        when(mapper.insert(ArgumentMatchers.any(BaseAiConfigEntity.class))).thenReturn(1);
        BaseAiConfigServiceImpl service = serviceWith(mapper);

        service.queryAiConfigList(request());
        ResponseResult<?> saved = service.saveOrUpdateConfig(saveRequest());
        ResponseResult<PageUtils<BaseAiConfigVO>> afterSave = service.queryAiConfigList(request());

        assertEquals(1, saved.getCode());
        assertEquals("new-model", afterSave.getData().getList().get(0).getModelName());
        verify(mapper, times(2)).selectList(ArgumentMatchers.any());
    }
    @Test
    void protectsConfiguredChatLicenseWhenReturningAndUpdatingAvatarOptions() {
        String stored = "{\"templateId\":\"template-1\",\"chatLicense\":\"secret-license\"}";

        assertTrue(BaseAiConfigServiceImpl.hasChatLicense(stored));
        assertFalse(BaseAiConfigServiceImpl.withoutChatLicense(stored).contains("secret-license"));
        String merged = BaseAiConfigServiceImpl.mergeChatLicense(
            stored, "{\"templateId\":\"template-2\",\"chatLicense\":\"\"}");
        assertTrue(BaseAiConfigServiceImpl.hasChatLicense(merged));
        assertFalse(BaseAiConfigServiceImpl.withoutChatLicense(merged).contains("secret-license"));
    }

    @Test
    void bulkProviderCredentialSavePersistsPerModelPricing() {
        BaseAiConfigMapper mapper = mock(BaseAiConfigMapper.class);
        when(mapper.selectOne(ArgumentMatchers.any())).thenReturn(null);
        when(mapper.insert(ArgumentMatchers.any(BaseAiConfigEntity.class))).thenReturn(1);
        when(mapper.selectList(ArgumentMatchers.any())).thenReturn(List.of());
        BaseAiConfigServiceImpl service = serviceWith(mapper);

        BaseAiConfigDTO model = new BaseAiConfigDTO();
        model.setProviderCode("ernie");
        model.setProviderName("百度文心一言");
        model.setModelName("ernie-speed-128k");
        model.setCostPer1kInput(new BigDecimal("0.004"));
        model.setCostPer1kOutput(new BigDecimal("0.004"));

        BaseAiProviderCredentialDTO request = new BaseAiProviderCredentialDTO();
        request.setProviderCode("ernie");
        request.setApiKey("api-key");
        request.setModels(List.of(model));

        ResponseResult<?> result = service.saveProviderCredential(request);

        assertEquals(1, result.getCode());
        ArgumentCaptor<BaseAiConfigEntity> saved = ArgumentCaptor.forClass(BaseAiConfigEntity.class);
        verify(mapper).insert(saved.capture());
        assertEquals(new BigDecimal("0.004"), saved.getValue().getCostPer1kInput());
        assertEquals(new BigDecimal("0.004"), saved.getValue().getCostPer1kOutput());
    }

    private static BaseAiConfigServiceImpl serviceWith(BaseAiConfigMapper mapper) {
        BaseAiConfigServiceImpl service = new BaseAiConfigServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return service;
    }

    private static BaseAiConfigDTO request() {
        BaseAiConfigDTO request = new BaseAiConfigDTO();
        request.setPage(1);
        request.setLimit(20);
        request.setProviderCode("qwen");
        return request;
    }

    private static BaseAiConfigDTO saveRequest() {
        BaseAiConfigDTO request = request();
        request.setModelName("qwen-plus");
        return request;
    }

    private static BaseAiConfigEntity entity(int id, String modelName) {
        BaseAiConfigEntity entity = new BaseAiConfigEntity();
        entity.setId(id);
        entity.setProviderCode("qwen");
        entity.setProviderName("Qwen");
        entity.setModelName(modelName);
        entity.setIsEnabled(1);
        entity.setStatus(1);
        entity.setSortOrder(1);
        return entity;
    }
}
