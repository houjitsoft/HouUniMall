package com.houjiang.logging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.logging.dto.LogAccessQueryDTO;
import com.houjiang.logging.entity.LogAccessEntity;

/**
 * 访问日志 Service 接口
 *
 * <p>提供访问日志的分页查询、保存及过期清理功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
public interface LogAccessService extends IService<LogAccessEntity> {

    /**
     * 分页查询访问日志
     *
     * @param dto     查询条件 DTO
     * @param current 当前页码（从1开始）
     * @param size    每页记录数
     * @return 分页结果
     */
    IPage<LogAccessEntity> queryPage(LogAccessQueryDTO dto, long current, long size);

    /**
     * 保存访问日志
     *
     * @param log 访问日志实体
     */
    void saveLog(LogAccessEntity log);

    /**
     * 清理过期访问日志
     *
     * @param days 保留天数
     */
    void cleanExpiredLogs(int days);
}
