package com.houjiang.base.service;

import com.houjiang.base.util.SqlScriptRunner;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@RequiredArgsConstructor
@Service
public class DatabaseAdminService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${houcloud.sql-patches-dir:D:/wwwroot/houcloud/docs/sql/patches}")
    private String sqlPatchesDir;

    private static final List<Map<String, String>> PRESET_PATCHES = List.of(
            Map.of("id", "hou_oa_wecom_scrm_extend", "name", "企微 SCRM 扩展表", "file", "hou_oa_wecom_scrm_extend.sql"),
            Map.of("id", "permission_wecom_menus_extend", "name", "企微菜单扩展", "file", "permission_wecom_menus_extend.sql")
    );

    public ResponseResult listPatches() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, String> preset : PRESET_PATCHES) {
            Map<String, Object> item = new LinkedHashMap<>(preset);
            File file = resolvePatchFile(preset.get("file"));
            item.put("exists", file.exists());
            item.put("size", file.exists() ? file.length() : 0);
            item.put("path", file.getAbsolutePath());
            list.add(item);
        }
        return ResponseResult.success(list);
    }

    public ResponseResult executeSql(Map<String, Object> body) {
        String sql = body == null ? null : String.valueOf(body.getOrDefault("sql", ""));
        if (!StringUtils.hasText(sql)) {
            return ResponseResult.fail(400, "SQL 不能为空");
        }
        boolean stopOnError = !"false".equalsIgnoreCase(String.valueOf(body.get("stopOnError")));
        try {
            Map<String, Object> result = SqlScriptRunner.executeScript(jdbcTemplate, sql, stopOnError);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail(500, "执行失败: " + e.getMessage());
        }
    }

    public ResponseResult executePatch(String patchId, Map<String, Object> body) {
        if (!StringUtils.hasText(patchId)) {
            return ResponseResult.fail(400, "补丁 ID 不能为空");
        }
        String fileName = resolveFileName(patchId);
        if (fileName == null) {
            return ResponseResult.fail(404, "未知补丁: " + patchId);
        }
        File file = resolvePatchFile(fileName);
        if (!file.exists()) {
            return ResponseResult.fail(404, "补丁文件不存在: " + file.getAbsolutePath());
        }
        boolean stopOnError = body == null || !"false".equalsIgnoreCase(String.valueOf(body.get("stopOnError")));
        try {
            Map<String, Object> result = SqlScriptRunner.executeFile(jdbcTemplate, file, stopOnError);
            result.put("patchId", patchId);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail(500, "补丁执行失败: " + e.getMessage());
        }
    }

    public ResponseResult executeAllWecomPatches(Map<String, Object> body) {
        List<Map<String, Object>> runs = new ArrayList<>();
        for (Map<String, String> preset : PRESET_PATCHES) {
            ResponseResult one = executePatch(preset.get("id"), body);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("patchId", preset.get("id"));
            item.put("name", preset.get("name"));
            item.put("code", one.getCode());
            item.put("message", one.getMessage());
            item.put("data", one.getData());
            runs.add(item);
            if (one.getCode() != 1) {
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("runs", runs);
                return ResponseResult.fail(500, "企微补丁执行中断: " + preset.get("id") + " — " + one.getMessage(), summary);
            }
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("runs", runs);
        return ResponseResult.success(summary);
    }

    public ResponseResult readPatchContent(String patchId) {
        String fileName = resolveFileName(patchId);
        if (fileName == null) {
            return ResponseResult.fail(404, "未知补丁");
        }
        File file = resolvePatchFile(fileName);
        if (!file.exists()) {
            return ResponseResult.fail(404, "文件不存在");
        }
        try {
            String content = Files.readString(file.toPath());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("patchId", patchId);
            data.put("fileName", fileName);
            data.put("content", content);
            return ResponseResult.success(data);
        } catch (Exception e) {
            return ResponseResult.fail(500, "读取失败: " + e.getMessage());
        }
    }

    public ResponseResult listDatabases() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SHOW DATABASES");
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String name = String.valueOf(row.values().iterator().next());
                if ("information_schema".equalsIgnoreCase(name)
                        || "mysql".equalsIgnoreCase(name)
                        || "performance_schema".equalsIgnoreCase(name)
                        || "sys".equalsIgnoreCase(name)) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", name);
                list.add(item);
            }
            return ResponseResult.success(list);
        } catch (Exception e) {
            return ResponseResult.fail(500, "获取数据库列表失败: " + e.getMessage());
        }
    }

    public ResponseResult listTables(String dbName) {
        if (!StringUtils.hasText(dbName)) {
            return ResponseResult.fail(400, "数据库名不能为空");
        }
        try {
            String sql = "SELECT TABLE_NAME AS tableName, TABLE_COMMENT AS tableComment, "
                    + "TABLE_ROWS AS tableRows, DATA_LENGTH AS dataLength, "
                    + "INDEX_LENGTH AS indexLength, CREATE_TIME AS createTime "
                    + "FROM information_schema.TABLES WHERE TABLE_SCHEMA = ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, dbName);
            return ResponseResult.success(rows);
        } catch (Exception e) {
            return ResponseResult.fail(500, "获取表列表失败: " + e.getMessage());
        }
    }

    public ResponseResult listColumns(String dbName, String tableName) {
        if (!StringUtils.hasText(dbName) || !StringUtils.hasText(tableName)) {
            return ResponseResult.fail(400, "数据库名和表名不能为空");
        }
        try {
            String sql = "SELECT COLUMN_NAME AS columnName, DATA_TYPE AS dataType, "
                    + "IS_NULLABLE AS isNullable, COLUMN_DEFAULT AS columnDefault, "
                    + "COLUMN_COMMENT AS columnComment, COLUMN_KEY AS columnKey, "
                    + "EXTRA AS extra, ORDINAL_POSITION AS ordinalPosition "
                    + "FROM information_schema.COLUMNS "
                    + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? "
                    + "ORDER BY ORDINAL_POSITION";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, dbName, tableName);
            return ResponseResult.success(rows);
        } catch (Exception e) {
            return ResponseResult.fail(500, "获取表结构失败: " + e.getMessage());
        }
    }

    private String resolveFileName(String patchId) {
        for (Map<String, String> preset : PRESET_PATCHES) {
            if (patchId.equals(preset.get("id")) || patchId.equals(preset.get("file"))) {
                return preset.get("file");
            }
        }
        if (patchId.endsWith(".sql")) {
            return patchId;
        }
        return null;
    }

    private File resolvePatchFile(String fileName) {
        Path base = Path.of(sqlPatchesDir).normalize();
        Path target = base.resolve(fileName).normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("非法路径");
        }
        return target.toFile();
    }
}
