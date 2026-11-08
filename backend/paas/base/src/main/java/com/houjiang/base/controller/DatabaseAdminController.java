package com.houjiang.base.controller;

import com.houjiang.base.service.DatabaseAdminService;
import com.houjiang.commons.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据库脚本管理（供 Cloud 后台「数据库执行」调用）
 * @author Amos
 */
@RestController
@RequestMapping("/base/database")
@RequiredArgsConstructor
public class DatabaseAdminController {

    private final DatabaseAdminService databaseAdminService;

    @GetMapping("/patches")
    public ResponseResult listPatches() {
        return databaseAdminService.listPatches();
    }

    @GetMapping("/patch/{patchId}/content")
    public ResponseResult readPatch(@PathVariable String patchId) {
        return databaseAdminService.readPatchContent(patchId);
    }

    @GetMapping("/list")
    public ResponseResult listDatabases() {
        return databaseAdminService.listDatabases();
    }

    @GetMapping("/{dbName}/tables")
    public ResponseResult listTables(@PathVariable String dbName) {
        return databaseAdminService.listTables(dbName);
    }

    @GetMapping("/{dbName}/tables/{tableName}/columns")
    public ResponseResult listColumns(@PathVariable String dbName, @PathVariable String tableName) {
        return databaseAdminService.listColumns(dbName, tableName);
    }

    @PostMapping("/execute")
    public ResponseResult execute(@RequestBody Map<String, Object> body) {
        return databaseAdminService.executeSql(body);
    }

    @PostMapping("/patch/{patchId}/execute")
    public ResponseResult executePatch(@PathVariable String patchId,
                                       @RequestBody(required = false) Map<String, Object> body) {
        return databaseAdminService.executePatch(patchId, body == null ? Map.of() : body);
    }

    @PostMapping("/wecom/import")
    public ResponseResult importWecom(@RequestBody(required = false) Map<String, Object> body) {
        return databaseAdminService.executeAllWecomPatches(body == null ? Map.of() : body);
    }
}
