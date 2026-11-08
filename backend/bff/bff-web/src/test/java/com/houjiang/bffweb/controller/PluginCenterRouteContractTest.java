package com.houjiang.bffweb.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Guards the single plugin-center public and internal route contract. */
class PluginCenterRouteContractTest {

    private static final Path REPO = Path.of("..", "..", "..").toAbsolutePath().normalize();

    @Test
    void bffExposesPluginCenterAndUsesBaseUpstream() throws Exception {
        String market = read("backend/bff/bff-web/src/main/java/com/houjiang/bffweb/controller/plugincenter/PluginMarketController.java");
        String center = read("backend/bff/bff-web/src/main/java/com/houjiang/bffweb/controller/plugincenter/PluginCenterController.java");
        String artifact = read("backend/bff/bff-web/src/main/java/com/houjiang/bffweb/controller/plugin/PluginArtifactProxyController.java");
        String update = read("backend/bff/bff-web/src/main/java/com/houjiang/bffweb/controller/plugin/PluginUpdateProxyController.java");

        assertTrue(market.contains("/web/plugin-center"));
        assertTrue(center.contains("/web/plugin-center"));
        assertTrue(artifact.contains("/web/plugin-center/artifact"));
        assertTrue(update.contains("/web/plugin-center/update"));
        assertTrue(market.contains("PluginCenterServiceFeign"));
        assertTrue(center.contains("PluginCenterServiceFeign"));
        assertFalse(market.contains("/web/mall/market"));
        assertFalse(center.contains("/web/mall/plugin-center"));
        assertFalse(artifact.contains("/web/mall/plugin-artifact"));
        assertFalse(update.contains("/web/mall/plugin-update"));
    }

    @Test
    void baseOwnsInternalPluginCenterRoutes() throws Exception {
        String market = read("backend/paas/base/modules/plugin-market/src/main/java/com/houjiang/mall/controller/market/PluginMarketController.java");
        String center = read("backend/paas/base/modules/plugin-center/src/main/java/com/houjiang/mall/controller/plugin/PluginCenterController.java");
        String artifact = read("backend/paas/base/modules/plugin-artifact/src/main/java/com/houjiang/plugin/artifact/controller/PluginArtifactController.java");
        String update = read("backend/paas/base/modules/plugin-update/src/main/java/com/houjiang/plugin/update/controller/PluginUpdateController.java");

        assertTrue(market.contains("/base/plugin-center"));
        assertTrue(center.contains("/base/plugin-center"));
        assertTrue(artifact.contains("/base/plugin-center/artifact"));
        assertTrue(update.contains("/base/plugin-center/update"));
        assertFalse(market.contains("/mall/market"));
        assertFalse(artifact.contains("/mall/artifact"));
        assertFalse(update.contains("/mall/plugin-update"));
    }

    private static String read(String relative) throws Exception {
        return Files.readString(REPO.resolve(relative));
    }
}
