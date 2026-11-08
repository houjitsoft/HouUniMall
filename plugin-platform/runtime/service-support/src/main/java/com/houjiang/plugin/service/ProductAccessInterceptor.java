package com.houjiang.plugin.service;

import com.houjiang.plugin.sdk.annotation.HouCaller;
import com.houjiang.plugin.sdk.annotation.HouRoute;
import com.houjiang.plugin.sdk.annotation.HouRouteDefaults;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** Applies product state and route authorization to BOTH stable and legacy service URLs. */
public final class ProductAccessInterceptor implements HandlerInterceptor {
    private final String code;
    private final String productPackage;
    private final ProductAccessPolicy policy;
    public ProductAccessInterceptor(String code, String productPackage, ProductAccessPolicy policy) {
        this.code = code; this.productPackage = productPackage; this.policy = policy;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) return true;
        String pkg = method.getBeanType().getPackageName();
        if (!pkg.equals(productPackage) && !pkg.startsWith(productPackage + ".")) return true;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return reject(response,401,"Authentication required");
        }
        HouRoute route = method.getMethodAnnotation(HouRoute.class);
        Set<String> callers = new HashSet<>();
        if (route != null) callers.addAll(Arrays.asList(route.allowCallers()));
        HouCaller classCaller = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(),HouCaller.class);
        HouCaller methodCaller = method.getMethodAnnotation(HouCaller.class);
        if (classCaller != null) callers.addAll(Arrays.asList(classCaller.value()));
        if (methodCaller != null) callers.addAll(Arrays.asList(methodCaller.value()));
        // A client-supplied caller header is never an identity.
        boolean internal = granted(auth,"ROLE_INTERNAL_JOB");
        if ((internal && !callers.contains("internal")) || (!internal && !callers.isEmpty() && !callers.contains("host"))) {
            return reject(response,403,"Caller not allowed");
        }
        String permission = route == null ? "" : route.permission();
        HouRouteDefaults defaults = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(),HouRouteDefaults.class);
        if (permission.isBlank() && defaults != null) permission = "GET".equals(request.getMethod()) ? defaults.readPermission() : defaults.writePermission();
        if (!internal && !permission.isBlank() && !hasPermission(auth,permission)) {
            return reject(response,403,"Permission denied: " + permission);
        }
        try {
            if (!policy.isEnabled(code,request)) {
                return reject(response,423,"Product not enabled or unlicensed");
            }
        } catch (RuntimeException unavailable) {
            // No mall fallback and no bypass when authoritative state cannot be verified.
            return reject(response,503,"Product access check unavailable");
        }
        return true;
    }
    private boolean hasPermission(Authentication auth,String permission) {
        if (granted(auth,"*") || granted(auth,permission)) return true;
        String product = permission.split(":",2)[0];
        return granted(auth,"shell:"+product+":apps") || granted(auth,"shell:"+product) || granted(auth,"dashboard:module:"+product);
    }
    private boolean granted(Authentication auth,String value) { return auth.getAuthorities().stream().anyMatch(a -> value.equals(a.getAuthority())); }
    private boolean reject(HttpServletResponse response,int status) throws java.io.IOException {
        return reject(response, status, "Product access denied or unavailable");
    }
    private boolean reject(HttpServletResponse response,int status,String message) throws java.io.IOException {
        response.setStatus(status);response.setContentType("application/json;charset=UTF-8");
        String safe = message == null ? "Product access denied or unavailable"
                : message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"code\":"+status+",\"msg\":\""+safe+"\"}");return false;
    }
}
