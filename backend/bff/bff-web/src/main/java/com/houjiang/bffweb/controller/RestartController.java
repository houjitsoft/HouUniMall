package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.support.OpsScriptPaths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 一键重启控制器
/**
 *
 * <p>提供给 unimall.houjit.com/restartall 页面使用，触发本机的 restart-all.ps1 完整重启流程。
 *
 * <ul>
 *   <li>POST /web/admin/restart        触发重启（detach 子进程，不会被自身停服杀死）
 *   <li>GET  /web/admin/restart/log    增量读取重启日志（tail）
 *   <li>GET  /web/admin/restart/status 查询重启是否已完成
 * </ul>
 *
 * <p>鉴权：必须携带 header {@code X-Admin-Token}，值与 bootstrap.yml 中
 * {@code restart.admin.token} 一致。
 * @author Amos
 */
@RestController
@RequestMapping("/web/admin/restart")
public class RestartController {

    @Value("${restart.admin.token}")
    private String adminToken;

    @Value("${restart.script.path:D:/wwwroot/houcloud/jar/restart-all.ps1}")
    private String scriptPath;

    @Value("${restart.frontend.script.path:D:/wwwroot/houcloud/jar/frontend-build.ps1}")
    private String frontendBuildScriptPath;

    @Value("${restart.log.dir:D:/wwwroot/houcloud/jar/restart-logs}")
    private String logDir;

    /** 触发完整重启或仅前端打包 */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> trigger(@RequestBody(required = false) Map<String, Object> body,
                                       HttpServletRequest req) {
        verifyToken(req);

        boolean frontendOnly  = bool(body, "frontendOnly",  false);

        String resolvedScriptPath = frontendOnly
                ? OpsScriptPaths.resolve(frontendBuildScriptPath, "jar/frontend-build.ps1")
                : OpsScriptPaths.resolve(scriptPath, "jar/restart-all.ps1");
        File scriptFile = new File(resolvedScriptPath);
        if (!scriptFile.exists() || !scriptFile.isFile()) {
            Map<String, Object> r = new HashMap<>();
            r.put("code", 500);
            r.put("msg", (frontendOnly ? "前端打包" : "重启") + "脚本不存在: " + resolvedScriptPath);
            return r;
        }
        // 预校验：日志目录必须存在或可创建，否则脚本即便启动也无处写日志
        String resolvedLogDir = resolveLogDirPath();
        File logDirFile = new File(resolvedLogDir);
        if (!logDirFile.exists() && !logDirFile.mkdirs()) {
            Map<String, Object> r = new HashMap<>();
            r.put("code", 500);
            r.put("msg", "重启日志目录不存在且无法创建: " + logDirFile.getAbsolutePath());
            return r;
        }

        String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        boolean skipFrontend = bool(body, "skipFrontend", false);
        boolean skipBackend  = bool(body, "skipBackend",  false);
        boolean backup       = bool(body, "backup",       false);
        boolean full         = bool(body, "full",         false);
        boolean forceRestart = bool(body, "forceRestart", false);
        boolean forceFrontend = bool(body, "forceFrontend", false);
        boolean runTests      = bool(body, "runTests", false);
        String testClasses    = validatedTestClasses(body);

        File log = new File(resolvedLogDir, "restart-all-" + stamp + ".log");
        List<String> cmd = new ArrayList<>();
        cmd.add("cmd.exe");
        cmd.add("/c");
        cmd.add("start");
        cmd.add(frontendOnly ? "\"houcloud-frontend-build\"" : "\"houcloud-restart\"");
        cmd.add("/B");
        cmd.add("powershell.exe");
        cmd.add("-NoProfile");
        cmd.add("-ExecutionPolicy");
        cmd.add("Bypass");
        cmd.add("-WindowStyle");
        cmd.add("Hidden");
        cmd.add("-File");
        cmd.add(resolvedScriptPath);
        cmd.add("-Stamp");
        cmd.add(stamp);

        if (frontendOnly) {
            appendFrontends(cmd, body);
        } else {
            if (skipFrontend) cmd.add("-SkipFrontend");
            if (skipBackend)  cmd.add("-SkipBackend");
            if (backup)       cmd.add("-Backup");
            if (full)         cmd.add("-NoIncremental");
            if (forceRestart) cmd.add("-ForceRestart");
            if (forceFrontend) cmd.add("-ForceFrontend");
            if (runTests) cmd.add("-RunTests");
            if (runTests && testClasses != null) {
                cmd.add("-TestClasses");
                cmd.add(testClasses);
            }
            appendFrontends(cmd, body);
            appendForceServices(cmd, body);
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmd).redirectErrorStream(true);
            // restart-all writes its durable output to restart-logs. The detached process must not
            // inherit an unread ProcessBuilder pipe, otherwise enough console output fills the
            // Windows pipe buffer and leaves the deployment holding restart-all.lock forever.
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.start();
        } catch (Exception e) {
            throw new RuntimeException("无法启动重启脚本: " + e.getMessage(), e);
        }

        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("msg", frontendOnly
                ? "前端打包任务已下发（独立脚本 jar/frontend-build.ps1，不会碰 bff-web / Java）"
                : "重启任务已下发。编译期间 bff-web 保持在线；仅最后替换网关 jar 时约 10～30 秒可能 502，日志轮询会自动恢复");
        r.put("data", Map.of(
                "stamp", stamp,
                "logFile", log.getAbsolutePath(),
                "gatewayDowntimeHint", "最后重启 bff-web(8391) 时 /web 可能短暂 502，属正常现象"
        ));
        return r;
    }

    /** 增量读取日志（offset 字节起，最多读 64KB） */
    @GetMapping(value = "/log", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> log(@RequestParam String stamp,
                                   @RequestParam(defaultValue = "0") long offset,
                                   HttpServletRequest req) {
        verifyToken(req);
        File log = new File(resolveLogDirPath(), "restart-all-" + stamp + ".log");
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        if (!log.exists()) {
            r.put("data", Map.of("text", "", "offset", 0L, "size", 0L, "exists", false));
            return r;
        }
        long size = log.length();
        StringBuilder sb = new StringBuilder();
        long newOffset = Math.min(offset, size);
        long max = Math.min(size - newOffset, 64 * 1024L);
        try (RandomAccessFile raf = new RandomAccessFile(log, "r")) {
            raf.seek(newOffset);
            byte[] buf = new byte[(int) max];
            int read = raf.read(buf);
            if (read > 0) sb.append(new String(buf, 0, read, StandardCharsets.UTF_8));
            newOffset += Math.max(read, 0);
        } catch (Exception e) {
            throw new RuntimeException("读取日志失败: " + e.getMessage(), e);
        }
        r.put("data", Map.of(
                "text", sb.toString(),
                "offset", newOffset,
                "size", size,
                "exists", true,
                "finished", isRestartFinished(log),
                "success", isRestartSuccess(log)
        ));
        return r;
    }

    /** 查询是否已完成 */
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> status(@RequestParam String stamp, HttpServletRequest req) {
        verifyToken(req);
        File log = new File(resolveLogDirPath(), "restart-all-" + stamp + ".log");
        boolean finished = log.exists() && isRestartFinished(log);
        boolean success = finished && isRestartSuccess(log);
        Map<String, Object> r = new HashMap<>();
        r.put("code", 200);
        r.put("data", Map.of(
                "finished", finished,
                "success", success,
                "size", log.exists() ? log.length() : 0
        ));
        return r;
    }

    /** ---------- 私有 ---------- */
    private String resolveLogDirPath() {
        return OpsScriptPaths.resolve(logDir, "jar/restart-logs");
    }

    private void verifyToken(HttpServletRequest req) {
        String t = req.getHeader("X-Admin-Token");
        if (t == null || !t.equals(adminToken)) {
            throw new RuntimeException("X-Admin-Token 鉴权失败");
        }
    }

    private boolean bool(Map<String, Object> body, String k, boolean def) {
        if (body == null) return def;
        Object v = body.get(k);
        if (v == null) return def;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(String.valueOf(v));
    }

    private String validatedTestClasses(Map<String, Object> body) {
        if (body == null || body.get("testClasses") == null) return null;
        String value = String.valueOf(body.get("testClasses")).trim();
        if (value.isEmpty()) return null;
        if (!value.matches("[A-Za-z0-9_.,]+")) {
            throw new IllegalArgumentException("testClasses 仅允许 Java 类名、点号和逗号");
        }
        return value;
    }

    /** 将 body.frontends 数组转为 -Frontends cloud,mall-admin 传给 PowerShell */
    private void appendFrontends(List<String> cmd, Map<String, Object> body) {
        if (body == null) return;
        Object v = body.get("frontends");
        if (v == null) return;
        List<String> keys = new ArrayList<>();
        if (v instanceof List) {
            for (Object o : (List<?>) v) {
                if (o != null && !String.valueOf(o).isBlank()) {
                    keys.add(String.valueOf(o).trim());
                }
            }
        } else {
            String s = String.valueOf(v).trim();
            if (!s.isEmpty()) {
                for (String part : s.split(",")) {
                    if (!part.isBlank()) keys.add(part.trim());
                }
            }
        }
        if (!keys.isEmpty()) {
            cmd.add("-Frontends");
            cmd.add(String.join(",", keys));
        }
    }

    /** 将 body.forceServices 转为 -ForceServices service1,service2，供受控增量部署使用。 */
    private void appendForceServices(List<String> cmd, Map<String, Object> body) {
        if (body == null) return;
        Object v = body.get("forceServices");
        if (v == null) return;
        List<String> services = new ArrayList<>();
        if (v instanceof List) {
            for (Object o : (List<?>) v) {
                if (o != null && !String.valueOf(o).isBlank()) services.add(String.valueOf(o).trim());
            }
        } else {
            for (String part : String.valueOf(v).split(",")) {
                if (!part.isBlank()) services.add(part.trim());
            }
        }
        if (!services.isEmpty()) {
            cmd.add("-ForceServices");
            cmd.add(String.join(",", services));
        }
    }

    private boolean tailContains(File f, String key) {
        return tailContains(f, key, 4096);
    }

    private boolean tailContains(File f, String key, int tailBytes) {
        long size = f.length();
        long start = Math.max(0, size - tailBytes);
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            raf.seek(start);
            byte[] buf = new byte[(int) (size - start)];
            int n = raf.read(buf);
            if (n <= 0) return false;
            return new String(buf, 0, n, StandardCharsets.UTF_8).contains(key);
        } catch (Exception e) {
            return false;
        }
    }

    /** 脚本正常结束或 finally 已写入 RESULT 行 */
    private boolean isRestartFinished(File log) {
        if (tailContains(log, "FINISHED")) return true;
        if (tailContains(log, "全流程结束")) return true;
        if (tailContains(log, "RESULT=SUCCESS")) return true;
        if (tailContains(log, "RESULT=FAILED")) return true;
        if (tailContains(log, "RESULT=ABORTED")) return true;
        // 兼容异常退出：阶段 6 已 OK 且日志超过 8 秒未增长
        if (!tailContains(log, "[OK] 阶段 6 / 关键端口监听验证 完成")) return false;
        long idleMs = System.currentTimeMillis() - log.lastModified();
        return idleMs >= 8000;
    }

    private boolean isRestartSuccess(File log) {
        if (tailContains(log, "RESULT=FAILED") || tailContains(log, "RESULT=ABORTED")) return false;
        if (tailContains(log, "RESULT=SUCCESS")) return true;
        if (tailContains(log, "frontend build done, no Java service touched")) return true;
        return tailContains(log, "[OK] 阶段 6 / 关键端口监听验证 完成")
                && !tailContains(log, "RESULT=FAILED")
                && !tailContains(log, "RESULT=ABORTED");
    }
}
