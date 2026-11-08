package com.houjiang.plugin.update.risk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.houjiang.plugin.update.precheck.PluginCompatibilityPrecheckService;
import com.houjiang.plugin.update.precheck.PrecheckResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UpgradeRiskServiceTest {

    @Test
    void alwaysRequiresBackupAndMigrationAck() {
        PluginCompatibilityPrecheckService precheck = mock(PluginCompatibilityPrecheckService.class);
        when(precheck.precheck(eq("goods"), any())).thenReturn(PrecheckResult.pass());
        UpgradeRiskService service = new UpgradeRiskService(precheck);

        Map<String, Object> report = service.report("goods", "2.0.0", "goods");
        assertTrue((Boolean) report.get("willBackup"));
        assertTrue((Boolean) report.get("irreversibleMigration"));
        assertFalse((Boolean) report.get("blocking"));
        assertEquals(List.of("backup", "migration", "extension"), report.get("mustAcknowledge"));
    }

    @Test
    void blockingWhenPrecheckFails() {
        PluginCompatibilityPrecheckService precheck = mock(PluginCompatibilityPrecheckService.class);
        when(precheck.precheck(eq("erp"), any())).thenReturn(PrecheckResult.fail(3014, "框架过低"));
        UpgradeRiskService service = new UpgradeRiskService(precheck);

        Map<String, Object> report = service.report("erp", "1.1.0", null);
        assertTrue((Boolean) report.get("blocking"));
        assertEquals("框架过低", report.get("precheckDetail"));
    }
}
