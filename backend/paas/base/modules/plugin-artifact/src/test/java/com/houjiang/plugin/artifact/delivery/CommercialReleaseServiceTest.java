package com.houjiang.plugin.artifact.delivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.mapper.artifact.PluginArtifactMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.market.PluginLicenseService;
import com.houjiang.plugin.artifact.authorization.*;
import com.houjiang.plugin.artifact.exception.DownloadDeniedException;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.artifact.spi.ArtifactStorage;
import com.houjiang.plugin.signature.ArtifactSigner;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommercialReleaseServiceTest {
    final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    final PluginLicenseService licenses=mock(PluginLicenseService.class);
    final PluginDownloadAuthorizationService auth=mock(PluginDownloadAuthorizationService.class);
    final ArtifactSigner signer=mock(ArtifactSigner.class);
    final CommercialReleaseService service=new CommercialReleaseService(jdbc,licenses,auth,signer,mock(ArtifactStorage.class),mock(PluginArtifactMapper.class),mock(MarketPluginVersionMapper.class));
    final PluginIdentity site=new PluginIdentity("site-1",1L);
    @BeforeEach void setup() throws Exception {
        var m=new ObjectMapper().createObjectNode().put("pluginCode","erp").put("hostService","erp").put("runtime","standalone")
                .put("sourceLicense","Apache-2.0").put("version","1.0.0").put("image","unimall-erp:1.0.0").put("imageId","sha256:"+"a".repeat(64))
                .put("sourceSha256","b".repeat(64)).put("sourceSize",100).put("runtimeSha256","c".repeat(64)).put("runtimeSize",200);
        String envelope=new ObjectMapper().writeValueAsString(Map.of("kid","k1","signature","sig","payload",Base64.getEncoder().encodeToString(m.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        when(signer.verify(eq("k1"),any(byte[].class),eq("sig"))).thenReturn(true);
        when(jdbc.queryForList(contains("SELECT envelope"),anyString(),anyString())).thenReturn(List.of(Map.of("envelope",envelope)));
        when(auth.authorize("erp","1.0.0",site)).thenReturn(DownloadDecision.allow(null,null));
    }
    MarketPluginLicense grant(boolean active) {
        var license=new MarketPluginLicense();license.setStatus(1);license.setLicenseType("PAID");
        license.setMaintenanceUntil(LocalDateTime.now().plusDays(active?10:-10));
        when(licenses.findValidPaid("erp","site-1")).thenReturn(license);return license;
    }
    @Test void paidDeliveryAuthorizationDoesNotInventDeliveryReceipt() {
        grant(true);assertNotNull(service.authorize("erp","1.0.0","source",site));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void rejectsTrialOrCrossSiteWithoutFormalPaidGrant() {
        assertThrows(DownloadDeniedException.class,()->service.authorize("erp","1.0.0","source",site));
        verifyNoInteractions(auth);
    }
    @Test void expiredMaintenanceBlocksNewArtifactButKeepsExactDeliveredSource() {
        grant(false);
        assertThrows(DownloadDeniedException.class,()->service.authorize("erp","1.0.0","oci",site));
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("site-1"),eq("erp"),eq("1.0.0"),eq("source"),eq("b".repeat(64)))).thenReturn(1);
        assertNotNull(service.authorize("erp","1.0.0","source",site));
    }
    @Test void publishedSignatureCannotBeReusedForAnotherProductOrVersion() {
        assertThrows(DownloadDeniedException.class,()->service.release("oa","1.0.0"));
        assertThrows(DownloadDeniedException.class,()->service.release("erp","2.0.0"));
    }
    @Test void invalidSignatureStopsDelivery() {
        when(signer.verify(anyString(),any(byte[].class),anyString())).thenReturn(false);
        assertThrows(IllegalArgumentException.class,()->service.release("erp","1.0.0"));
    }
    @Test void commercialTicketsNeverSignStorageUrlsOrRecordDelivery() {
        grant(true);
        var storage=mock(ArtifactStorage.class);
        var artifacts=mock(PluginArtifactMapper.class);
        var tokens=mock(com.houjiang.plugin.artifact.token.DownloadTokenService.class);
        var issued=mock(com.houjiang.plugin.artifact.token.DownloadTokenService.IssuedToken.class);
        when(issued.getToken()).thenReturn("ticket");
        when(tokens.issue(any())).thenReturn(issued);
        var downloads=new com.houjiang.plugin.artifact.download.PluginArtifactDownloadService(auth,tokens,artifacts,storage);
        downloads.setCommercial(service);
        for(String kind:List.of("source","oci")) {
            var artifact=new com.houjiang.mall.entity.artifact.PluginArtifact();
            artifact.setArtifactId(kind);artifact.setArtifactKind(kind);artifact.setHostService("erp");
            artifact.setStorageKey("commercial/"+kind);artifact.setSha256((kind.equals("source")?"b":"c").repeat(64));
            artifact.setSizeBytes(kind.equals("source")?100L:200L);
            when(artifacts.selectOne(any())).thenReturn(artifact);
            var result=downloads.issueDownloadUrl("erp","1.0.0",kind,site,null,null);
            assertNull(result.getCloudUrl());
            assertEquals("/base/plugin-center/artifact/raw?token=ticket",result.getUrl());
            assertEquals(kind,result.getArtifactKind());
        }
        verifyNoInteractions(storage);
        verify(jdbc,never()).update(anyString(),any(Object[].class));
        verify(tokens,times(2)).issue(any());
    }
    @Test void rawTransferPassesTicketIdentityAndExactRangeToCommercialReceipt() throws Exception {
        var storage=mock(ArtifactStorage.class);
        var artifacts=mock(PluginArtifactMapper.class);
        var tokens=mock(com.houjiang.plugin.artifact.token.DownloadTokenService.class);
        var commercial=mock(CommercialReleaseService.class);
        var downloads=new com.houjiang.plugin.artifact.download.PluginArtifactDownloadService(auth,tokens,artifacts,storage);
        downloads.setCommercial(commercial);
        for(String kind:List.of("source","oci")) {
            var artifact=new com.houjiang.mall.entity.artifact.PluginArtifact();
            artifact.setArtifactId(kind);artifact.setPluginCode("erp");artifact.setVersion("1.0.0");
            artifact.setArtifactKind(kind);artifact.setStorageKey(kind);artifact.setSha256("digest-"+kind);artifact.setSizeBytes(10L);
            when(artifacts.selectOne(any())).thenReturn(artifact);
            when(tokens.verify("ticket",null)).thenReturn(new com.houjiang.plugin.artifact.token.DownloadTokenPayload(kind,"erp","1.0.0","site-1",1L,Long.MAX_VALUE,"nonce",5));
            when(storage.open(kind,null,null)).thenReturn(new com.houjiang.plugin.artifact.model.ArtifactStream(new java.io.ByteArrayInputStream(new byte[10]),10,10,0,9,null));
            when(storage.open(kind,2L,5L)).thenReturn(new com.houjiang.plugin.artifact.model.ArtifactStream(new java.io.ByteArrayInputStream(new byte[4]),4,10,2,5,null));
            var raw=downloads.openByToken("ticket","bytes=2-5");
            verifyNoInteractions(commercial);
            try(var stream=raw.getStream()) { assertEquals(4,stream.getStream().readAllBytes().length); }
            raw.transferred();
            verify(commercial).transferred("site-1",1L,"erp","1.0.0",kind,"digest-"+kind,10,2,5);
            clearInvocations(commercial);
        }
    }
    @Test void jarTicketsKeepSignedUrlAndRawFallback() {
        var storage=mock(ArtifactStorage.class);
        var artifacts=mock(PluginArtifactMapper.class);
        var tokens=mock(com.houjiang.plugin.artifact.token.DownloadTokenService.class);
        var issued=mock(com.houjiang.plugin.artifact.token.DownloadTokenService.IssuedToken.class);
        when(issued.getToken()).thenReturn("ticket");when(tokens.issue(any())).thenReturn(issued);
        var artifact=new com.houjiang.mall.entity.artifact.PluginArtifact();
        artifact.setArtifactId("jar");artifact.setArtifactKind("jar");artifact.setStorageKey("plugins/demo.jar");
        when(artifacts.selectOne(any())).thenReturn(artifact);
        when(auth.authorize("demo","1.0.0",site)).thenReturn(DownloadDecision.allow(null,null));
        when(storage.signedInternalUrl("plugins/demo.jar",java.time.Duration.ofMinutes(10)))
                .thenReturn("signed-jar-url").thenThrow(new UnsupportedOperationException()).thenThrow(new IllegalStateException("存储不可用"));
        var downloads=new com.houjiang.plugin.artifact.download.PluginArtifactDownloadService(auth,tokens,artifacts,storage);
        assertEquals("signed-jar-url",downloads.issueDownloadUrl("demo","1.0.0",site,null,null).getCloudUrl());
        for(int i=0;i<2;i++) {
            var result=downloads.issueDownloadUrl("demo","1.0.0",site,null,null);
            assertNull(result.getCloudUrl());assertEquals("/base/plugin-center/artifact/raw?token=ticket",result.getUrl());
        }
        verify(storage,times(3)).signedInternalUrl("plugins/demo.jar",java.time.Duration.ofMinutes(10));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void expiredMaintenanceKeepsExactDeliveredRuntimeWithoutUpgradeAuthorization() {
        grant(false);
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("site-1"),eq("erp"),eq("1.0.0"),eq("oci"),eq("c".repeat(64)))).thenReturn(1);
        assertNotNull(service.authorize("erp","1.0.0","oci",site));
        verifyNoInteractions(auth);
        assertThrows(DownloadDeniedException.class,()->service.authorize("erp","1.0.0","source",site));
    }
    @Test void completedDeliveryAllowsExpiredMaintenanceRedownloadForEachKind() {
        grant(false);
        for(String kind:List.of("source","oci")) {
            String digest=(kind.equals("source")?"b":"c").repeat(64);
            long size=kind.equals("source")?100:200;
            var delivered=new java.util.concurrent.atomic.AtomicBoolean();
            when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("site-1"),eq("erp"),eq("1.0.0"),eq(kind),eq(digest)))
                    .thenAnswer(invocation->delivered.get()?1:0);
            when(jdbc.update(startsWith("INSERT IGNORE INTO hou_service_artifact_delivery"),eq("site-1"),eq("erp"),eq("1.0.0"),eq(kind),eq(digest),eq(1L)))
                    .thenAnswer(invocation->{delivered.set(true);return 1;});
            assertThrows(DownloadDeniedException.class,()->service.authorize("erp","1.0.0",kind,site));
            when(jdbc.queryForList(contains("SELECT range_start"),anyString(),anyString(),anyString(),anyString(),anyString()))
                    .thenReturn(List.of(Map.of("range_start",0L,"range_end",size-1)));
            service.transferred("site-1",1L,"erp","1.0.0",kind,digest,size,0,size-1);
            assertNotNull(service.authorize("erp","1.0.0",kind,site));
        }
    }
    @Test void fullSourceTransferRecordsDelivery() {
        when(jdbc.queryForList(contains("SELECT range_start"),anyString(),anyString(),anyString(),anyString(),anyString()))
                .thenReturn(List.of(Map.of("range_start",0L,"range_end",99L)));
        service.transferred("site-1",1L,"erp","1.0.0","source","b".repeat(64),100,0,99);
        verify(jdbc).update(startsWith("INSERT IGNORE INTO hou_service_artifact_delivery"),eq("site-1"),eq("erp"),eq("1.0.0"),eq("source"),eq("b".repeat(64)),eq(1L));
    }
    @Test void completeRuntimeTransferRecordsOnlyRuntimeDelivery() {
        when(jdbc.queryForList(contains("SELECT range_start"),anyString(),anyString(),anyString(),anyString(),anyString()))
                .thenReturn(List.of(Map.of("range_start",0L,"range_end",199L)));
        service.transferred("site-1",1L,"erp","1.0.0","oci","c".repeat(64),200,0,199);
        verify(jdbc).update(startsWith("INSERT IGNORE INTO hou_service_artifact_delivery"),eq("site-1"),eq("erp"),eq("1.0.0"),eq("oci"),eq("c".repeat(64)),eq(1L));
    }
    @Test void invalidReceiptNeverWritesRangesOrDelivery() {
        assertThrows(IllegalArgumentException.class,()->service.transferred("site-1",1L,"erp","1.0.0","source","c".repeat(64),100,0,99));
        assertThrows(IllegalArgumentException.class,()->service.transferred("site-1",1L,"erp","1.0.0","oci","c".repeat(64),100,0,99));
        assertThrows(IllegalArgumentException.class,()->service.transferred("site-1",1L,"erp","1.0.0","source","b".repeat(64),100,0,100));
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void incompleteRangesDoNotCountAsDelivery() {
        when(jdbc.queryForList(contains("SELECT range_start"),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(List.of(Map.of("range_start",0L,"range_end",39L),Map.of("range_start",60L,"range_end",99L)));
        service.transferred("site-1",1L,"erp","1.0.0","source","b".repeat(64),100,60,99);
        verify(jdbc,never()).update(startsWith("INSERT IGNORE INTO hou_service_artifact_delivery"),any(Object[].class));
    }
    @Test void completeRangesCountExactlyOneDeliveredArtifact() {
        when(jdbc.queryForList(contains("SELECT range_start"),anyString(),anyString(),anyString(),anyString(),anyString())).thenReturn(List.of(Map.of("range_start",0L,"range_end",59L),Map.of("range_start",50L,"range_end",99L)));
        service.transferred("site-1",1L,"erp","1.0.0","source","b".repeat(64),100,50,99);
        verify(jdbc).update(startsWith("INSERT IGNORE INTO hou_service_artifact_delivery"),eq("site-1"),eq("erp"),eq("1.0.0"),eq("source"),eq("b".repeat(64)),eq(1L));
    }
}
