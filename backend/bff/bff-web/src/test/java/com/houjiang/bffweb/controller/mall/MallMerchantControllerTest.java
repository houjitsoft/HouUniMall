package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MerchantServiceFeign;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MallMerchantControllerTest {

    @Test
    void forwardsShopDetailToMerchantProvisioningService() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        when(feign.getProvisionedShopDetail(Map.of("id", 1)))
                .thenReturn(Map.of("code", 1, "data", Map.of("id", 1, "name", "测试店铺")));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new MallMerchantController(feign)).build();

        mvc.perform(post("/web/mall/merchant/detail")
                        .contentType("application/json")
                        .content("{\"id\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(feign).getProvisionedShopDetail(Map.of("id", 1));
    }

    @Test
    void forwardsShopUpdateToMerchantProvisioningService() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        Map<String, Object> request = Map.of("id", 1, "name", "测试店铺");
        when(feign.updateProvisionedShop(request)).thenReturn(Map.of("code", 1));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new MallMerchantController(feign)).build();

        mvc.perform(post("/web/mall/merchant/update")
                        .contentType("application/json")
                        .content("{\"id\":1,\"name\":\"测试店铺\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        verify(feign).updateProvisionedShop(request);
    }
}
