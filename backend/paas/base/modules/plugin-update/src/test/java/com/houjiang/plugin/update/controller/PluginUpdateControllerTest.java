package com.houjiang.plugin.update.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import com.houjiang.plugin.update.exception.UpdateRejectedException;
import com.houjiang.plugin.update.model.InstallResult;
import com.houjiang.plugin.update.model.UpdateOutcome;
import com.houjiang.plugin.update.precheck.PrecheckResult;
import com.houjiang.plugin.update.risk.UpgradeRiskService;
import com.houjiang.plugin.update.service.PluginUpdateService;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 客户端组控制器测试。
 *
 * <p>重点覆盖三件事：
 * <ol>
 *   <li><b>未登录一律 3001</b>（安装是写操作，必须确认是本人在操作）；</li>
 *   <li><b>身份以登录态为准</b>：请求体里塞 instanceId 不会被采用；</li>
 *   <li><b>失败时 data 为 null 且错误码原样透传</b>（前端按 code 分支，不能被统一成 500）。</li>
 * </ol>
 *
 * @author Amos
 * @since M3
 */
class PluginUpdateControllerTest {

    private PluginUpdateService updateService;
    private UpgradeRiskService upgradeRiskService;

    private PluginUpdateController controller;

    @BeforeEach
    void setUp() {
        updateService = mock(PluginUpdateService.class);
        upgradeRiskService = mock(UpgradeRiskService.class);
        controller = new PluginUpdateController(updateService, upgradeRiskService);
        login("org-1", "100");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 未登录返回3001() {
        SecurityContextHolder.clearContext();

        ResponseResult<Object> result = controller.install(Map.of("pluginCode", "demo"), null);

        assertEquals(PluginDistErrorCode.NOT_LOGIN, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void 安装成功返回结果() {
        when(updateService.install(any(), anyString(), any(), any(), any()))
                .thenReturn(new InstallResult("upd-1", "demo", "2.0.0", "SUCCESS", "HEALTH", "/tmp/demo.jar", 1024, "md5"));

        ResponseResult<Object> result = controller.install(Map.of("pluginCode", "demo", "version", "2.0.0"), null);

        assertEquals(1, result.getCode());
        assertNotNull(result.getData());
        InstallResult data = (InstallResult) result.getData();
        assertEquals("upd-1", data.getTaskId());
        assertEquals("SUCCESS", data.getState());
    }

    @Test
    void 请求体里的身份字段被忽略以登录态为准() {
        when(updateService.install(any(), anyString(), any(), any(), any()))
                .thenReturn(new InstallResult("upd-1", "demo", "2.0.0", "SUCCESS", "HEALTH", "/tmp/demo.jar", 1, "md5"));

        controller.install(Map.of("pluginCode", "demo", "instanceId", "org-hacker", "userId", 999), null);

        ArgumentCaptor<PluginIdentity> captor = ArgumentCaptor.forClass(PluginIdentity.class);
        verify(updateService).install(captor.capture(), eq("demo"), isNull(), any(), any());
        assertEquals("org-1", captor.getValue().getInstanceId(), "必须采用登录态的站点实例而不是请求体里的值");
        assertEquals(100L, captor.getValue().getUserId(), "必须采用登录态的用户而不是请求体里的值");
    }

    @Test
    void 预检不通过返回200且pass为false携带原因() {
        when(updateService.precheck("demo", "2.0.0"))
                .thenReturn(PrecheckResult.fail(PluginDistErrorCode.INCOMPATIBLE_FRAMEWORK,
                        "当前框架版本 1.0.0 低于插件要求的最低版本 2.0"));

        ResponseResult<Object> result = controller.precheck(Map.of("pluginCode", "demo", "version", "2.0.0"));

        assertEquals(1, result.getCode());
        PrecheckResult data = (PrecheckResult) result.getData();
        assertFalse(data.isPass());
        assertEquals(PluginDistErrorCode.INCOMPATIBLE_FRAMEWORK, data.getReason());
        assertTrue(data.getDetail().contains("2.0"));
    }

    @Test
    void 预检缺少插件编码直接拒绝() {
        ResponseResult<Object> result = controller.precheck(Map.of());

        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, result.getCode());
    }

    @Test
    void 安装被拒绝时错误码原样透传且data为null() {
        when(updateService.install(any(), anyString(), any(), any(), any()))
                .thenThrow(new UpdateRejectedException(PluginDistErrorCode.DEPENDENCY_MISSING,
                        "依赖插件 erp 未安装，请先安装它再安装本插件"));

        ResponseResult<Object> result = controller.install(Map.of("pluginCode", "demo"), null);

        assertEquals(PluginDistErrorCode.DEPENDENCY_MISSING, result.getCode());
        assertNull(result.getData());
        assertTrue(result.getMessage().contains("erp"));
    }

    @Test
    void 任务不存在返回错误码() {
        when(updateService.taskStatus("upd-404", null, "org-1")).thenReturn(null);

        ResponseResult<Object> result = controller.taskStatus(Map.of("taskId", "upd-404"));

        assertEquals(PluginDistErrorCode.ARTIFACT_NOT_FOUND, result.getCode());
    }

    /**
     * 写入登录态。
     *
     * @param orgId  组织 ID（站点实例）
     * @param userId 用户 ID
     */
    private static void login(String orgId, String userId) {
        UserInfoVO user = new UserInfoVO();
        user.setUserId(userId);
        user.setOrgId(orgId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, java.util.List.of()));
    }

    // ------------------------------------------------------------------
    // M4：升级 / 回滚端点
    // ------------------------------------------------------------------

    @Test
    void 升级成功返回任务结果() {
        when(updateService.upgrade(any(PluginIdentity.class), eq("demo"), eq("2.0.0"), any(), any()))
                .thenReturn(new UpdateOutcome("upd-1", "demo", "UPGRADE", "1.0.0", "2.0.0", "2.0.0",
                        "SUCCESS", "HEALTH", false, null, null));

        ResponseResult<Object> result =
                controller.upgrade(Map.of("pluginCode", "demo", "targetVersion", "2.0.0"), null);

        assertEquals(1, result.getCode(), "ResponseResult.success 的约定成功码为 1");
        UpdateOutcome data = (UpdateOutcome) result.getData();
        assertEquals("2.0.0", data.getCurrentVersion());
        assertFalse(data.isRolledBack());
    }

    @Test
    void 升级被拒时错误码原样透传() {
        when(updateService.upgrade(any(PluginIdentity.class), any(), any(), any(), any()))
                .thenThrow(new UpdateRejectedException(PluginDistErrorCode.VERSION_CONFLICT, "目标版本必须高于当前版本"));

        ResponseResult<Object> result =
                controller.upgrade(Map.of("pluginCode", "demo", "targetVersion", "1.0.0"), null);

        assertEquals(PluginDistErrorCode.VERSION_CONFLICT, result.getCode(),
                "3012 必须原样透传：前端靠它提示「版本不合法」，统一成 500 就没法分支了");
        assertNull(result.getData());
    }

    @Test
    void 回滚以登录态身份执行且豁免授权() {
        when(updateService.rollback(eq("demo"), isNull(), eq("org-1"), eq(100L)))
                .thenReturn(new UpdateOutcome("upd-2", "demo", "ROLLBACK", "2.0.0", "1.0.0", "1.0.0",
                        "ROLLED_BACK", "STOP", true, null, null));

        ResponseResult<Object> result = controller.rollback(Map.of("pluginCode", "demo"));

        assertEquals(1, result.getCode());
        UpdateOutcome data = (UpdateOutcome) result.getData();
        assertTrue(data.isRolledBack());
        // 站点实例与操作人只能来自登录态（org-1 / 100），请求体里塞什么都不好使
        verify(updateService).rollback("demo", null, "org-1", 100L);
    }

    @Test
    void 回滚点缺失返回3018() {
        when(updateService.rollback(anyString(), any(), anyString(), any()))
                .thenThrow(new UpdateRejectedException(PluginDistErrorCode.ROLLBACK_POINT_MISSING, "无回滚锚点"));

        ResponseResult<Object> result = controller.rollback(Map.of("pluginCode", "demo"));

        assertEquals(PluginDistErrorCode.ROLLBACK_POINT_MISSING, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void 未登录不能回滚() {
        SecurityContextHolder.clearContext();

        ResponseResult<Object> result = controller.rollback(Map.of("pluginCode", "demo"));

        assertEquals(PluginDistErrorCode.NOT_LOGIN, result.getCode());
    }
}
