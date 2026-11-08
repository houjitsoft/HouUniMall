package com.houjiang.plugin.engine.registry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RouteRegistryTest {

    @Test
    void templateLookupUsesPluginAndMethodSnapshotIndex() {
        RouteRegistry registry = new RouteRegistry();
        RouteRegistry.RouteEntry broad = route("erp", "/orders/{id}", "GET");
        RouteRegistry.RouteEntry narrow = route("erp", "/orders/detail/{id}", "GET");
        registry.register(broad);
        registry.register(narrow);

        assertNotNull(registry.lookup("erp", "/orders/detail/42", "GET"));
        assertNotNull(registry.lookup("erp", "/orders/42", "GET"));
        RouteRegistry.RouteEntry trailing = route("erp", "/health/", "GET");
        registry.register(trailing);
        assertNotNull(registry.lookup("erp", "/health", "GET"));
        assertNotNull(registry.lookup("erp", "/health/", "GET"));
        assertNull(registry.lookup("erp", "/orders/42", "POST"));
        assertNull(registry.lookup("other", "/orders/42", "GET"));

        registry.removeByPlugin("erp");
        assertNull(registry.lookup("erp", "/orders/42", "GET"));
    }

    private RouteRegistry.RouteEntry route(String code, String path, String method) {
        RouteRegistry.RouteEntry entry = new RouteRegistry.RouteEntry();
        entry.code = code;
        entry.path = path;
        entry.httpMethod = method;
        return entry;
    }
}
