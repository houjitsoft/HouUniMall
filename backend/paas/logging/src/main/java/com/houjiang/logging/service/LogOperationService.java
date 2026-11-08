package com.houjiang.logging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.logging.dto.LogOperationQueryDTO;
import com.houjiang.logging.entity.LogOperationEntity;

/**
 * 操作日志 Service 接口
 *
 * <p>提供操作日志的分页查询、保存及过期清理功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
public interface LogOperationService extends IService<LogOperationEntity> {

    /**
     * 分页查询操作日志
     *
     * <p>根据 DTO 中非空字段动态构建查询条件，按 create_time 倒序排列。</p>
     *
     * @param dto     查询条件 DTO
     * @param current 当前页码（从1开始）
     * @param size    每页记录数
     * @return 分页结果
     */
    IPage<LogOperationEntity> queryPage(LogOperationQueryDTO dto, long current, long size);

    /**
     * 保存操作日志
     *
     * @param log 操作日志实体
     */
    void saveLog(LogOperationEntity log);

    /**
     * 清理过期操作日志
     *
     * <p>删除创建时间早于当前时间减去指定天数的日志记录。</p>
     *
     * @param days 保留天数，超过此天数的记录将被删除
     */
    void cleanExpiredLogs(int days);
}
