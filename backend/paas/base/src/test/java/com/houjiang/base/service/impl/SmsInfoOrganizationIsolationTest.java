package com.houjiang.base.service.impl;

import com.houjiang.base.dto.BaseSmsConfigDTO;
import com.houjiang.base.entity.SmsInfoEntity;
import com.houjiang.base.mapper.SmsInfoMapper;
import com.houjiang.base.security.BaseOrganizationResolver;
import com.houjiang.commons.config.RedisCache;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SmsInfoOrganizationIsolationTest {

    @Test
    void updateCreatesConfigWithServerResolvedOrganization() {
        SmsInfoMapper mapper = mock(SmsInfoMapper.class);
        BaseOrganizationResolver resolver = mock(BaseOrganizationResolver.class);
        when(resolver.resolveCurrentOrgId()).thenReturn("2");
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any(SmsInfoEntity.class))).thenReturn(1);

        SmsInfoServiceImpl service = new SmsInfoServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        ReflectionTestUtils.setField(service, "redisCache", mock(RedisCache.class));
        setResolver(service, resolver);

        BaseSmsConfigDTO request = new BaseSmsConfigDTO();
        request.setSmsType("aliyun");
        request.setOrgId("999");
        request.setSmsSecretId("secret");
        service.updateSmsConfig(request);

        ArgumentCaptor<SmsInfoEntity> captor = ArgumentCaptor.forClass(SmsInfoEntity.class);
        verify(mapper).insert(captor.capture());
        assertEquals("2", captor.getValue().getOrgId());
        assertNotEquals("999", captor.getValue().getOrgId());
    }

    @Test
    void rejectsSmsAccessWhenOrganizationCannotBeResolved() {
        SmsInfoMapper mapper = mock(SmsInfoMapper.class);
        BaseOrganizationResolver resolver = mock(BaseOrganizationResolver.class);
        when(resolver.resolveCurrentOrgId()).thenReturn(null);

        SmsInfoServiceImpl service = new SmsInfoServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        ReflectionTestUtils.setField(service, "redisCache", mock(RedisCache.class));
        setResolver(service, resolver);

        BaseSmsConfigDTO request = new BaseSmsConfigDTO();
        request.setSmsType("aliyun");
        assertTrue(service.updateSmsConfig(request).getCode() != 1);
        verifyNoInteractions(mapper);
    }

    private static void setResolver(SmsInfoServiceImpl service, BaseOrganizationResolver resolver) {
        try {
            var field = SmsInfoServiceImpl.class.getDeclaredField("organizationResolver");
            field.setAccessible(true);
            field.set(service, resolver);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
