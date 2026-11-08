package com.houjiang.plugin.service;
import jakarta.servlet.http.HttpServletRequest;
/** The control plane owns installation/license state; this service owns execution. */
@FunctionalInterface
public interface ProductAccessPolicy {
    boolean isEnabled(String code, HttpServletRequest request);
}
