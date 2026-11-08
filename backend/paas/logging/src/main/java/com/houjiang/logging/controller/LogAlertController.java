package com.houjiang.logging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.logging.dto.LogAlertQueryDTO;
import com.houjiang.logging.entity.LogAlertEntity;
import com.houjiang.logging.service.LogAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 运行错误告警 Controller
 *
 * <p>面向后台「运行告警」中心：把流式错误日志聚合后的告警暴露给运维，
 * 回答三个问题 —— 出了什么错、谁在什么时候触发的、修没修。</p>
 *
 * @author Amos
 * @since 2026-09-25
 */
@Slf4j
@RestController
@RequestMapping("/logging/alert")
@RequiredArgsConstructor
@Tag(name = "运行错误告警", description = "运行期错误的聚合告警与处理接口")
public class LogAlertController {

    private final LogAlertService alertService;

    /**
     * 告警分页查询
     *
     * @param dto     查询条件（status 不传默认只查未处理）
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @Operation(summary = "运行告警分页查询")
    @PostMapping("/page")
    public ResponseResult<IPage<LogAlertEntity>> page(
            @RequestBody LogAlertQueryDTO dto,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页记录数") @RequestParam(defaultValue = "20") long size) {
        return ResponseResult.success(alertService.queryPage(dto == null ? new LogAlertQueryDTO() : dto, current, size));
    }

    /**
     * 未处理告警数量（后台红点）
     *
     * @return 未处理条数
     */
    @Operation(summary = "未处理告警数量")
    @GetMapping("/unread-count")
    public ResponseResult<Map<String, Object>> unreadCount() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", alertService.countUnhandled());
        return ResponseResult.success(data);
    }

    /**
     * 告警详情（含最近一次堆栈）
     *
     * @param id 告警ID
     * @return 告警实体
     */
    @Operation(summary = "运行告警详情")
    @GetMapping("/{id}")
    public ResponseResult<LogAlertEntity> detail(@PathVariable Long id) {
        return ResponseResult.success(alertService.getById(id));
    }

    /**
     * 标记告警为已处理
     *
     * @param id     告警ID
     * @param remark 处理备注（写清楚修复原因，便于回溯）
     * @return 操作结果
     */
    @Operation(summary = "标记告警为已处理")
    @PostMapping("/{id}/handle")
    public ResponseResult<Boolean> handle(@PathVariable Long id,
                                         @Parameter(description = "处理备注") @RequestParam(required = false) String remark) {
        return ResponseResult.success(alertService.markHandled(id, remark));
    }

    /**
     * 标记告警为已忽略
     *
     * @param id     告警ID
     * @param remark 忽略原因
     * @return 操作结果
     */
    @Operation(summary = "标记告警为已忽略")
    @PostMapping("/{id}/ignore")
    public ResponseResult<Boolean> ignore(@PathVariable Long id,
                                         @Parameter(description = "忽略原因") @RequestParam(required = false) String remark) {
        return ResponseResult.success(alertService.markIgnored(id, remark));
    }

    /**
     * 清理已闭环且长时间未复现的告警
     *
     * @param days 保留天数，默认90天
     * @return 操作结果
     */
    @Operation(summary = "清理已闭环告警")
    @PostMapping("/clean")
    public ResponseResult<Boolean> clean(@Parameter(description = "保留天数") @RequestParam(defaultValue = "90") int days) {
        alertService.cleanExpiredAlerts(days);
        return ResponseResult.success(true);
    }
}
