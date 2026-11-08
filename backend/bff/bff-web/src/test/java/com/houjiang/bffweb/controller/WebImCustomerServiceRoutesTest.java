package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.ImServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebImCustomerServiceRoutesTest {

    @Test
    void forwardsEvaluationSubmitAndConversationLookup() throws Exception {
        ImServiceFeign feign = mock(ImServiceFeign.class);
        ResponseResult<Object> result = ResponseResult.success(Map.of("ok", true));
        when(feign.submitCustomerServiceEvaluation(any())).thenReturn(result);
        when(feign.getCustomerServiceEvaluationByConversation(91)).thenReturn(result);
        MockMvc mvc = standaloneSetup(controllerWith(feign)).build();

        mvc.perform(post("/web/im/customer/service/evaluation/submit")
                        .contentType("application/json").content("{\"conversationId\":91,\"score\":5}"))
                .andExpect(status().isOk());
        mvc.perform(get("/web/im/customer/service/evaluation/conversation/91"))
                .andExpect(status().isOk());

        verify(feign).submitCustomerServiceEvaluation(any());
        verify(feign).getCustomerServiceEvaluationByConversation(91);
    }

    @Test
    void forwardsListPageAndStatsSummaryWithQueryParameters() throws Exception {
        ImServiceFeign feign = mock(ImServiceFeign.class);
        ResponseResult<Object> result = ResponseResult.success();
        when(feign.listCustomerServiceEvaluationsPage(any())).thenReturn(result);
        when(feign.getCustomerServiceSessionDailySummary(any())).thenReturn(result);
        MockMvc mvc = standaloneSetup(controllerWith(feign)).build();

        mvc.perform(get("/web/im/customer/service/evaluation/listPage")
                        .param("page", "2").param("limit", "20").param("shopId", "7"))
                .andExpect(status().isOk());
        mvc.perform(get("/web/im/csSessionStats/dailySummary")
                        .param("startDate", "2026-09-01").param("endDate", "2026-09-08"))
                .andExpect(status().isOk());

        verify(feign).listCustomerServiceEvaluationsPage(any());
        verify(feign).getCustomerServiceSessionDailySummary(any());
    }

    private WebImController controllerWith(ImServiceFeign feign) {
        WebImController controller = new WebImController();
        ReflectionTestUtils.setField(controller, "imServiceFeign", feign);
        return controller;
    }
}
