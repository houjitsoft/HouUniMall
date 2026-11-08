package com.houjiang.base.service.impl;

import com.houjiang.base.entity.OrganizationEntity;
import com.houjiang.base.mapper.OrganizationMapper;
import com.houjiang.commons.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 个人空间组织的保障逻辑：注册后按 PERSONAL_&lt;userId&gt; 幂等建组织。
 */
class OrganizationServiceImplPersonalOrgTest {

    /**
     * ServiceImpl#getEntityClass 需要从真实 mapper 代理里反推实体类型，mock 拿不到，
     * 因此用 spy 直接把实体类型打桩，其余逻辑走真实实现。
     */
    private OrganizationServiceImpl serviceWith(OrganizationMapper mapper) {
        OrganizationServiceImpl impl = new OrganizationServiceImpl();
        ReflectionTestUtils.setField(impl, "baseMapper", mapper);
        OrganizationServiceImpl service = spy(impl);
        doReturn(OrganizationEntity.class).when(service).getEntityClass();
        return service;
    }

    @Test
    void createsPersonalOrganizationWhenMissing() {
        OrganizationMapper mapper = mock(OrganizationMapper.class);
        when(mapper.insert(any(OrganizationEntity.class))).thenAnswer(invocation -> {
            OrganizationEntity entity = invocation.getArgument(0);
            entity.setOrgId(77);
            return 1;
        });
        OrganizationServiceImpl service = serviceWith(mapper);

        ResponseResult result = service.ensurePersonalOrganization(
                Map.of("userId", "42", "displayName", "19900002026"));

        assertEquals(1, result.getCode(), String.valueOf(result.getMessage()));
        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals("77", data.get("orgId"));
        assertEquals("PERSONAL_42", data.get("orgCode"));
        assertEquals(Boolean.TRUE, data.get("isNewOrg"));

        ArgumentCaptor<OrganizationEntity> captor = ArgumentCaptor.forClass(OrganizationEntity.class);
        verify(mapper).insert(captor.capture());
        assertEquals("19900002026的个人空间", captor.getValue().getOrgName());
        assertEquals(4, captor.getValue().getOrgType());
        assertEquals(1, captor.getValue().getOrgStatus());
    }

    @Test
    void reusesExistingPersonalOrganization() {
        OrganizationMapper mapper = mock(OrganizationMapper.class);
        OrganizationEntity existing = new OrganizationEntity();
        existing.setOrgId(9);
        existing.setOrgCode("PERSONAL_42");
        when(mapper.selectOne(any())).thenReturn(existing);
        OrganizationServiceImpl service = serviceWith(mapper);

        ResponseResult result = service.ensurePersonalOrganization(Map.of("userId", "42"));

        Map<?, ?> data = (Map<?, ?>) result.getData();
        assertEquals("9", data.get("orgId"));
        assertEquals(Boolean.FALSE, data.get("isNewOrg"));
        verify(mapper, never()).insert(any(OrganizationEntity.class));
    }

    @Test
    void rejectsBlankUserId() {
        OrganizationServiceImpl service = serviceWith(mock(OrganizationMapper.class));

        ResponseResult result = service.ensurePersonalOrganization(Map.of("displayName", "x"));

        assertNotEquals(1, result.getCode());
    }
}
