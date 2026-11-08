package com.houjiang.logging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.logging.dto.LogErrorQueryDTO;
import com.houjiang.logging.entity.LogErrorEntity;

/**
 * 错误日志 Service 接口
 *
 * <p>提供错误日志的分页查询、保存及过期清理功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
public interface LogErrorService extends IService<LogErrorEntity> {

    /**
     * 分页查询错误日志
     *
     * @param dto     查询条件 DTO
     * @param current 当前页码（从1开始）
     * @param size    每页记录数
     * @return 分页结果
     */
    IPage<LogErrorEntity> queryPage(LogErrorQueryDTO dto, long current, long size);

    /**
     * 保存错误日志
     *
     * @param log 错误日志实体
     */
    void saveLog(LogErrorEntity log);

    /**
     * 清理过期错误日志
     *
     * @param days 保留天数
     */
    void cleanExpiredLogs(int days);
}
