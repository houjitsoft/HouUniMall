package com.houjiang.userms.service.impl;

import com.houjiang.commons.dto.ThirdPartyConfigDTO;
import com.houjiang.userms.feign.ThirdPartyConfigFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThirdPartyConfigServiceImplTest {

    @Test
    void feignFailureDefaultConfigDoesNotClaimCompanyOne() {
        ThirdPartyConfigFeignClient client = mock(ThirdPartyConfigFeignClient.class);
        when(client.getConfig("dingtalk")).thenThrow(new RuntimeException("unavailable"));
        ThirdPartyConfigServiceImpl service = new ThirdPartyConfigServiceImpl();
        ReflectionTestUtils.setField(service, "thirdPartyConfigFeignClient", client);

        ThirdPartyConfigDTO config = service.getConfigByPlatform("dingtalk");

        assertNull(config.getOrgId());
    }
}
