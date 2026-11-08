package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebBaseControllerWebsiteSwitchesTest {

    private final ExecutorService refreshExecutor = Executors.newSingleThreadExecutor();

    @AfterEach
    void tearDown() {
        refreshExecutor.shutdownNow();
    }

    @Test
    void coldCacheReturnsDefaultWithoutWaitingForSlowBaseService() throws Exception {
        WebBaseController controller = new WebBaseController();
        BaseServiceFeign feign = mock(BaseServiceFeign.class);
        CountDownLatch refreshStarted = new CountDownLatch(1);
        CountDownLatch releaseRefresh = new CountDownLatch(1);
        when(feign.selectConfigValueByKey(anyString())).thenAnswer(invocation -> {
            refreshStarted.countDown();
            releaseRefresh.await(5, TimeUnit.SECONDS);
            return "0";
        });
        ReflectionTestUtils.setField(controller, "baseServiceFeign", feign);
        ReflectionTestUtils.setField(controller, "websiteSwitchesRefreshExecutor", refreshExecutor);

        ResponseResult response = assertTimeoutPreemptively(
                Duration.ofMillis(500),
                controller::getWebsiteSwitches
        );

        assertEquals(1, response.getCode());
        Map<?, ?> data = (Map<?, ?>) response.getData();
        assertEquals(Boolean.TRUE, data.get("internationalizationEnabled"));
        assertEquals(Boolean.TRUE, data.get("multiOrganizationEnabled"));
        assertEquals(Boolean.TRUE, data.get("aiAssistantEnabled"));
        assertTrue(refreshStarted.await(1, TimeUnit.SECONDS));
        releaseRefresh.countDown();
    }

    @Test
    void saveWebsiteSwitchesPersistsAiAssistantSwitch() {
        WebBaseController controller = new WebBaseController();
        BaseServiceFeign feign = mock(BaseServiceFeign.class);
        when(feign.uploadLanguageSwitch(org.mockito.ArgumentMatchers.anyMap()))
                .thenReturn(ResponseResult.success());
        ReflectionTestUtils.setField(controller, "baseServiceFeign", feign);

        ResponseResult response = controller.saveWebsiteSwitches(Map.of(
                "internationalizationEnabled", true,
                "multiOrganizationEnabled", true,
                "aiAssistantEnabled", false
        ));

        assertEquals(1, response.getCode());
        Map<?, ?> data = (Map<?, ?>) response.getData();
        assertEquals(Boolean.FALSE, data.get("aiAssistantEnabled"));
        verify(feign).uploadLanguageSwitch(argThat(payload ->
                "aiComparison_switch".equals(payload.get("configKey"))
                        && "0".equals(payload.get("configValue"))));
    }
}
