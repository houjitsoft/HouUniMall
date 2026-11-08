package com.houjiang.merchant.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.merchant.common.entity.mall.MallShopConfig;
import com.houjiang.merchant.common.mapper.mall.MallShopConfigMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigServiceImplTest {

    @Test
    void returnsDefaultH5ConfigWhenOnlyH5ConfigIsMissing() {
        MallShopConfigMapper mapper = mock(MallShopConfigMapper.class);
        when(mapper.selectOne(any()))
                .thenReturn(config("mnp"), config("oa"), null);
        ConfigServiceImpl service = new ConfigServiceImpl();
        service.mallShopConfigMapper = mapper;

        JSONObject result = assertDoesNotThrow(() -> service.getService(1));

        assertEquals("1", result.getJSONObject("h5").getString("way"));
        assertEquals("", result.getJSONObject("h5").getString("qr_code"));
    }

    private MallShopConfig config(String name) {
        MallShopConfig config = new MallShopConfig();
        config.setName(name);
        config.setValue("{\"qr_code\":\"\"}");
        return config;
    }
}
