package com.houjiang.bffweb.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 管理端运维操作公共工具（X-Admin-Token 鉴权 + PowerShell 脚本执行）。
/**
 * @author Amos
 */
public final class AdminOpsSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AdminOpsSupport() {
    }

    /**
     * 校验 X-Admin-Token 请求头。
/**
     *
     * @param req            HTTP 请求
     * @param expectedToken  配置的令牌
     */
    public static void verifyToken(HttpServletRequest req, String expectedToken) {
        String t = req.getHeader("X-Admin-Token");
        if (t == null || !t.equals(expectedToken)) {
            throw new RuntimeException("X-Admin-Token 鉴权失败");
        }
    }

    /**
     * 执行 PowerShell 脚本并解析 stdout 最后一行 JSON。
/**
     *
     * @param scriptPath 脚本绝对路径
     * @param timeoutSec 超时秒数
     * @param psArgs     传给 -File 之后的参数（不含 powershell 自身参数）
     * @return 解析后的 Map（含 ok/message/data）
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> runPowerShellJson(String scriptPath, int timeoutSec, String... psArgs)
            throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add(OpsScriptPaths.isWindows() ? "powershell.exe" : "pwsh");
        cmd.add("-NoProfile");
        cmd.add("-ExecutionPolicy");
        cmd.add("Bypass");
        cmd.add("-File");
        cmd.add(scriptPath);
        for (String a : psArgs) {
            cmd.add(a);
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        }

        boolean finished = proc.waitFor(timeoutSec, TimeUnit.SECONDS);
        if (!finished) {
            proc.destroyForcibly();
            throw new RuntimeException("脚本执行超时 (" + timeoutSec + "s): " + scriptPath);
        }

        int exit = proc.exitValue();
        String raw = output.toString().trim();
        if (raw.isEmpty()) {
            throw new RuntimeException("脚本无输出 (exit=" + exit + ")");
        }

        String jsonLine = extractScriptJsonLine(raw);

        Map<String, Object> parsed = MAPPER.readValue(jsonLine, new TypeReference<Map<String, Object>>() {});
        Object ok = parsed.get("ok");
        if (Boolean.FALSE.equals(ok) || (ok instanceof String && "false".equalsIgnoreCase((String) ok))) {
            Object msg = parsed.get("message");
            throw new RuntimeException(msg != null ? String.valueOf(msg) : "脚本执行失败");
        }
        if (exit != 0 && !Boolean.TRUE.equals(ok)) {
            throw new RuntimeException("脚本 exit=" + exit + ": " + parsed.getOrDefault("message", raw));
        }
        return parsed;
    }

    /**
     * 从脚本 stdout 中提取根 JSON 行（Write-JsonResult 输出的 {@code {"ok":...}}）。
/**
     *
     * <p>不能用 {@code lastIndexOf('{')}：list 响应的 data 数组内每个服务对象也含 {@code {}，会截断成无效片段。
     *
     * @param raw 脚本完整 stdout
     * @return 可解析的根 JSON 字符串
     */
    static String extractScriptJsonLine(String raw) {
        String[] lines = raw.split("\\R");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.startsWith("{\"ok\"")) {
                return line;
            }
        }
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.startsWith("{") && line.endsWith("}")) {
                return line;
            }
        }
        throw new RuntimeException("脚本输出中未找到 JSON: " + raw.substring(0, Math.min(200, raw.length())));
    }
}
