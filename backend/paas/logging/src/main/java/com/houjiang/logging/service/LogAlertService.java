package com.houjiang.logging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.logging.dto.LogAlertQueryDTO;
import com.houjiang.logging.entity.LogAlertEntity;
import com.houjiang.logging.entity.LogErrorEntity;

/**
 * 运行错误告警 Service 接口
 *
 * <p>把流式写入的错误日志聚合成「一个问题一条告警」，供后台告警中心消费。</p>
 *
 * @author Amos
 * @since 2026-09-25
 */
public interface LogAlertService extends IService<LogAlertEntity> {

    /**
     * 分页查询告警
     *
     * @param dto     查询条件 DTO（status 为空时默认只查未处理）
     * @param current 当前页码（从1开始）
     * @param size    每页记录数
     * @return 分页结果
     */
    IPage<LogAlertEntity> queryPage(LogAlertQueryDTO dto, long current, long size);

    /**
     * 收到一条错误日志后聚合成告警
     *
     * <p>按指纹命中已有告警则累加次数并刷新最近信息；已处理的告警再次出现时重新打开。</p>
     *
     * @param errorLog 已落库的错误日志
     */
    void onError(LogErrorEntity errorLog);

    /**
     * 统计未处理告警数量（后台红点用）
     *
     * @return 未处理（status=0）告警条数
     */
    long countUnhandled();

    /**
     * 标记告警为已处理
     *
     * @param id     告警ID
     * @param remark 处理备注（修复原因）
     * @return 是否更新成功
     */
    boolean markHandled(Long id, String remark);

    /**
     * 标记告警为已忽略
     *
     * @param id     告警ID
     * @param remark 忽略原因
     * @return 是否更新成功
     */
    boolean markIgnored(Long id, String remark);

    /**
     * 清理已处理/已忽略且长时间未复现的告警
     *
     * @param days 保留天数
     */
    void cleanExpiredAlerts(int days);
}
