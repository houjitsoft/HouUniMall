package com.houjiang.bffweb.service;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgreementConsentServiceTest {
    private final BaseServiceFeign base = mock(BaseServiceFeign.class);
    private final UserServiceFeign users = mock(UserServiceFeign.class);
    private final AgreementConsentService service = new AgreementConsentService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseServiceFeign", base);
        ReflectionTestUtils.setField(service, "userServiceFeign", users);
        when(base.agreementList(anyMap())).thenAnswer(invocation -> {
            Map<String, Object> request = invocation.getArgument(0);
            if ("install".equals(request.get("scene"))) {
                return ResponseResult.success(List.of(
                        agreement("OPEN_SOURCE_EULA", "开源软件使用许可协议（EULA）", "v1.0.0", "install")));
            }
            return ResponseResult.success(List.of(
                    agreement("SERVICE", "用户服务协议", "v1.1.0"),
                    agreement("PRIVACY", "隐私政策", "v1.0.0"),
                    agreement("COMPLIANCE", "账号与内容合规使用承诺书", "v1.0.0")));
        });
        when(users.agreementConsentListByUser(anyMap())).thenReturn(ResponseResult.success(List.of(
                consent("SERVICE", "v1.0.0"), consent("PRIVACY", "v1.0.0"),
                consent("COMPLIANCE", "v1.0.0"))));
        when(users.agreementConsentRecord(anyMap())).thenReturn(ResponseResult.success(1));
    }

    @Test
    void acceptingOnlyTheDisplayedUpdatedAgreementSucceeds() {
        var pending = service.listPendingAgreements("user-1", "register");
        assertEquals(List.of("SERVICE"), pending.stream().map(item -> item.get("code")).toList());
        var result = reagree(List.of(Map.of("code", "SERVICE", "versionNo", "v1.1.0")));
        assertTrue(result.isOk(), result.getMessage());
        verify(users).agreementConsentRecord(argThat(request ->
                "user-1".equals(request.get("userId")) && "reagree".equals(request.get("scene"))
                        && request.get("consents") instanceof List<?> rows && rows.size() == 1
                        && rows.get(0) instanceof Map<?, ?> row
                        && "SERVICE".equals(row.get("code")) && "v1.1.0".equals(row.get("versionNo"))));
    }

    @Test
    void missingUpdatedAgreementStillFails() {
        var result = reagree(List.of());
        assertFalse(result.isOk());
        assertTrue(result.getMessage().contains("用户服务协议"));
        assertFalse(result.getMessage().contains("隐私政策"));
        verify(users, never()).agreementConsentRecord(anyMap());
    }

    @Test
    void outdatedSubmittedVersionStillFails() {
        var result = reagree(List.of(Map.of("code", "SERVICE", "versionNo", "v1.0.0")));
        assertFalse(result.isOk());
        verify(users, never()).agreementConsentRecord(anyMap());
    }

    @Test
    void previouslyUnacceptedAgreementsAreStillRequired() {
        when(users.agreementConsentListByUser(anyMap())).thenReturn(ResponseResult.success(List.of()));
        var result = reagree(List.of(Map.of("code", "SERVICE", "versionNo", "v1.1.0")));
        assertFalse(result.isOk());
        assertTrue(result.getMessage().contains("隐私政策"));
        verify(users, never()).agreementConsentRecord(anyMap());
    }

    @Test
    void registrationStillRequiresEveryRequiredAgreement() {
        var result = service.validateAgreements(
                List.of(Map.of("code", "SERVICE", "versionNo", "v1.1.0")), "register");
        assertFalse(result.isOk());
        assertTrue(result.getMessage().contains("隐私政策"));
        verifyNoInteractions(users);
    }

    @Test
    void saveFailureIsNotReportedAsSuccess() {
        when(users.agreementConsentRecord(anyMap())).thenReturn(ResponseResult.error("unavailable"));
        var result = reagree(List.of(Map.of("code", "SERVICE", "versionNo", "v1.1.0")));
        assertFalse(result.isOk());
        assertTrue(result.getMessage().contains("保存失败"));
    }

    @Test
    void loginPendingContainsOnlySiteOperatorAgreements() {
        var pending = service.listPendingForLogin("user-1");

        assertEquals(List.of("SERVICE"), pending.stream().map(item -> item.get("code")).toList());
    }

    @Test
    void loginReagreementDoesNotAcceptInstallerEulaForTheSite() {
        var result = service.validateAndRecordLoginPending(
                "user-1",
                "tester",
                List.of(
                        Map.of("code", "SERVICE", "versionNo", "v1.1.0"),
                        Map.of("code", "OPEN_SOURCE_EULA", "versionNo", "v1.0.0")),
                "127.0.0.1",
                "test",
                "web");

        assertTrue(result.isOk(), result.getMessage());
        verify(base, never()).agreementPersistSiteEula(anyMap());
    }

    @Test
    void consentLookupFailureMustNotLookLikeNeverAgreed() {
        when(users.agreementConsentListByUser(anyMap())).thenReturn(ResponseResult.error("unavailable"));

        assertNull(service.listPendingAgreements("user-1", "register"),
                "userms 不可用时必须返回 null，禁止当成「从未同意」而弹出补签");
        assertNull(service.listPendingForLogin("user-1"));
    }

    @Test
    void reagreeMustFailClosedWhenAgreementCatalogUnavailable() {
        when(base.agreementList(anyMap())).thenReturn(ResponseResult.error("base down"));

        var result = reagree(List.of(Map.of("code", "SERVICE", "versionNo", "v1.1.0")));

        assertFalse(result.isOk());
        assertTrue(result.getMessage().contains("协议服务不可用")
                || result.getMessage().contains("稍后"), result.getMessage());
        verify(users, never()).agreementConsentRecord(anyMap());
    }

    @Test
    void reagreeMustFailClosedWhenConsentLookupUnavailable() {
        when(users.agreementConsentListByUser(anyMap())).thenReturn(ResponseResult.error("unavailable"));

        var result = reagree(List.of(Map.of("code", "SERVICE", "versionNo", "v1.1.0")));

        assertFalse(result.isOk());
        assertTrue(result.getMessage().contains("同意记录")
                || result.getMessage().contains("稍后"), result.getMessage());
        verify(users, never()).agreementConsentRecord(anyMap());
    }

    @Test
    void agreedVersionsAcceptSnakeCaseKeysFromFeign() {
        when(users.agreementConsentListByUser(anyMap())).thenReturn(ResponseResult.success(List.of(
                Map.of("agreement_code", "SERVICE", "version_no", "v1.1.0"),
                Map.of("agreement_code", "PRIVACY", "version_no", "v1.0.0"),
                Map.of("agreement_code", "COMPLIANCE", "version_no", "v1.0.0"))));

        var pending = service.listPendingAgreements("user-1", "register");
        assertNotNull(pending);
        assertTrue(pending.isEmpty(), "snake_case 同意记录应被识别，避免已同意仍弹窗");
    }

    @Test
    void idempotentReagreeWhenNothingPendingSucceedsWithoutWrite() {
        when(users.agreementConsentListByUser(anyMap())).thenReturn(ResponseResult.success(List.of(
                consent("SERVICE", "v1.1.0"), consent("PRIVACY", "v1.0.0"),
                consent("COMPLIANCE", "v1.0.0"))));

        var result = reagree(List.of(Map.of("code", "SERVICE", "versionNo", "v1.1.0")));

        assertTrue(result.isOk(), result.getMessage());
        verify(users, never()).agreementConsentRecord(anyMap());
    }

    private AgreementConsentService.ValidateResult reagree(Object agreements) {
        return service.validateAndRecord("user-1", "tester", agreements,
                "127.0.0.1", "test", "web", "register", "reagree");
    }

    private Map<String, Object> agreement(String code, String name, String version) {
        return agreement(code, name, version, "register");
    }

    private Map<String, Object> agreement(String code, String name, String version, String scene) {
        return Map.of("id", code, "code", code, "name", name, "required", 1,
                "scene", scene, "versionId", version, "versionNo", version);
    }

    private Map<String, Object> consent(String code, String version) {
        return Map.of("agreementCode", code, "versionNo", version);
    }
}
