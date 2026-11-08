package com.houjiang.thirdparty.controller.message;

import com.houjiang.thirdparty.entity.message.JPushConfigEntity;
import com.houjiang.thirdparty.entity.message.JPushHistoryEntity;
import com.houjiang.thirdparty.service.message.JPushConfigService;
import com.houjiang.thirdparty.service.message.JPushHistoryService;
import com.houjiang.thirdparty.service.message.MessageRequestContext;
import com.houjiang.thirdparty.service.message.MessageRequestContextResolver;
import com.houjiang.thirdparty.service.message.sender.JPushSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class JPushControllerTest {

    private JPushConfigService configService;
    private JPushHistoryService historyService;
    private JPushSender sender;
    private MessageRequestContextResolver contextResolver;
    private JPushController controller;

    @BeforeEach
    void setUp() {
        configService = mock(JPushConfigService.class);
        historyService = mock(JPushHistoryService.class);
        sender = mock(JPushSender.class);
        contextResolver = mock(MessageRequestContextResolver.class);
        when(contextResolver.resolveCurrent()).thenReturn(new MessageRequestContext("501", "org-2"));
        controller = new JPushController();
        ReflectionTestUtils.setField(controller, "jPushConfigService", configService);
        ReflectionTestUtils.setField(controller, "jPushHistoryService", historyService);
        ReflectionTestUtils.setField(controller, "jPushSender", sender);
        ReflectionTestUtils.setField(controller, "contextResolver", contextResolver);
    }

    @Test
    void configLookupUsesAuthorizedOrganizationInsteadOfRequestParameter() {
        controller.getConfig("org-1");

        verify(configService).getConfigByOrgId("org-2");
        verify(configService, never()).getConfigByOrgId("org-1");
    }

    @Test
    void historyDetailLoadsOnlyWithinAuthorizedOrganization() {
        controller.getHistoryDetail(9L);

        verify(contextResolver).resolveCurrent();
        verify(historyService).getById("org-2", 9L);
    }

    @Test
    void saveConfigOverwritesSubmittedOrganization() {
        JPushConfigEntity config = new JPushConfigEntity();
        config.setOrgId("org-1");

        controller.saveConfig(config);

        ArgumentCaptor<JPushConfigEntity> captor = ArgumentCaptor.forClass(JPushConfigEntity.class);
        verify(configService).saveOrUpdateConfig(captor.capture());
        assertEquals("org-2", captor.getValue().getOrgId());
    }
}
