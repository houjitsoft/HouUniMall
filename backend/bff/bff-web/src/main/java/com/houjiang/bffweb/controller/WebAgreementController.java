package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.BaseServiceFeign;
import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.service.AgreementConsentService;
import com.houjiang.bffweb.util.CommonStatusEnum;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册协议与同意记录 Web 端 BFF 适配器。
 *
 * <p>聚合 base（协议定义 + 生效版本）与 userms（用户同意记录）两侧能力，
 * 对前端暴露统一入口：注册页协议清单、待同意协议、重新同意、后台同意记录查询与导出。</p>
 *
 * @author Amos
 */
@RestController
@RequestMapping("/web/agreement")
public class WebAgreementController {

    private static final Logger log = LoggerFactory.getLogger(WebAgreementController.class);

    @Autowired
    private BaseServiceFeign baseServiceFeign;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private AgreementConsentService agreementConsentService;

    // ================= 注册页 / 登录页（免登录） =================

    /**
     * 按场景查询启用中的协议及其当前生效版本正文。
     *
     * <p>注册页首次加载即调用，用于渲染三项协议的勾选框与正文弹窗。</p>
     */
    @GetMapping("/list")
    public ResponseResult list(@RequestParam(value = "scene", required = false) String scene) {
        String targetScene = (scene == null || scene.isBlank())
                ? AgreementConsentService.SCENE_REGISTER : scene.trim();
        Map<String, Object> param = new HashMap<>(2);
        param.put("scene", targetScene);
        ResponseResult result = baseServiceFeign.agreementList(param);
        if (result == null || result.getCode() != CommonStatusEnum.SUCCESS.getCode()) {
            // 兜底为空列表：前端回退到内置文案，注册页不因协议服务异常而白屏
            return ResponseResult.success(Collections.emptyList());
        }
        return result;
    }

    /**
     * 查询当前登录用户待重新同意的协议（版本更新后登录即提示补签）。
     */
    @PostMapping("/pending")
    public ResponseResult pending(@RequestBody(required = false) Map<String, Object> param,
                                  HttpServletRequest request) {
        String token = resolveToken(request);
        String userId = resolveUserId(token);
        if (userId == null || userId.isEmpty()) {
            return ResponseResult.fail(401, "登录状态已失效，请重新登录");
        }
        String scene = param == null || param.get("scene") == null
                ? "" : String.valueOf(param.get("scene")).trim();
        List<Map<String, Object>> pending;
        if (scene.isEmpty() || "login".equalsIgnoreCase(scene) || "all".equalsIgnoreCase(scene)) {
            // 默认只处理站点运营者面向站内用户配置的协议；安装 EULA 不进入登录流程。
            pending = agreementConsentService.listPendingForLogin(userId);
        } else {
            pending = agreementConsentService.listPendingAgreements(userId, scene);
        }
        // null = 协议目录或同意记录查询失败。禁止当成「从未同意」返回全量 pending，
        // 否则已确认用户会在依赖抖动时反复被弹补签窗。
        if (pending == null) {
            return ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "协议状态查询失败，请稍后重试");
        }

        long requiredCount = 0;
        for (Map<String, Object> item : pending) {
            Object required = item.get("required");
            if (required != null && "1".equals(String.valueOf(required).trim())) {
                requiredCount++;
            }
        }
        Map<String, Object> data = new HashMap<>(4);
        data.put("pending", pending);
        data.put("pendingCount", pending.size());
        data.put("requiredCount", requiredCount);
        return ResponseResult.success(data);
    }

    /**
     * 版本更新后重新同意：校验版本并留痕。
     */
    @PostMapping("/reagree")
    public ResponseResult reagree(@RequestBody(required = false) Map<String, Object> param,
                                  HttpServletRequest request) {
        String token = resolveToken(request);
        String userId = resolveUserId(token);
        if (userId == null || userId.isEmpty()) {
            return ResponseResult.fail(401, "登录状态已失效，请重新登录");
        }
        Object agreements = param == null ? null : param.get("agreements");
        // 登录后只补签站点运营者配置的 register 协议。
        AgreementConsentService.ValidateResult result = agreementConsentService.validateAndRecordLoginPending(
                userId,
                resolveUsername(token),
                agreements,
                getClientIp(request),
                request.getHeader("User-Agent"),
                (param == null ? null : String.valueOf(param.getOrDefault("source", "web"))));
        if (!result.isOk()) {
            return ResponseResult.error(result.getMessage());
        }
        Map<String, Object> data = new HashMap<>(4);
        data.put("message", "已记录你的同意");
        return ResponseResult.success(data);
    }

    // ================= 后台：协议同意记录 =================

    /**
     * 后台：同意记录分页查询（按用户、协议类型、时间范围）
     */
    @PostMapping("/consent/page")
    public ResponseResult consentPage(@RequestBody(required = false) Map<String, Object> param) {
        return userServiceFeign.agreementConsentPage(param == null ? new HashMap<>() : param);
    }

    /**
     * 后台：同意记录导出（返回筛选后的全量数据，由前端生成 Excel）
     */
    @PostMapping("/consent/export")
    public ResponseResult consentExport(@RequestBody(required = false) Map<String, Object> param) {
        return userServiceFeign.agreementConsentExport(param == null ? new HashMap<>() : param);
    }

    /**
     * 后台：查询某用户的同意记录
     */
    @PostMapping("/consent/listByUser")
    public ResponseResult consentListByUser(@RequestBody(required = false) Map<String, Object> param) {
        return userServiceFeign.agreementConsentListByUser(param == null ? new HashMap<>() : param);
    }

    // ================= 后台：协议定义 =================

    /**
     * 后台：协议定义分页
     */
    @PostMapping("/admin/page")
    public ResponseResult adminPage(@RequestBody(required = false) Map<String, Object> param) {
        return baseServiceFeign.agreementAdminPage(param == null ? new HashMap<>() : param);
    }

    /**
     * 后台：新增或更新协议定义
     */
    @PostMapping("/admin/save")
    public ResponseResult adminSave(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.agreementAdminSave(param);
    }

    /**
     * 后台：删除协议定义（连同全部版本）
     */
    @PostMapping("/admin/delete")
    public ResponseResult adminDelete(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.agreementAdminDelete(param);
    }

    // ================= 后台：协议版本 =================

    /**
     * 后台：查询某协议的版本列表
     */
    @PostMapping("/version/list")
    public ResponseResult versionList(@RequestBody(required = false) Map<String, Object> param) {
        return baseServiceFeign.agreementVersionList(param == null ? new HashMap<>() : param);
    }

    /**
     * 后台：新增或更新协议版本（草稿）
     */
    @PostMapping("/version/save")
    public ResponseResult versionSave(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.agreementVersionSave(param);
    }

    /**
     * 后台：发布协议版本
     */
    @PostMapping("/version/publish")
    public ResponseResult versionPublish(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.agreementVersionPublish(param);
    }

    /**
     * 后台：下线协议版本
     */
    @PostMapping("/version/offline")
    public ResponseResult versionOffline(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.agreementVersionOffline(param);
    }

    /**
     * 后台：删除协议版本
     */
    @PostMapping("/version/delete")
    public ResponseResult versionDelete(@RequestBody Map<String, Object> param) {
        return baseServiceFeign.agreementVersionDelete(param);
    }

    // ================= 内部工具 =================

    /**
     * 从 Authorization 头解析 token（去掉 Bearer 前缀）。
     */
    public static String resolveToken(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            authorization = request.getHeader("token");
        }
        if (authorization == null) {
            return "";
        }
        String token = authorization.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }
        return token;
    }

    /**
     * 依据 token 解析当前登录用户ID。
     *
     * @param token 登录 token
     * @return userId；解析失败返回 null
     */
    @SuppressWarnings("unchecked")
    public String resolveUserId(String token) {
        Map<String, Object> user = resolveUser(token);
        if (user == null) {
            return null;
        }
        Object userId = user.get("userId");
        if (userId == null) {
            userId = user.get("user_id");
        }
        return userId == null ? null : String.valueOf(userId);
    }

    /**
     * 依据 token 解析当前登录用户信息。
     *
     * <p><b>这里必须用 /queryUserInfo，不能用 /information/getUserInfo。</b>
     * 后者返回的是 userms 的 {@code UserInfoForUpdateVO}（昵称/手机/头像/组织等），
     * <b>整张 VO 里没有 userId 字段</b>；而 {@link #resolveUserId} 只能从 userId / user_id
     * 取值，取不到就返回 401「登录状态已失效，请重新登录」。该消息又命中前端
     * request.js 的未登录判定（含「重新登录」），于是清 token + 强跳登录页 ——
     * 表现为「每个已登录用户一进后台就被踢回登录页」。
     * /queryUserInfo 返回的 UserInfoVO 明确带 userId，故改用它。</p>
     *
     * <p>两者都由 userms 依据请求上下文里的 token 解析，无需在请求体传 token；
     * 请求体仍带上 token 只为兼容可能按 body 取 token 的实现。</p>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveUser(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            Map<String, Object> param = new HashMap<>(2);
            param.put("token", token);
            ResponseResult result = userServiceFeign.getUserInfo(param);
            if (result == null || result.getCode() != CommonStatusEnum.SUCCESS.getCode()
                    || !(result.getData() instanceof Map)) {
                return null;
            }
            return (Map<String, Object>) result.getData();
        } catch (Exception e) {
            log.warn("解析登录用户失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 用户名（登录名/手机号），用于同意记录冗余检索
     */
    public String resolveUsername(String token) {
        Map<String, Object> user = resolveUser(token);
        if (user == null) {
            return "";
        }
        for (String key : new String[]{"userName", "user_name", "userMobile", "user_mobile"}) {
            Object value = user.get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    /**
     * 取客户端真实 IP（多级代理取第一个）
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        List<String> headers = new ArrayList<>();
        headers.add("X-Forwarded-For");
        headers.add("X-Real-IP");
        headers.add("Proxy-Client-IP");
        headers.add("WL-Proxy-Client-IP");
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
                if (value.contains(",")) {
                    return value.split(",")[0].trim();
                }
                return value.trim();
            }
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "" : remote;
    }
}
