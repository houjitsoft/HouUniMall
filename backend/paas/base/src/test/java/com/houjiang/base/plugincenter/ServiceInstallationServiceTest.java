package com.houjiang.base.plugincenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.plugin.artifact.delivery.CommercialReleaseService;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceInstallationServiceTest {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final CommercialReleaseService releases=mock(CommercialReleaseService.class);
    private final ServiceInstallationService service=new ServiceInstallationService(jdbc,releases);
    private final String id="12345678-1234-1234-1234-123456789012";
    private Map<String,Object> task(String state) {
        return Map.of("taskId",id,"state",state,"pluginCode","erp","version","1.0.0","imageId","sha256:"+"a".repeat(64),"sourceSha256","b".repeat(64),"runtimeSha256","c".repeat(64));
    }
    private void pending() {
        when(jdbc.queryForList(anyString(),eq("site-1"),eq(id))).thenReturn(List.of(Map.of("id",7L,"plugin_code","erp","version","1.0.0","state","PENDING")));
    }
    @Test void persistsOnlySignedSuccessForKnownInstanceTask() {
        pending();
        when(releases.release("erp","1.0.0")).thenReturn(new CommercialReleaseService.Release("","","",new ObjectMapper().valueToTree(task("SUCCESS"))));
        service.reconcile("site-1",id,task("SUCCESS"));
        verify(jdbc).update(contains("INSERT INTO hou_service_installation"),eq("site-1"),eq("erp"),eq("1.0.0"),eq("sha256:"+"a".repeat(64)),eq("b".repeat(64)),eq("c".repeat(64)),eq(7L));
    }
    @Test void rejectsAnotherInstanceReceipt() {
        when(jdbc.queryForList(anyString(),eq("site-2"),eq(id))).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class,()->service.reconcile("site-2",id,task("SUCCESS")));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void rejectsMismatchingSignedDigest() {
        pending();
        when(releases.release("erp","1.0.0")).thenReturn(new CommercialReleaseService.Release("","","",new ObjectMapper().createObjectNode().put("imageId","wrong")));
        assertThrows(IllegalArgumentException.class,()->service.reconcile("site-1",id,task("SUCCESS")));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void pendingOrRolledBackTaskDoesNotPublishInstallation() {
        pending();service.reconcile("site-1",id,task("RUNNING"));service.reconcile("site-1",id,task("ROLLED_BACK"));
        verifyNoInteractions(releases);
        verify(jdbc,never()).update(contains("hou_service_installation"),any(Object[].class));
    }
    @Test void oldTerminalTaskCannotOverwriteCurrentInstallation() {
        when(jdbc.queryForList(anyString(),eq("site-1"),eq(id))).thenReturn(List.of(Map.of("id",7L,"state","SUCCESS")));
        service.reconcile("site-1",id,task("SUCCESS"));verifyNoInteractions(releases);
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void agentMustReturnRequestedVersion() {
        assertThrows(IllegalStateException.class,()->service.submitted("site-1","erp","2.0.0",task("PENDING")));
        verifyNoInteractions(jdbc);
    }
}
