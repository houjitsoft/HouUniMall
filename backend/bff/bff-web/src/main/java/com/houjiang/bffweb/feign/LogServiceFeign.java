package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 日志服务 Feign 客户端
/**
 *
 * <p>只做服务调用，不包含任何业务逻辑。</p>
 * <ul>
 *   <li>未配置 {@code feign.logging.url}：走 Nacos 服务发现（name=logging）</li>
 *   <li>配置 {@code feign.logging.url}：使用直连地址（例如 http://&lt;logging主机&gt;:8093）</li>
 * </ul>
 * @author Amos
 */
@FeignClient(
        name = "logging",
        contextId = "logServiceFeign",
        url = "${feign.logging.url:}",
        path = "/plugin",
        fallback = LogServiceFeignFallback.class
)
public interface LogServiceFeign {

    // ==================== 日志查询接口 ====================

    /**
     * 操作日志分页查询
/**
     *
     * @param params  查询条件（对应 LogOperationQueryDTO 字段，以 Map 透传）
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页数据
     */
    @PostMapping("/logging/operation/page")
    ResponseResult queryOperationLogs(@RequestBody Map<String, Object> params,
                                      @RequestParam("current") Long current,
                                      @RequestParam("size") Long size);

    /**
     * 访问日志分页查询
/**
     *
     * @param params  查询条件（对应 LogAccessQueryDTO 字段，以 Map 透传）
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页数据
     */
    @PostMapping("/logging/access/page")
    ResponseResult queryAccessLogs(@RequestBody Map<String, Object> params,
                                   @RequestParam("current") Long current,
                                   @RequestParam("size") Long size);

    /**
     * 登录日志分页查询
/**
     *
     * @param params  查询条件（对应 LogLoginQueryDTO 字段，以 Map 透传）
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页数据
     */
    @PostMapping("/logging/login/page")
    ResponseResult queryLoginLogs(@RequestBody Map<String, Object> params,
                                  @RequestParam("current") Long current,
                                  @RequestParam("size") Long size);

    /**
     * 错误日志分页查询
/**
     *
     * @param params  查询条件（对应 LogErrorQueryDTO 字段，以 Map 透传）
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页数据
     */
    @PostMapping("/logging/error/page")
    ResponseResult queryErrorLogs(@RequestBody Map<String, Object> params,
                                  @RequestParam("current") Long current,
                                  @RequestParam("size") Long size);

    /**
     * 业务日志分页查询
/**
     *
     * @param params  查询条件（对应 LogBusinessQueryDTO 字段，以 Map 透传）
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页数据
     */
    @PostMapping("/logging/business/page")
    ResponseResult queryBusinessLogs(@RequestBody Map<String, Object> params,
                                     @RequestParam("current") Long current,
                                     @RequestParam("size") Long size);

    /**
     * 系统日志分页查询（复用操作日志，module 强制为 system）
/**
     *
     * @param params  查询条件（对应 LogOperationQueryDTO 字段，以 Map 透传）
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页数据
     */
    @PostMapping("/logging/system/page")
    ResponseResult querySystemLogs(@RequestBody Map<String, Object> params,
                                   @RequestParam("current") Long current,
                                   @RequestParam("size") Long size);

    // ==================== 日志分析接口 ====================

    /**
     * 获取日志统计概览
/**
     *
     * <p>返回各类日志总数、今日新增量、错误率等汇总指标。</p>
     *
     * @return 统计概览数据
     */
    @GetMapping("/logging/analysis/overview")
    ResponseResult getAnalysisOverview();

    /**
     * 获取日志数量趋势数据
/**
     *
     * @param params 分析查询条件（对应 LogAnalysisQueryDTO 字段，以 Map 透传）
     * @return 趋势数据列表
     */
    @PostMapping("/logging/analysis/trend")
    ResponseResult getAnalysisTrend(@RequestBody Map<String, Object> params);

    /**
     * 获取热门错误排行
/**
     *
     * @param limit 返回最大条数，默认10
     * @return 错误排行列表
     */
    @GetMapping("/logging/analysis/top-errors")
    ResponseResult getTopErrors(@RequestParam(defaultValue = "10") Integer limit);

    // ==================== 日志设置接口 ====================

    /**
     * 获取所有日志配置项
/**
     *
     * @return 配置项列表
     */
    @PostMapping("/logging/setting/list")
    ResponseResult getSettings();

    /**
     * 更新日志配置项
/**
     *
     * <p>请求体须包含 key（配置键名）和 value（新配置值）。</p>
     *
     * @param params 更新参数，key 和 value 均为 String
     * @return 操作结果
     */
    @PostMapping("/logging/setting/update")
    ResponseResult updateSetting(@RequestBody Map<String, String> params);

    // ==================== 日志清理接口 ====================

    /**
     * 清理指定类型的过期日志
/**
     *
     * @param type 日志类型（operation/access/login/error/business）
     * @param days 保留天数，超过此天数的记录将被删除
     * @return 操作结果
     */
    @DeleteMapping("/logging/{type}/clean")
    ResponseResult cleanLogs(@PathVariable("type") String type,
                             @RequestParam("days") Integer days);

    // ==================== ELK 运行日志接口 ====================

    /**
     * ELK 运行日志分页查询（系统日志 / 运行错误）
     */
    @PostMapping("/logging/elk/page")
    ResponseResult queryElkLogs(@RequestBody Map<String, Object> params,
                                @RequestParam("current") Long current,
                                @RequestParam("size") Long size);

    /**
     * ELK 日志级别分布
     */
    @PostMapping("/logging/elk/level-stats")
    ResponseResult elkLevelStats(@RequestBody Map<String, String> params);

    // ==================== 运行错误告警接口 ====================

    /**
     * 运行告警分页查询
     *
     * <p>错误日志按指纹聚合后的告警列表，回答「出了什么错、谁触发的、发生了几次」。</p>
     *
     * @param params  查询条件（status/module/errorType/startTime/endTime/keyword）
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页数据
     */
    @PostMapping("/logging/alert/page")
    ResponseResult queryAlerts(@RequestBody Map<String, Object> params,
                               @RequestParam("current") Long current,
                               @RequestParam("size") Long size);

    /**
     * 未处理告警数量（后台红点）
     *
     * @return 未处理条数
     */
    @GetMapping("/logging/alert/unread-count")
    ResponseResult alertUnreadCount();

    /**
     * 运行告警详情（含最近一次堆栈）
     *
     * @param id 告警ID
     * @return 告警详情
     */
    @GetMapping("/logging/alert/{id}")
    ResponseResult alertDetail(@PathVariable("id") Long id);

    /**
     * 标记告警为已处理
     *
     * @param id     告警ID
     * @param remark 处理备注（可为空）
     * @return 操作结果
     */
    @PostMapping("/logging/alert/{id}/handle")
    ResponseResult handleAlert(@PathVariable("id") Long id,
                               @RequestParam(value = "remark", required = false) String remark);

    /**
     * 标记告警为已忽略
     *
     * @param id     告警ID
     * @param remark 忽略原因（可为空）
     * @return 操作结果
     */
    @PostMapping("/logging/alert/{id}/ignore")
    ResponseResult ignoreAlert(@PathVariable("id") Long id,
                               @RequestParam(value = "remark", required = false) String remark);
}
