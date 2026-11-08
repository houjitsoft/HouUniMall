package com.houjiang.logging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.logging.dto.LogAccessQueryDTO;
import com.houjiang.logging.dto.LogBusinessQueryDTO;
import com.houjiang.logging.dto.LogErrorQueryDTO;
import com.houjiang.logging.dto.LogLoginQueryDTO;
import com.houjiang.logging.dto.LogOperationQueryDTO;
import com.houjiang.logging.entity.LogAccessEntity;
import com.houjiang.logging.entity.LogBusinessEntity;
import com.houjiang.logging.entity.LogErrorEntity;
import com.houjiang.logging.entity.LogLoginEntity;
import com.houjiang.logging.entity.LogOperationEntity;
import com.houjiang.logging.service.LogAccessService;
import com.houjiang.logging.service.LogBusinessService;
import com.houjiang.logging.service.LogErrorService;
import com.houjiang.logging.service.LogLoginService;
import com.houjiang.logging.service.LogOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志查询 Controller
 *
 * <p>提供各类日志（操作、访问、登录、错误、业务、系统）的分页查询接口，
 * 以及按类型批量清理过期日志的接口。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@RestController
@RequestMapping("/logging")
@RequiredArgsConstructor
@Tag(name = "日志查询", description = "各类日志的分页查询与清理接口")
public class LogQueryController {

    private final LogOperationService operationService;
    private final LogAccessService accessService;
    private final LogLoginService loginService;
    private final LogErrorService errorService;
    private final LogBusinessService businessService;

    /**
     * 操作日志分页查询
     *
     * @param dto     查询条件
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @Operation(summary = "操作日志分页查询")
    @PostMapping("/operation/page")
    public ResponseResult<IPage<LogOperationEntity>> operationPage(
            @RequestBody LogOperationQueryDTO dto,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "20") long size) {
        return ResponseResult.success(operationService.queryPage(dto, current, size));
    }

    /**
     * 访问日志分页查询
     *
     * @param dto     查询条件
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @Operation(summary = "访问日志分页查询")
    @PostMapping("/access/page")
    public ResponseResult<IPage<LogAccessEntity>> accessPage(
            @RequestBody LogAccessQueryDTO dto,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "20") long size) {
        return ResponseResult.success(accessService.queryPage(dto, current, size));
    }

    /**
     * 登录日志分页查询
     *
     * @param dto     查询条件
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @Operation(summary = "登录日志分页查询")
    @PostMapping("/login/page")
    public ResponseResult<IPage<LogLoginEntity>> loginPage(
            @RequestBody LogLoginQueryDTO dto,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "20") long size) {
        return ResponseResult.success(loginService.queryPage(dto, current, size));
    }

    /**
     * 错误日志分页查询
     *
     * @param dto     查询条件
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @Operation(summary = "错误日志分页查询")
    @PostMapping("/error/page")
    public ResponseResult<IPage<LogErrorEntity>> errorPage(
            @RequestBody LogErrorQueryDTO dto,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "20") long size) {
        return ResponseResult.success(errorService.queryPage(dto, current, size));
    }

    /**
     * 业务日志分页查询
     *
     * @param dto     查询条件
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @Operation(summary = "业务日志分页查询")
    @PostMapping("/business/page")
    public ResponseResult<IPage<LogBusinessEntity>> businessPage(
            @RequestBody LogBusinessQueryDTO dto,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "20") long size) {
        return ResponseResult.success(businessService.queryPage(dto, current, size));
    }

    /**
     * 系统日志分页查询
     *
     * <p>系统日志复用操作日志表，通过 module=system 字段过滤。</p>
     *
     * @param dto     查询条件（module 字段会被强制覆盖为 "system"）
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @Operation(summary = "系统日志分页查询（复用操作日志，module=system）")
    @PostMapping("/system/page")
    public ResponseResult<IPage<LogOperationEntity>> systemPage(
            @RequestBody LogOperationQueryDTO dto,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "20") long size) {
        // 系统日志强制过滤 module=system
        dto.setModule("system");
        return ResponseResult.success(operationService.queryPage(dto, current, size));
    }

    /**
     * 清理指定类型的过期日志
     *
     * <p>根据 type 参数决定清理哪类日志表，清理周期从 days 参数获取（默认90天）。</p>
     *
     * @param type 日志类型（operation/access/login/error/business）
     * @param days 保留天数，超过此天数的记录将被删除，默认90天
     * @return 操作结果
     */
    @Operation(summary = "清理指定类型过期日志")
    @DeleteMapping("/{type}/clean")
    public ResponseResult<String> cleanLogs(
            @Parameter(description = "日志类型（operation/access/login/error/business）")
            @PathVariable String type,
            @Parameter(description = "保留天数，默认90天")
            @RequestParam(defaultValue = "90") int days) {
        switch (type.toLowerCase()) {
            case "operation":
                operationService.cleanExpiredLogs(days);
                break;
            case "access":
                accessService.cleanExpiredLogs(days);
                break;
            case "login":
                loginService.cleanExpiredLogs(days);
                break;
            case "error":
                errorService.cleanExpiredLogs(days);
                break;
            case "business":
                businessService.cleanExpiredLogs(days);
                break;
            default:
                return ResponseResult.fail("不支持的日志类型: " + type);
        }
        return ResponseResult.success("清理完成");
    }
}
