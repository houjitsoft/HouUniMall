package com.houjiang.plugin.service;

import com.houjiang.plugin.sdk.annotation.HouRoute;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import java.lang.reflect.Method;
import static org.assertj.core.api.Assertions.*;

class ProductRequestMappingTest {
    @RestController @RequestMapping("/api/erp")
    static class ErpController {
        @GetMapping("/dashboard") @HouRoute(path="/dashboard", method="GET", permission="erp:dashboard:view")
        public String dashboard() { return "ok"; }
        @PostMapping("/sale") public String sale() { return "ok"; }
    }
    static class Exposed extends ProductRequestMapping {
        Exposed() { super("erp", "com.houjiang.plugin.service"); }
        RequestMappingInfo mapping(String method) throws Exception {
            return getMappingForMethod(ErpController.class.getMethod(method), ErpController.class);
        }
    }
    @Test void retainsLegacyPathsAndAddsProductPathWithSameMethod() throws Exception {
        var mapping = new Exposed().mapping("sale");
        assertThat(mapping.getPatternValues()).containsExactlyInAnyOrder("/api/erp/sale", "/plugin/erp/sale");
        assertThat(mapping.getMethodsCondition().getMethods()).containsExactly(RequestMethod.POST);
    }
    @Test void explicitContractRouteIsPreserved() throws Exception {
        assertThat(new Exposed().mapping("dashboard").getPatternValues()).contains("/plugin/erp/dashboard");
    }
    @Test void rejectsMallAndUnrecognizedHost() {
        assertThatThrownBy(() -> new ProductRequestMapping("mall", "com.houjiang.mall")).isInstanceOf(IllegalStateException.class);
    }
}
