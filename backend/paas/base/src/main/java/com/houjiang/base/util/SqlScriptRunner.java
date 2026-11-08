package com.houjiang.base.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 批量执行 SQL 脚本（按语句拆分，跳过空行与注释）
 * @author Amos
 */
public final class SqlScriptRunner {

    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(DROP\\s+DATABASE|DROP\\s+SCHEMA|SHUTDOWN)\\b",
            Pattern.CASE_INSENSITIVE);

    private SqlScriptRunner() {
    }

    public static List<String> splitStatements(String script) {
        if (!StringUtils.hasText(script)) {
            return List.of();
        }
        List<String> statements = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (String line : script.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            buf.append(line).append('\n');
            if (trimmed.endsWith(";")) {
                String stmt = buf.toString().trim();
                if (stmt.endsWith(";")) {
                    stmt = stmt.substring(0, stmt.length() - 1).trim();
                }
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                buf.setLength(0);
            }
        }
        String tail = buf.toString().trim();
        if (!tail.isEmpty()) {
            if (tail.endsWith(";")) {
                tail = tail.substring(0, tail.length() - 1).trim();
            }
            if (!tail.isEmpty()) {
                statements.add(tail);
            }
        }
        return statements;
    }

    public static void assertAllowed(String sql) {
        if (FORBIDDEN.matcher(sql).find()) {
            throw new IllegalArgumentException("禁止执行 DROP DATABASE / DROP SCHEMA / SHUTDOWN 类语句");
        }
    }

    public static Map<String, Object> executeScript(JdbcTemplate jdbcTemplate, String script, boolean stopOnError) {
        List<String> statements = splitStatements(script);
        List<Map<String, Object>> details = new ArrayList<>();
        int success = 0;
        int failed = 0;

        for (int i = 0; i < statements.size(); i++) {
            String sql = statements.get(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("index", i + 1);
            row.put("sql", abbreviate(sql, 500));
            try {
                assertAllowed(sql);
                Map<String, Object> one = executeOne(jdbcTemplate, sql);
                row.put("status", "ok");
                row.put("affected", one.get("affected"));
                if (one.containsKey("rows")) {
                    row.put("rows", one.get("rows"));
                }
                success++;
            } catch (Exception ex) {
                row.put("status", "error");
                row.put("message", ex.getMessage());
                failed++;
                if (stopOnError) {
                    details.add(row);
                    break;
                }
            }
            details.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", statements.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("details", details);
        return result;
    }

    private static boolean isQuery(String sql) {
        String upper = sql.trim().toUpperCase(Locale.ROOT);
        return upper.startsWith("SELECT") || upper.startsWith("SHOW") || upper.startsWith("DESC")
                || upper.startsWith("DESCRIBE") || upper.startsWith("EXPLAIN");
    }

    private static Map<String, Object> executeOne(JdbcTemplate jdbcTemplate, String sql) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (isQuery(sql)) {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            result.put("affected", rows.size());
            result.put("rows", rows);
        } else {
            int affected = jdbcTemplate.update(sql);
            result.put("affected", affected);
        }
        return result;
    }

    public static Map<String, Object> executeFile(JdbcTemplate jdbcTemplate, java.io.File file, boolean stopOnError)
            throws java.io.IOException {
        String content = java.nio.file.Files.readString(file.toPath(), StandardCharsets.UTF_8);
        Map<String, Object> result = executeScript(jdbcTemplate, content, stopOnError);
        result.put("file", file.getName());
        return result;
    }

    /** 使用 Spring ScriptUtils 整文件执行（适合标准 .sql） */
    public static void executeFileSpring(Connection connection, org.springframework.core.io.Resource resource)
            throws java.sql.SQLException {
        ScriptUtils.executeSqlScript(connection, resource);
    }

    private static String abbreviate(String sql, int max) {
        if (sql == null || sql.length() <= max) {
            return sql;
        }
        return sql.substring(0, max) + "...";
    }
}
