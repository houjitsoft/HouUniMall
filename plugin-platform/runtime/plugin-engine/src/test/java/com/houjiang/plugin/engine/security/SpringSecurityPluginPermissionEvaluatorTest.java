package com.houjiang.plugin.engine.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringSecurityPluginPermissionEvaluatorTest {
    private final SpringSecurityPluginPermissionEvaluator evaluator =
            new SpringSecurityPluginPermissionEvaluator();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void applicationPermissionCoversPluginFineGrainedRoutePermission() {
        authenticate("shell:erp:apps");
        assertTrue(evaluator.hasPermission("erp:dashboard:view"));
    }

    @Test
    void dashboardModulePermissionCoversPluginFineGrainedRoutePermission() {
        authenticate("dashboard:module:finance");

        assertTrue(evaluator.hasPermission("finance:income:list"));
    }

    @Test
    void applicationPermissionDoesNotCrossPluginBoundaries() {
        authenticate("shell:erp:apps");
        assertFalse(evaluator.hasPermission("aicode:read"));
    }

    @Test
    void exactPermissionStillWorks() {
        authenticate("aicode:read");
        assertTrue(evaluator.hasPermission("aicode:read"));
    }

    @Test
    void hasPermissionIsFalseWhenNotAuthenticated() {
        assertFalse(evaluator.isAuthenticated());
        assertFalse(evaluator.hasPermission("erp:goods:read"));
    }

    @Test
    void hasPermissionIsFalseForBlankPermissionEvenWhenAuthenticated() {
        authenticate("erp:goods:read");
        assertFalse(evaluator.hasPermission(null));
        assertFalse(evaluator.hasPermission(""));
        assertFalse(evaluator.hasPermission("   "));
    }

    @Test
    void hasPermissionMatchesGrantedAuthority() {
        authenticate("erp:goods:read");
        assertTrue(evaluator.hasPermission("erp:goods:read"));
        assertFalse(evaluator.hasPermission("erp:goods:write"));
    }

    @Test
    void hasPermissionAcceptsWildcardAuthority() {
        authenticate("*");
        assertTrue(evaluator.hasPermission("任意:权限"));
    }

    private static void authenticate(String... permissions) {
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("user", "N/A",
                Arrays.stream(permissions).map(SimpleGrantedAuthority::new).toList()));
        SecurityContextHolder.setContext(context);
    }
}
