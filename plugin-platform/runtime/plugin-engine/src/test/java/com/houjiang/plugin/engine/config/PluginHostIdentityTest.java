package com.houjiang.plugin.engine.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginHostIdentityTest {
    @Test void usesSpringApplicationNameByDefault() {
        assertEquals("goods", new PluginEngineProperties().resolveHostService("goods"));
    }

    @Test void explicitServiceIdentityTakesPrecedence() {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setHostService("oa");
        assertEquals("oa", properties.resolveHostService("deployment-instance"));
    }

    @Test void missingIdentityAndLegacyModeHaveNoImplicitFallback() {
        PluginEngineProperties properties = new PluginEngineProperties();
        assertNull(properties.resolveHostService(null));
        assertFalse(properties.isAllowLegacyHost());
    }
}
