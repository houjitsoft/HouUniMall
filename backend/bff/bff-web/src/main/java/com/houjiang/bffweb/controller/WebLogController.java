package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.LogServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 日志管理 BFF 适配器
/**
 *
 * <p>只做请求转发，不包含任何业务逻辑。
 * 对外暴露 {@code /web/log/**} 路径，内部通过 {@link LogServiceFeign} 调用 logging 微服务。</p>
 *
 * <p>认证方式：统一依赖 commons 中的 JWT Token 过滤器进行权限验证，
 * 禁止使用 permitAll() 放开任何业务接口。</p>
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/log")
public class WebLogController {

    @Autowired
    private LogServiceFeign logServiceFeign;

    // ==================== 日志查询接口 ====================

    /**
     * 操作日志分页查询
/**
     *
     * @param params  查询条件，支持 operator/module/action/startTime/endTime 等字段过滤
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果，包含 records/total/pages 等字段
     */
    @PostMapping("/operation/page")
    public ResponseResult queryOperationLogs(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return logServiceFeign.queryOperationLogs(params, current, size);
    }

    /**
     * 访问日志分页查询
/**
     *
     * @param params  查询条件，支持 ip/path/method/startTime/endTime 等字段过滤
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @PostMapping("/access/page")
    public ResponseResult queryAccessLogs(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return logServiceFeign.queryAccessLogs(params, current, size);
    }

    /**
     * 登录日志分页查询
/**
     *
     * @param params  查询条件，支持 username/ip/loginResult/startTime/endTime 等字段过滤
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @PostMapping("/login/page")
    public ResponseResult queryLoginLogs(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return logServiceFeign.queryLoginLogs(params, current, size);
    }

    /**
     * 错误日志分页查询
/**
     *
     * @param params  查询条件，支持 errorType/errorMessage/startTime/endTime 等字段过滤
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @PostMapping("/error/page")
    public ResponseResult queryErrorLogs(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return logServiceFeign.queryErrorLogs(params, current, size);
    }

    /**
     * 业务日志分页查询
/**
     *
     * @param params  查询条件，支持 bizType/bizId/operator/startTime/endTime 等字段过滤
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @PostMapping("/business/page")
    public ResponseResult queryBusinessLogs(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return logServiceFeign.queryBusinessLogs(params, current, size);
    }

    /**
     * 系统日志分页查询（复用操作日志，module 强制为 system）
/**
     *
     * @param params  查询条件，支持 operator/action/startTime/endTime 等字段过滤
     * @param current 当前页码，默认1
     * @param size    每页记录数，默认20
     * @return 分页结果
     */
    @PostMapping("/system/page")
    public ResponseResult querySystemLogs(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return logServiceFeign.querySystemLogs(params, current, size);
    }

    // ==================== 日志分析接口 ====================

    /**
     * 日志统计概览
/**
     *
     * <p>返回各类日志总数、今日新增量、错误率等汇总指标，供数据看板使用。</p>
     *
     * @return 统计概览数据 Map
     */
    @GetMapping("/analysis/overview")
    public ResponseResult getAnalysisOverview() {
        return logServiceFeign.getAnalysisOverview();
    }

    /**
     * 日志趋势数据
/**
     *
     * <p>按时间范围统计每天的日志数量，支持按模块和日志类型过滤。</p>
     *
     * @param params 分析查询条件，支持 startDate/endDate/logType/module 等字段
     * @return 趋势数据列表，每项包含 date 和 count
     */
    @PostMapping("/analysis/trend")
    public ResponseResult getAnalysisTrend(@RequestBody Map<String, Object> params) {
        return logServiceFeign.getAnalysisTrend(params);
    }

    /**
     * 热门错误排行
/**
     *
     * <p>按错误类型分组统计，返回出现次数最多的前 N 条。</p>
     *
     * @param limit 返回最大条数，默认10
     * @return 错误排行列表，每项包含 errorType 和 count
     */
    @GetMapping("/analysis/top-errors")
    public ResponseResult getTopErrors(
            @RequestParam(defaultValue = "10") Integer limit) {
        return logServiceFeign.getTopErrors(limit);
    }

    // ==================== 日志设置接口 ====================

    /**
     * 获取所有日志配置项
/**
     *
     * <p>返回系统中所有已配置的日志模块设置列表。</p>
     *
     * @return 日志配置项列表
     */
    @PostMapping("/setting/list")
    public ResponseResult getSettings() {
        return logServiceFeign.getSettings();
    }

    /**
     * 更新日志配置项
/**
     *
     * <p>请求体须包含 key（配置键名）和 value（新配置值），两者均为字符串。</p>
     *
     * @param params 更新参数，key 为配置键名，value 为新配置值
     * @return 操作结果
     */
    @PostMapping("/setting/update")
    public ResponseResult updateSetting(@RequestBody Map<String, String> params) {
        return logServiceFeign.updateSetting(params);
    }

    // ==================== 日志清理接口 ====================

    /**
     * 清理指定类型的过期日志
/**
     *
     * <p>根据 type 参数决定清理哪类日志表，保留最近 days 天的数据。</p>
     *
     * @param type 日志类型（operation/access/login/error/business）
     * @param days 保留天数，超过此天数的记录将被删除
     * @return 操作结果
     */
    @DeleteMapping("/{type}/clean")
    public ResponseResult cleanLogs(
            @PathVariable("type") String type,
            @RequestParam("days") Integer days) {
        return logServiceFeign.cleanLogs(type, days);
    }

    // ==================== ELK 运行日志接口 ====================

    /**
     * 运行日志分页（Elasticsearch / Kibana 数据源）
     */
    @PostMapping("/elk/page")
    public ResponseResult queryElkLogs(
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size) {
        return logServiceFeign.queryElkLogs(params, current, size);
    }

    /**
     * 运行日志级别分布（分析页图表）
     */
    @PostMapping("/elk/level-stats")
    public ResponseResult elkLevelStats(@RequestBody Map<String, String> params) {
        return logServiceFeign.elkLevelStats(params);
    }

    // 运行错误告警接口已迁出到 WebLogAlertController：
    // 本类带 houcloud.legacy-bff.enabled 条件装配，生产未开该开关（详见新类注释），
    // 放在这里会一律「BFF 路由未命中」。
}
