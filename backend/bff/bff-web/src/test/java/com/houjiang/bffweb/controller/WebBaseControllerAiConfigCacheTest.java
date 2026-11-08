package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WebBaseControllerAiConfigCacheTest {

    @Test
    void reusesLastSuccessfulListResultWithoutCallingBaseAgain() {
        BaseServiceFeign feign = mock(BaseServiceFeign.class);
        ResponseResult<?> result = ResponseResult.success(Map.of("total", 1));
        when(feign.queryAiConfigList(Map.of("page", 1, "limit", 20))).thenReturn(result);
        WebBaseController controller = controllerWith(feign);

        ResponseResult first = controller.queryAiConfigList(Map.of("page", 1, "limit", 20));
        ResponseResult second = controller.queryAiConfigList(Map.of("page", 1, "limit", 20));

        assertEquals(1, first.getCode());
        assertEquals(result, second);
        verify(feign, times(1)).queryAiConfigList(Map.of("page", 1, "limit", 20));
    }

    @Test
    void returnsExpiredRealCacheAndRefreshesItInBackground() {
        BaseServiceFeign feign = mock(BaseServiceFeign.class);
        Map<String, Object> request = Map.of("page", 1, "limit", 20);
        ResponseResult<?> stale = ResponseResult.success(Map.of("total", 1));
        ResponseResult<?> fresh = ResponseResult.success(Map.of("total", 2));
        when(feign.queryAiConfigList(request)).thenReturn(fresh);
        WebBaseController controller = controllerWith(feign);
        String key = controller.aiConfigCacheKey(request);
        controller.aiConfigListCache.put(key,
                new WebBaseController.CachedAiConfigList(stale, System.currentTimeMillis() - 1));
        ReflectionTestUtils.setField(controller, "aiConfigRefreshExecutor", (Executor) Runnable::run);

        ResponseResult returned = controller.queryAiConfigList(request);

        assertEquals(stale, returned);
        verify(feign, times(1)).queryAiConfigList(request);
        assertEquals(fresh, controller.aiConfigListCache.get(key).result);
    }

    @Test
    void keepsExpiredRealCacheWhenBackgroundRefreshTimesOut() {
        BaseServiceFeign feign = mock(BaseServiceFeign.class);
        Map<String, Object> request = Map.of("page", 1, "limit", 20);
        ResponseResult<?> stale = ResponseResult.success(Map.of("total", 1));
        when(feign.queryAiConfigList(request)).thenThrow(new RuntimeException("Read timed out"));
        WebBaseController controller = controllerWith(feign);
        String key = controller.aiConfigCacheKey(request);
        controller.aiConfigListCache.put(key,
                new WebBaseController.CachedAiConfigList(stale, System.currentTimeMillis() - 1));
        ReflectionTestUtils.setField(controller, "aiConfigRefreshExecutor", (Executor) Runnable::run);

        ResponseResult returned = controller.queryAiConfigList(request);

        assertEquals(stale, returned);
        assertEquals(stale, controller.aiConfigListCache.get(key).result);
        verify(feign, times(1)).queryAiConfigList(request);
    }

    @Test
    void invalidatesAllListCachesAfterSuccessfulMutation() {
        BaseServiceFeign feign = mock(BaseServiceFeign.class);
        WebBaseController controller = controllerWith(feign);
        controller.aiConfigListCache.put("one", new WebBaseController.CachedAiConfigList(
                ResponseResult.success(Map.of()), Long.MAX_VALUE));
        controller.aiConfigListCache.put("two", new WebBaseController.CachedAiConfigList(
                ResponseResult.success(Map.of()), Long.MAX_VALUE));
        when(feign.toggleAiConfigEnabled(7, 0)).thenReturn(ResponseResult.success("已禁用"));

        controller.toggleAiConfigEnabled(7, 0);

        assertEquals(0, controller.aiConfigListCache.size());
        verify(feign).toggleAiConfigEnabled(7, 0);
    }

    private static WebBaseController controllerWith(BaseServiceFeign feign) {
        WebBaseController controller = new WebBaseController();
        ReflectionTestUtils.setField(controller, "baseServiceFeign", feign);
        return controller;
    }
}
