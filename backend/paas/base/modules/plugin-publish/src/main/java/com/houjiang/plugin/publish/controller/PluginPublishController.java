package com.houjiang.plugin.publish.controller;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.plugin.artifact.exception.ArtifactStorageException;
import com.houjiang.plugin.publish.config.PluginPublishProperties;
import com.houjiang.plugin.publish.exception.PluginPublishException;
import com.houjiang.plugin.publish.model.UploadCommand;
import com.houjiang.plugin.publish.model.UploadResult;
import com.houjiang.plugin.publish.model.VersionListItem;
import com.houjiang.plugin.publish.service.PluginArtifactUploadService;
import com.houjiang.plugin.publish.service.PluginPublishService;
import com.houjiang.plugin.publish.state.PluginVersionStateMachine;
import com.houjiang.plugin.signature.ArtifactSignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 插件发布接口（PaaS 内部 {@code /base/plugin-center/publish/**}，对外经 BFF {@code /web/plugin-center/publish/**} 暴露）。
 *
 * <p>M1 只开放 6 个接口：upload / submit / review / offline / relist / list；
 * {@code versions/resign}（密钥轮换重签）依赖安全管理员角色与权限模型，挪到 M2。
 *
 * <p>响应统一 {@code ResponseResult{code, data, message}}，错误码取 {@code PluginDistErrorCode}（3001-3018）。
 *
 * @author Amos
 * @since M1
 */
@Slf4j
@RestController
@RequestMapping("/base/plugin-center/publish")
public class PluginPublishController {

    /**
     * 平台管理员权限码：由 mall 的 {@code MallAdminTokenAuthenticationFilter}
     * 在 bbc 后台登录时写入 SecurityContext，与商城其它管理端接口用的是同一套约定。
     */
    private static final String ROLE_MALL_ADMIN = "ROLE_MALL_ADMIN";

    private final PluginArtifactUploadService uploadService;

    private final PluginPublishService publishService;

    private final PluginPublishProperties publishProperties;

    /** 开发者身份服务（可选）：用于发布主体门禁，见 {@link #developerGate(String)}。 */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.houjiang.mall.service.market.PluginDeveloperService developerService;

    /**
     * 构造控制器。
     *
     * @param uploadService     上传服务
     * @param publishService    发布流程服务
     * @param publishProperties 发布配置（控制是否只允许管理员调用）
     */
    public PluginPublishController(PluginArtifactUploadService uploadService,
                                   PluginPublishService publishService,
                                   PluginPublishProperties publishProperties) {
        this.uploadService = uploadService;
        this.publishService = publishService;
        this.publishProperties = publishProperties;
    }

    /**
     * 平台管理员闸门（受 {@code houcloud.plugin.admin-only} 控制，默认开）。
     *
     * @return 通过时返回 null，被拦截时返回可直接响应的失败结果
     */
    private ResponseResult adminGate() {
        if (publishProperties == null || !publishProperties.isAdminOnly()) {
            return null;
        }
        // 超管会话的权限集是字面 ["*"]（没有任何 RBAC 角色），精确比对单项权限必拒
        // （2026-10-27 OCR 403 同款坑）。对齐 mall 模块 hasAnyAuthority('ROLE_MALL_ADMIN','*')
        // 的语义：角色命中或显式通配，任一放行。
        if (SecurityContextUtils.hasPermission(ROLE_MALL_ADMIN)
                || SecurityContextUtils.hasPermission("*")
                || SecurityContextUtils.hasPermission("*:*")) {
            return null;
        }
        return ResponseResult.fail(403, "仅平台管理员可执行该操作");
    }

    /**
     * 开发者身份门禁：{@code strict} 模式下未通过审核 / 超配额直接拒绝上传。
     *
     * <p>用 {@code required=false} 注入：plugin-publish 依赖 plugin-market，
     * 正常情况下 Bean 一定在；但单测与裁剪部署里可能没有，缺了就不做开发者校验，
     * 与 {@code enforcement=off} 等价。
     *
     * @param pluginCode 目标插件编码（配额判定用：发已有插件的新版本不占新配额）
     * @return 通过返回 null，被拦截返回失败结果
     */
    private ResponseResult developerGate(String pluginCode) {
        if (developerService == null) {
            return null;
        }
        try {
            developerService.assertCanPublish(currentUserId(), pluginCode);
            return null;
        } catch (IllegalStateException e) {
            return ResponseResult.fail(403, e.getMessage());
        } catch (RuntimeException e) {
            // 开发者数据不可用不能把上传整体打死（warn 模式本就放行）
            log.warn("开发者门禁校验异常，按放行处理: {}", e.getMessage());
            return null;
        }
    }

    /** 当前登录用户 ID（发布主体），解析不出返回 null */
    private Long currentUserId() {
        var user = SecurityContextUtils.getCurrentUserOrNull();
        if (user == null || user.getUserId() == null) {
            return null;
        }
        try {
            return Long.valueOf(user.getUserId().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 上传制品：校验 → 落存储 → 重算摘要 → 签名 → 落库（status=draft）。
     *
     * <p>入参 {@code uploadId} 为客户端幂等键，重传同值直接返回首次结果。
     */
    @PostMapping("/upload")
    public ResponseResult upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam("pluginCode") String pluginCode,
                                 @RequestParam("version") String version,
                                 @RequestParam("uploadId") String uploadId,
                                 @RequestParam(value = "changelog", required = false) String changelog,
                                 @RequestParam(value = "createdBy", required = false) Long createdBy) {
        ResponseResult denied = adminGate();
        if (denied != null) {
            return denied;
        }
        // 开发者身份门禁（审计 2026-10-27 缺失能力 ①）：发布主体必须可追溯。
        // 强度由 houcloud.plugin.developer.enforcement 控制（off/warn/strict，默认 warn），
        // 默认不打断现有平台管理员发布流程；要收口时切成 strict 即可。
        ResponseResult developerDenied = developerGate(pluginCode);
        if (developerDenied != null) {
            return developerDenied;
        }
        try {
            if (file == null || file.isEmpty()) {
                return ResponseResult.fail(PluginDistErrorCode.MANIFEST_INVALID, "上传文件为空");
            }
            UploadCommand command = new UploadCommand();
            command.setPluginCode(pluginCode);
            command.setVersion(version);
            command.setUploadId(uploadId);
            command.setChangelog(changelog);
            command.setCreatedBy(createdBy);
            command.setJarStream(file.getInputStream());
            UploadResult result = uploadService.upload(command);
            return ResponseResult.success(result);
        } catch (PluginPublishException e) {
            // 带字段级校验结果时一并返回，供前端逐字段提示
            if (e.getValidations() != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", e.getMessage());
                data.put("validations", e.getValidations().getIssues());
                return ResponseResult.fail(e.getCode(), e.getMessage(), data);
            }
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (PluginVersionStateMachine.PluginStateTransitionException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (ArtifactStorageException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (ArtifactSignatureException e) {
            return ResponseResult.fail(PluginDistErrorCode.SIGNATURE_INVALID, e.getMessage());
        } catch (Exception e) {
            log.error("插件制品上传失败", e);
            return ResponseResult.fail(500, "插件制品上传失败: " + e.getMessage());
        }
    }

    /**
     * 提交审核：draft/rejected → pending。
     */
    @PostMapping("/versions/submit")
    public ResponseResult submit(@RequestBody(required = false) Map<String, Object> params) {
        ResponseResult denied = adminGate();
        if (denied != null) {
            return denied;
        }
        try {
            return ResponseResult.success(publishService.submit(
                    text(params, "pluginCode"), text(params, "version")));
        } catch (PluginPublishException | PluginVersionStateMachine.PluginStateTransitionException e) {
            return ResponseResult.fail(extractCode(e), e.getMessage());
        } catch (Exception e) {
            log.error("提交版本审核失败", e);
            return ResponseResult.fail(500, "提交版本审核失败: " + e.getMessage());
        }
    }

    /**
     * 审核：pending → published / rejected。
     */
    @PostMapping("/versions/review")
    public ResponseResult review(@RequestBody(required = false) Map<String, Object> params) {
        ResponseResult denied = adminGate();
        if (denied != null) {
            return denied;
        }
        try {
            Long versionId = longValue(params, "versionId");
            String decision = text(params, "decision");
            String note = text(params, "note");
            Long reviewerId = longValue(params, "reviewerId");
            return ResponseResult.success(publishService.review(versionId, decision, note, reviewerId));
        } catch (PluginPublishException | PluginVersionStateMachine.PluginStateTransitionException e) {
            return ResponseResult.fail(extractCode(e), e.getMessage());
        } catch (Exception e) {
            log.error("版本审核失败", e);
            return ResponseResult.fail(500, "版本审核失败: " + e.getMessage());
        }
    }

    /**
     * 下架：published → offline。
     */
    @PostMapping("/versions/offline")
    public ResponseResult offline(@RequestBody(required = false) Map<String, Object> params) {
        ResponseResult denied = adminGate();
        if (denied != null) {
            return denied;
        }
        try {
            return ResponseResult.success(publishService.offline(
                    text(params, "pluginCode"), text(params, "version"), text(params, "reason")));
        } catch (PluginPublishException | PluginVersionStateMachine.PluginStateTransitionException e) {
            return ResponseResult.fail(extractCode(e), e.getMessage());
        } catch (Exception e) {
            log.error("版本下架失败", e);
            return ResponseResult.fail(500, "版本下架失败: " + e.getMessage());
        }
    }

    /**
     * 重新上架：offline → published（仅原版本，不可换制品）。
     */
    @PostMapping("/versions/relist")
    public ResponseResult relist(@RequestBody(required = false) Map<String, Object> params) {
        ResponseResult denied = adminGate();
        if (denied != null) {
            return denied;
        }
        try {
            return ResponseResult.success(publishService.relist(
                    text(params, "pluginCode"), text(params, "version")));
        } catch (PluginPublishException | PluginVersionStateMachine.PluginStateTransitionException e) {
            return ResponseResult.fail(extractCode(e), e.getMessage());
        } catch (Exception e) {
            log.error("版本重新上架失败", e);
            return ResponseResult.fail(500, "版本重新上架失败: " + e.getMessage());
        }
    }

    /**
     * 版本列表。<b>不返回 jar_url</b>（方案 §4.1）。
     */
    @PostMapping("/versions/list")
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> params) {
        try {
            List<VersionListItem> items = publishService.list(
                    text(params, "pluginCode"), intValue(params, "status"));
            return ResponseResult.success(items);
        } catch (PluginPublishException e) {
            return ResponseResult.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询版本列表失败", e);
            return ResponseResult.fail(500, "查询版本列表失败: " + e.getMessage());
        }
    }

    private static int extractCode(RuntimeException e) {
        if (e instanceof PluginPublishException publishException) {
            return publishException.getCode();
        }
        if (e instanceof PluginVersionStateMachine.PluginStateTransitionException transition) {
            return transition.getCode();
        }
        return 500;
    }

    private static String text(Map<String, Object> params, String key) {
        if (params == null) {
            return null;
        }
        Object value = params.get(key);
        return value == null ? null : value.toString().trim();
    }

    private static Long longValue(Map<String, Object> params, String key) {
        String text = text(params, key);
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer intValue(Map<String, Object> params, String key) {
        String text = text(params, key);
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
