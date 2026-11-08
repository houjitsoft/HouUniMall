package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.controller.finance.FinanceSettingController;
import com.houjiang.bffweb.feign.FinanceServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinanceSettingBatchCreateRouteTest {
    @Test
    void exposesAndForwardsBatchCreateRoute() throws Exception {
        FinanceServiceFeign feign = mock(FinanceServiceFeign.class);
        FinanceSettingController controller = new FinanceSettingController();
        ReflectionTestUtils.setField(controller, "financeServiceFeign", feign);
        Map<String, Object> payload = Map.of(
                "names", List.of("微信", "支付宝"), "descr", "收付款", "status", 1);
        ResponseResult downstream = ResponseResult.success(Map.of("createdCount", 2, "skippedCount", 0));
        when(feign.batchCreateSubjects(payload)).thenReturn(downstream);

        assertSame(downstream, controller.batchCreateSubjects(payload));
        verify(feign).batchCreateSubjects(payload);
        assertArrayEquals(new String[]{"/subjects/batch-create"},
                FinanceSettingController.class.getDeclaredMethod("batchCreateSubjects", Map.class)
                        .getAnnotation(PostMapping.class).value());
        assertArrayEquals(new String[]{"/finance/subjects/batch-create"},
                FinanceServiceFeign.class.getDeclaredMethod("batchCreateSubjects", Map.class)
                        .getAnnotation(PostMapping.class).value());
    }
}
