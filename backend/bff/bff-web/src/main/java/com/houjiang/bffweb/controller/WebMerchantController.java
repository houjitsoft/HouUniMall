package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.MerchantServiceFeign;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Merchant商户服务BFF适配器
/**
 * 只做请求转发，不包含业务逻辑
 * 
 * @description 将前端请求 /web/merchant/** 转发到 merchant 微服务
 * @author Amos
 * @date 2026-04-28
 */
@Slf4j
@RestController
@RequestMapping("/web/merchant")
public class WebMerchantController {

    private static final String LOGIN_FAILURE_MSG = "登录失败，请稍后重试";
    private static final String SERVICE_UNAVAILABLE_MSG = "商户服务暂时不可用，请稍后重试";
    private static final String SERVICE_TIMEOUT_MSG = "商户服务请求超时，请稍后重试";

    private static final Set<String> ADMINAPI_MODULES = Set.of(
            "article.article",
            "article.articleCate",
            "auth.admin",
            "auth.menu",
            "auth.role",
            "channel.mnp_settings",
            "channel.official_account_menu",
            "channel.official_account_reply",
            "channel.official_account_setting",
            "channel.open_setting",
            "channel.web_page_setting",
            "crontab.crontab",
            "decorate.data",
            "decorate.page",
            "decorate.tabbar",
            "dept.dept",
            "dept.jobs",
            "notice.notice",
            "notice.sms_config",
            "recharge.recharge",
            "setting.dict.dict_data",
            "setting.dict.dict_type",
            "setting.hot_search",
            "setting.pay.pay_config",
            "setting.pay.pay_way",
            "setting.storage",
            "setting.system.cache",
            "setting.system.log",
            "setting.system.system",
            "setting.user.user",
            "setting.web.web_setting"
    );

    @Autowired
    private MerchantServiceFeign merchantServiceFeign;

    // ================= 登录认证接口 =================

    /**
     * 系统登录
/**
     *
     * @param params 登录参数，包含账号密码等（username/password/terminal）
     * @return Object 登录结果，成功时返回 merchant 微服务的 AjaxResult（code=1）；
     *               异常时统一返回 AjaxResult 风格的失败包（code=0），避免 Spring Boot
     *               BasicErrorController 黑盒 500 让前端拿不到 msg。
     */
    @PostMapping("/login/login")
    public Object login(@RequestBody Map<String, Object> params) {
        try {
            return merchantServiceFeign.login(params);
        } catch (Exception e) {
            log.error("商家登录调用失败\n{}", sanitizedStackTrace(e));

            String frontMsg = isTimeout(e)
                    ? SERVICE_TIMEOUT_MSG
                    : isServiceUnavailable(e) ? SERVICE_UNAVAILABLE_MSG : LOGIN_FAILURE_MSG;
            // 与 merchant 内部 AjaxResult.failed(code, msg, data, show) 保持同构
            // 前端 mall/bcenter 通过 res.code !== 1 判定失败并取 res.msg 展示
            Map<String, Object> fail = new HashMap<>();
            fail.put("code", 0);
            fail.put("msg", frontMsg);
            fail.put("data", new ArrayList<>());
            fail.put("show", 1);
            return fail;
        }
    }

    private static String sanitizedStackTrace(Throwable throwable) {
        StringBuilder rendered = new StringBuilder();
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = throwable;
        boolean causedBy = false;
        while (current != null) {
            if (!visited.add(current)) {
                rendered.append("Caused by: [cycle detected: ")
                        .append(current.getClass().getName())
                        .append("]\n");
                break;
            }
            if (causedBy) {
                rendered.append("Caused by: ");
            }
            rendered.append(current.getClass().getName()).append('\n');
            for (StackTraceElement frame : current.getStackTrace()) {
                rendered.append("\tat ").append(frame).append('\n');
            }
            causedBy = true;
            current = current.getCause();
        }
        return rendered.toString();
    }

    private static boolean isTimeout(Throwable throwable) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = throwable;
             current != null && visited.add(current);
             current = current.getCause()) {
            String className = current.getClass().getName();
            int status = current instanceof FeignException feignException
                    ? feignException.status() : -1;
            if (current instanceof SocketTimeoutException
                    || className.endsWith("TimeoutException")
                    || status == 408
                    || status == 504) {
                return true;
            }
        }
        return false;
    }

    private static boolean isServiceUnavailable(Throwable throwable) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = throwable;
             current != null && visited.add(current);
             current = current.getCause()) {
            String className = current.getClass().getName();
            String message = current.getMessage();
            int status = current instanceof FeignException feignException
                    ? feignException.status() : -1;
            if (status == 502
                    || status == 503
                    || current instanceof java.net.ConnectException
                    || className.contains("ServiceUnavailable")
                    || message != null && (message.contains("No instances available")
                    || message.contains("Load balancer does not contain"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 退出登录
/**
     * 
     * @param request HTTP请求对象，用于传递Authorization头
     * @return Object 退出结果
     */
    @PostMapping("/login/out")
    public Object logout(HttpServletRequest request) {
        return merchantServiceFeign.logout(request.getHeader("Authorization"));
    }

    // ================= 文件上传接口（multipart/form-data） =================

    /**
     * 通用上传转发（image/file/video）
/**
     *
     * <p>修复 415 Unsupported Media Type：通用 forwardPost 用 @RequestBody Map 接收 JSON，
     * 无法处理 multipart/form-data 的文件上传，需要独立声明 consumes 和 @RequestPart。
     *
     * @param type 上传类型（image/file/video，对应 merchant 的 /merchant/upload/{type}）
     * @param file 上传文件（前端 ElUpload 默认字段名为 file）
     * @param cid  分类 ID（可选，bff 层兜底为 "0" 后转发，避免下游 @RequestParam(required=true) 报 400）
     * @return Object merchant 端 AjaxResult
     */
    @PostMapping(value = "/upload/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object uploadFile(@PathVariable("type") String type,
                             @RequestPart("file") MultipartFile file,
                             @RequestPart(value = "cid", required = false) String cid,
                             HttpServletRequest request) {
        String safeCid = (cid == null || cid.isEmpty()) ? "0" : cid;
        String authorization = request.getHeader("Authorization");
        log.info("upload/{} - cid={}, authorizationPresent={}",
                type, safeCid, authorization != null && !authorization.isBlank());
        return merchantServiceFeign.uploadFile(type, file, safeCid, authorization);
    }

    // ================= 通用转发接口 =================

    /** Explicit seller advertising route kept separate from the generic compatibility proxy. */
    @PostMapping("/advertising/{action}")
    public Object advertising(@PathVariable("action") String action,
                              @RequestBody(required = false) Map<String, Object> params) {
        return merchantServiceFeign.advertising(action, params == null ? Map.of() : params);
    }

    /**
     * 判断 module 是否是 merchant 端明确声明的 /adminapi/* dotted alias。
/**
     * 未列入白名单的 module（包括带点 module）默认转发到 /merchant。
     *
     * @param module 请求 module 名
     * @return true 表示需走 /adminapi 路径
     */
    private static boolean isAdminapiModule(String module) {
        return ADMINAPI_MODULES.contains(module);
    }

    /**
     * 通用POST请求转发
/**
     * 用于处理未单独定义的merchant服务POST接口
     * 路径格式: /web/merchant/{module}/{action}
     *
     * <p>module 命中 ADMINAPI_MODULES 时转发到 /adminapi/{module}/{action}；
     * 其他 module 统一走 /merchant/{module}/{action}。
     *
     * @param module 模块名（如 mall.goods, order, auth.menu 等）
     * @param action 操作名（如 lists, detail, add 等）
     * @param params 请求参数
     * @return Object 响应结果
     */
    @PostMapping("/{module}/{action}")
    public Object forwardPost(@PathVariable("module") String module,
                              @PathVariable("action") String action,
                              @RequestBody(required = false) Map<String, Object> params) {
        if (isAdminapiModule(module)) {
            return merchantServiceFeign.forwardAdminapiPost(module, action, params);
        }
        return merchantServiceFeign.forwardPost(module, action, params);
    }

    /**
     * 通用GET请求转发
/**
     * 用于处理未单独定义的merchant服务GET接口
     * 路径格式: /web/merchant/{module}/{action}
     *
     * <p>智能路由见 forwardPost 说明。
     *
     * @param module 模块名（如 mall.goods, order, auth.menu 等）
     * @param action 操作名（如 lists, detail 等）
     * @param params 请求参数
     * @return Object 响应结果
     */
    @GetMapping("/{module}/{action}")
    public Object forwardGet(@PathVariable("module") String module,
                             @PathVariable("action") String action,
                             @RequestParam(required = false) Map<String, Object> params) {
        if (isAdminapiModule(module)) {
            return merchantServiceFeign.forwardAdminapiGet(module, action, params);
        }
        return merchantServiceFeign.forwardGet(module, action, params);
    }
}
