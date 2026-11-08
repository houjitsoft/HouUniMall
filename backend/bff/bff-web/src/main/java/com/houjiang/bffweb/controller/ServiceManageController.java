package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.support.AdminOpsSupport;
import com.houjiang.bffweb.support.OpsScriptPaths;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Java 微服务管理（IDEA Services 风格）
/**
 *
 * <p>供 cloud 管理后台「服务管理」页面使用，通过 {@code jar/service-manage.ps1} 查询/启停本机 jar 进程。
 *
 * <ul>
 *   <li>GET  /web/admin/services              服务列表（状态/端口/PID）</li>
 *   <li>POST /web/admin/services/{name}/start  启动单个服务</li>
 *   <li>POST /web/admin/services/{name}/stop   停止单个服务</li>
 *   <li>POST /web/admin/services/{name}/restart 重启单个服务</li>
 *   <li>GET  /web/admin/services/{name}/status  单服务状态（轻量轮询）</li>
 *   <li>GET  /web/admin/services/{name}/log    读取启动日志 tail</li>
 *   <li>POST /web/admin/services/start-all     启动全部</li>
 *   <li>POST /web/admin/services/stop-all      停止全部</li>
 * </ul>
 *
 * <p>鉴权：{@code X-Admin-Token}，与 restart.admin.token 一致。
 * @author Amos
 */
@RestController
@RequestMapping("/web/admin/services")
public class ServiceManageController {

    @Value("${restart.admin.token}")
    private String adminToken;

    @Value("${restart.service.script.path:D:/wwwroot/houcloud/jar/service-manage.ps1}")
    private String serviceScriptPath;

    /**
     * 获取全部 jar 服务状态列表。
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> list(HttpServletRequest req) {
        verify(req);
        return wrap(runScript(120, "-Action", "list"));
    }

    /**
     * 启动指定服务。
/**
     *
     * @param name 服务名（不含 .jar）
     */
    @PostMapping(value = "/{name}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> start(@PathVariable String name, HttpServletRequest req) {
        verify(req);
        validateName(name);
        return wrap(runScript(180, "-Action", "start", "-Name", name));
    }

    /**
     * 停止指定服务。
/**
     *
     * @param name 服务名
     */
    @PostMapping(value = "/{name}/stop", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> stop(@PathVariable String name, HttpServletRequest req) {
        verify(req);
        validateName(name);
        return wrap(runScript(120, "-Action", "stop", "-Name", name));
    }

    /**
     * 重启指定服务。
/**
     *
     * @param name 服务名
     */
    @PostMapping(value = "/{name}/restart", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> restart(@PathVariable String name, HttpServletRequest req) {
        verify(req);
        validateName(name);
        return wrap(runScript(600, "-Action", "restart", "-Name", name));
    }

    /**
     * 查询单个服务当前状态（供前端启动后轮询，避免全量 list）。
/**
     *
     * @param name 服务名
     */
    @GetMapping(value = "/{name}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> status(@PathVariable String name, HttpServletRequest req) {
        verify(req);
        validateName(name);
        return wrap(runScript(30, "-Action", "status", "-Name", name));
    }

    /**
     * 读取服务启动日志（stdout + stderr tail）。
/**
     *
     * @param name 服务名
     * @param tail 行数，默认 200
     */
    @GetMapping(value = "/{name}/log", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> log(@PathVariable String name,
                                   @RequestParam(defaultValue = "200") int tail,
                                   HttpServletRequest req) {
        verify(req);
        validateName(name);
        return wrap(runScript(60, "-Action", "log", "-Name", name, "-Tail", String.valueOf(tail)));
    }

    /**
     * 启动 jar 目录下全部服务。
     */
    @PostMapping(value = "/start-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> startAll(HttpServletRequest req) {
        verify(req);
        return wrap(runScript(600, "-Action", "start-all"));
    }

    /**
     * 停止 jar 目录下全部服务。
     */
    @PostMapping(value = "/stop-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> stopAll(HttpServletRequest req) {
        verify(req);
        return wrap(runScript(300, "-Action", "stop-all"));
    }

    private void verify(HttpServletRequest req) {
        AdminOpsSupport.verifyToken(req, adminToken);
    }

    private void validateName(String name) {
        if (name == null || !name.matches("[a-zA-Z0-9\\-]+")) {
            throw new RuntimeException("非法服务名: " + name);
        }
    }

    private Map<String, Object> runScript(int timeoutSec, String... args) {
        String scriptPath = OpsScriptPaths.resolve(serviceScriptPath, "jar/service-manage.ps1");
        File script = new File(scriptPath);
        if (!script.isFile()) {
            throw new RuntimeException("服务管理脚本不存在: " + scriptPath
                    + "（Linux 请确认 jar/service-manage.ps1 存在，或设置 RESTART_SERVICE_SCRIPT_PATH）");
        }
        try {
            return AdminOpsSupport.runPowerShellJson(scriptPath, timeoutSec, args);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("执行服务管理脚本失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> wrap(Map<String, Object> scriptResult) {
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("msg", scriptResult.getOrDefault("message", "ok"));
        r.put("data", scriptResult.get("data"));
        return r;
    }
}
