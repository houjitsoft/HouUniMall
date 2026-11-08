package com.houjiang.plugin.service;

import com.houjiang.plugin.sdk.annotation.HouRoute;
import com.houjiang.plugin.sdk.annotation.HouCaller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ProductAccessInterceptorTest {
    static class Handler {
        @HouRoute(path="/save",permission="erp:goods:write") public void save() {}
        @HouCaller("anonymous") public void publicStatus() {}
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void disabledProductRejectsLegacyRouteToo() throws Exception {
        login("*");
        var interceptor = new ProductAccessInterceptor("erp", Handler.class.getPackageName(), (c,r) -> false);
        var request = new MockHttpServletRequest("POST", "/api/erp/save");
        var response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(request,response,handler())).isFalse();
        assertThat(response.getStatus()).isEqualTo(423);
    }
    @Test void enabledProductStillRequiresRoutePermission() throws Exception {
        login("oa:read");
        var interceptor = new ProductAccessInterceptor("erp", Handler.class.getPackageName(), (c,r) -> true);
        var response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(new MockHttpServletRequest("POST","/plugin/erp/save"),response,handler())).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }
    @Test void authorizedEnabledProductCanRun() throws Exception {
        login("erp:goods:write");
        var interceptor = new ProductAccessInterceptor("erp", Handler.class.getPackageName(), (c,r) -> true);
        assertThat(interceptor.preHandle(new MockHttpServletRequest("POST","/plugin/erp/save"),new MockHttpServletResponse(),handler())).isTrue();
    }
    @Test void missingIdentityCannotUseProduct() throws Exception {
        var interceptor = new ProductAccessInterceptor("erp", Handler.class.getPackageName(), (c,r) -> true);
        var response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(new MockHttpServletRequest(),response,handler())).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }
    @Test void explicitlyAnonymousEndpointCanRunWithoutIdentity() throws Exception {
        var interceptor = new ProductAccessInterceptor("erp", Handler.class.getPackageName(), (c,r) -> false);
        var response = new MockHttpServletResponse();
        assertThat(interceptor.preHandle(new MockHttpServletRequest("GET", "/erp/public-status"),
                response, publicHandler())).isTrue();
    }
    private HandlerMethod handler() throws Exception { return new HandlerMethod(new Handler(),Handler.class.getMethod("save")); }
    private HandlerMethod publicHandler() throws Exception { return new HandlerMethod(new Handler(),Handler.class.getMethod("publicStatus")); }
    private void login(String authority) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user","",List.of(new SimpleGrantedAuthority(authority)))); }
}
