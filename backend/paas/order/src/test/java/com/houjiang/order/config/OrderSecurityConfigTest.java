package com.houjiang.order.config;

import com.houjiang.commons.config.RedisCache;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderSecurityTestController.class)
@Import(SecurityConfig.class)
class OrderSecurityConfigTest {

    @MockBean
    private RedisCache redisCache;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsPcOrderQueryToValidateItsBodyToken() throws Exception {
        mockMvc.perform(post("/order/base/queryOrdersByPC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\":1}"));
    }

}

@RestController
class OrderSecurityTestController {
    @PostMapping("/order/base/queryOrdersByPC")
    String queryOrdersByPC() {
        return "{\"code\":1}";
    }
}
