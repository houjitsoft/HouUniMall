package com.houjiang.bffweb.controller;

import lombok.extern.slf4j.Slf4j;
import com.houjiang.bffweb.util.ResponseResult;
import com.houjiang.bffweb.feign.OaServiceFeign;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 工作台Web端BFF适配器
/**
 * BFF 层对 submenus 加本地缓存（10分钟），减少对 OA 服务的重复 Feign 调用
 * @author Amos
 */
@RestController
@RequestMapping("/web/workbench")
@Slf4j
public class WebWorkbenchController {

    @Autowired
    private OaServiceFeign oaServiceFeign;

    @Autowired
    private HttpServletRequest request;

    /** BFF 层子菜单缓存 TTL（10分钟） */
    private static final long SUBMENUS_CACHE_TTL_MS = 10 * 60 * 1000L;
    private final AtomicReference<ResponseResult> subMenusCache = new AtomicReference<>(null);
    private final AtomicLong subMenusCacheExpireAt = new AtomicLong(0L);

    /**
     * 获取工作台菜单数�?
/**
     * 动态调�?docs/sql/permission.sql �?pid=260 的内�?
     */
    @GetMapping("/menus")
    public ResponseResult getWorkbenchMenus() {
        ResponseResult authCheck = checkAuthorization();
        if (authCheck != null) {
            return authCheck;
        }
        try {
            return oaServiceFeign.getWorkbenchMenus();
        } catch (Exception e) {
            log.error("获取工作台菜单失败", e);
            return ResponseResult.fail(500, "获取工作台菜单失败: " + simplifyError(e));
        }
    }

    /**
     * 获取工作台子菜单数据（GET）
/**
     * BFF 层加 10 分钟本地缓存，避免每次 Feign 调 OA 服务
     */
    @GetMapping("/submenus")
    public ResponseResult getWorkbenchSubMenusGet() {
        ResponseResult authCheck = checkAuthorization();
        if (authCheck != null) {
            return authCheck;
        }
        // BFF 层缓存命中直接返回
        long now = System.currentTimeMillis();
        ResponseResult cached = subMenusCache.get();
        if (cached != null && subMenusCacheExpireAt.get() > now) {
            return cached;
        }
        try {
            ResponseResult fresh = oaServiceFeign.getWorkbenchSubMenus();
            if (fresh != null && (fresh.getCode() == 1 || fresh.getCode() == 200)) {
                subMenusCache.set(fresh);
                subMenusCacheExpireAt.set(now + SUBMENUS_CACHE_TTL_MS);
            }
            return fresh;
        } catch (Exception e) {
            log.error("获取工作台子菜单失败", e);
            // 下游异常时返回旧缓存兜底，保证前端可用
            ResponseResult stale = subMenusCache.get();
            if (stale != null) {
                log.warn("工作台子菜单下游异常，返回旧缓存兜底");
                return stale;
            }
            return ResponseResult.fail(500, "获取工作台子菜单失败: " + simplifyError(e));
        }
    }

    /**
     * 获取工作台子菜单数据（POST）
     */
    @PostMapping("/submenus")
    public ResponseResult getWorkbenchSubMenusPost() {
        return getWorkbenchSubMenusGet();
    }

    /**
     * 获取工作台统计数�?
/**
     * 获取工作台相关的统计信息
     */
    @GetMapping("/stats")
    public ResponseResult getWorkbenchStats() {
        ResponseResult authCheck = checkAuthorization();
        if (authCheck != null) {
            return authCheck;
        }
        try {
            return oaServiceFeign.getWorkbenchStats();
        } catch (Exception e) {
            log.error("获取工作台统计失败", e);
            return ResponseResult.fail(500, "获取工作台统计失败: " + simplifyError(e));
        }
    }

    /**
     * 获取审批菜单数据
/**
     * 动态调取审批相关的菜单数据
     */
    @GetMapping("/approval/menus")
    public ResponseResult getApprovalMenus() {
        ResponseResult authCheck = checkAuthorization();
        if (authCheck != null) {
            return authCheck;
        }
        try {
            return oaServiceFeign.getApprovalMenus();
        } catch (Exception e) {
            log.error("获取审批菜单失败", e);
            return ResponseResult.fail(500, "获取审批菜单失败: " + simplifyError(e));
        }
    }

    /**
     * BFF 层先做请求头兜底校验：未携带 Authorization 时直接返回未登录，
/**
     * 避免下游异常被包装成通用失败信息。
     */
    private ResponseResult checkAuthorization() {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            authorization = request.getHeader("authorization");
        }
        if (authorization == null || authorization.isBlank()) {
            return ResponseResult.fail(11006, "用户未登录");
        }
        return null;
    }

    /**
     * 规整异常文案，避免把整段堆栈回传给前端。
     */
    private String simplifyError(Exception e) {
        if (e == null) {
            return "未知异常";
        }
        Throwable cause = e.getCause();
        String msg = e.getMessage();
        if ((msg == null || msg.isBlank()) && cause != null) {
            msg = cause.getMessage();
        }
        if (msg == null || msg.isBlank()) {
            return e.getClass().getSimpleName();
        }
        return msg.length() > 240 ? msg.substring(0, 240) : msg;
    }
}