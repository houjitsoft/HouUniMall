package com.houjiang.plugin.service;

import com.houjiang.plugin.sdk.annotation.HouRoute;
import com.houjiang.plugin.sdk.model.PluginServiceOwnership;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Exposes the same service-owned handler through its stable plugin API. No PF4J loading. */
public class ProductRequestMapping extends RequestMappingHandlerMapping {
    private final String code;
    private final String productPackage;

    public ProductRequestMapping(String service, String productPackage) {
        this.code = PluginServiceOwnership.requireService(service);
        this.productPackage = productPackage;
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo original = super.getMappingForMethod(method, handlerType);
        if (!handlerType.getPackageName().equals(productPackage)
                && !handlerType.getPackageName().startsWith(productPackage + ".")) return original;
        HouRoute route = AnnotatedElementUtils.findMergedAnnotation(method, HouRoute.class);
        if (original == null && route == null) return null;
        if (original == null) {
            return RequestMappingInfo.paths("/plugin/" + code + normalized(route.path()))
                    .methods(RequestMethod.valueOf(route.method())).options(getBuilderConfiguration()).build();
        }
        LinkedHashSet<String> paths = new LinkedHashSet<>(original.getPatternValues());
        if (route != null) {
            paths.add("/plugin/" + code + normalized(route.path()));
        } else {
            for (String path : original.getPatternValues()) {
                String normalizedPath = normalized(path);
                for (String prefix : new String[]{"/api/" + code, "/" + code}) {
                    if (normalizedPath.equals(prefix) || normalizedPath.startsWith(prefix + "/")) {
                        paths.add("/plugin/" + code + normalizedPath.substring(prefix.length()));
                        break;
                    }
                }
            }
        }
        return original.mutate().paths(paths.toArray(String[]::new)).build();
    }

    private static String normalized(String value) {
        return value.startsWith("/") ? value : "/" + value;
    }
}
