package com.houjiang.logging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.logging.dto.LogBusinessQueryDTO;
import com.houjiang.logging.entity.LogBusinessEntity;

/**
 * 业务日志 Service 接口
 *
 * <p>提供业务日志的分页查询、保存及过期清理功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
public interface LogBusinessService extends IService<LogBusinessEntity> {

    /**
     * 分页查询业务日志
     *
     * @param dto     查询条件 DTO
     * @param current 当前页码（从1开始）
     * @param size    每页记录数
     * @return 分页结果
     */
    IPage<LogBusinessEntity> queryPage(LogBusinessQueryDTO dto, long current, long size);

    /**
     * 保存业务日志
     *
     * @param log 业务日志实体
     */
    void saveLog(LogBusinessEntity log);

    /**
     * 清理过期业务日志
     *
     * @param days 保留天数
     */
    void cleanExpiredLogs(int days);
}
