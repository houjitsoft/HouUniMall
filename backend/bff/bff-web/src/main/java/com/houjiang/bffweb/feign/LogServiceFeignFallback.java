package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 日志服务 Feign 客户端降级处理
/**
 *
 * <p>当日志服务不可用时，所有接口统一返回友好的错误提示，
 * 避免因日志服务异常导致整体系统不可用。</p>
 * @author Amos
 */
@Component
public class LogServiceFeignFallback implements LogServiceFeign {

    // ==================== 日志查询接口降级处理 ====================

    /**
     * 操作日志分页查询降级
/**
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页记录数
     * @return 降级结果
     */
    @Override
    public ResponseResult queryOperationLogs(Map<String, Object> params, Long current, Long size) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 访问日志分页查询降级
/**
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页记录数
     * @return 降级结果
     */
    @Override
    public ResponseResult queryAccessLogs(Map<String, Object> params, Long current, Long size) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 登录日志分页查询降级
/**
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页记录数
     * @return 降级结果
     */
    @Override
    public ResponseResult queryLoginLogs(Map<String, Object> params, Long current, Long size) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 错误日志分页查询降级
/**
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页记录数
     * @return 降级结果
     */
    @Override
    public ResponseResult queryErrorLogs(Map<String, Object> params, Long current, Long size) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 业务日志分页查询降级
/**
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页记录数
     * @return 降级结果
     */
    @Override
    public ResponseResult queryBusinessLogs(Map<String, Object> params, Long current, Long size) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 系统日志分页查询降级
/**
     *
     * @param params  查询条件
     * @param current 当前页码
     * @param size    每页记录数
     * @return 降级结果
     */
    @Override
    public ResponseResult querySystemLogs(Map<String, Object> params, Long current, Long size) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    // ==================== 日志分析接口降级处理 ====================

    /**
     * 日志统计概览降级
/**
     *
     * @return 降级结果
     */
    @Override
    public ResponseResult getAnalysisOverview() {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 日志趋势数据降级
/**
     *
     * @param params 查询条件
     * @return 降级结果
     */
    @Override
    public ResponseResult getAnalysisTrend(Map<String, Object> params) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 热门错误排行降级
/**
     *
     * @param limit 返回最大条数
     * @return 降级结果
     */
    @Override
    public ResponseResult getTopErrors(Integer limit) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    // ==================== 日志设置接口降级处理 ====================

    /**
     * 获取日志配置项列表降级
/**
     *
     * @return 降级结果
     */
    @Override
    public ResponseResult getSettings() {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 更新日志配置项降级
/**
     *
     * @param params 更新参数
     * @return 降级结果
     */
    @Override
    public ResponseResult updateSetting(Map<String, String> params) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    // ==================== 日志清理接口降级处理 ====================

    /**
     * 清理日志降级
/**
     *
     * @param type 日志类型
     * @param days 保留天数
     * @return 降级结果
     */
    @Override
    public ResponseResult cleanLogs(String type, Integer days) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult queryElkLogs(Map<String, Object> params, Long current, Long size) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult elkLevelStats(Map<String, String> params) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    // ==================== 运行错误告警接口降级处理 ====================

    @Override
    public ResponseResult queryAlerts(Map<String, Object> params, Long current, Long size) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    /**
     * 未处理告警数量降级
     *
     * <p>告警服务不可用时按 0 处理，避免后台红点因为服务抖动而假报警。</p>
     *
     * <p>这是后台首屏就会打的接口，返回 fail 会让每个页面加载都在 console 刷一条
     * 「接口返回非成功状态」。告警未读数只是装饰性红点，服务不可用时「没有未读」
     * 是比「报错」更诚实的表达（真要看告警列表时 {@link #queryAlerts} 仍会如实报错）。</p>
     *
     * @return 数量为 0 的降级结果
     */
    @Override
    public ResponseResult alertUnreadCount() {
        return ResponseResult.success(0);
    }

    @Override
    public ResponseResult alertDetail(Long id) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult handleAlert(Long id, String remark) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult ignoreAlert(Long id, String remark) {
        return ResponseResult.fail("日志服务暂时不可用，请稍后重试");
    }
}
