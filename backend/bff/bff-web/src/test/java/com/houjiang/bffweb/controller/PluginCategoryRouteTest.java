package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.controller.plugincenter.PluginCenterController;
import com.houjiang.bffweb.feign.PluginCenterServiceFeign;
import com.houjiang.bffweb.feign.PluginCenterServiceFeignFallback;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PluginCategoryRouteTest {
    @Test void publicCategoryRouteReturnsBaseDictionaryUnchanged() throws Exception {
        var upstream = mock(PluginCenterServiceFeign.class);
        when(upstream.pluginCenterCategories()).thenReturn(ResponseResult.success(
                List.of(Map.of("id", 72, "name", "客户自定义", "sort", 9))));
        MockMvcBuilders.standaloneSetup(new PluginCenterController(upstream)).build()
                .perform(post("/web/plugin-center/categories"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value(72))
                .andExpect(jsonPath("$.data[0].name").value("客户自定义"))
                .andExpect(jsonPath("$.data[0].sort").value(9));
        verify(upstream).pluginCenterCategories();
    }

    @Test void missingUpstreamIsNotAnEmptySuccessfulDictionary() {
        assertNotEquals(ResponseResult.success().getCode(), new PluginCenterServiceFeignFallback().pluginCenterCategories().getCode());
    }
}
