package com.houjiang.logging.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.logging.dto.LogLoginQueryDTO;
import com.houjiang.logging.entity.LogLoginEntity;

/**
 * 登录日志 Service 接口
 *
 * <p>提供登录日志的分页查询、保存及过期清理功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
public interface LogLoginService extends IService<LogLoginEntity> {

    /**
     * 分页查询登录日志
     *
     * @param dto     查询条件 DTO
     * @param current 当前页码（从1开始）
     * @param size    每页记录数
     * @return 分页结果
     */
    IPage<LogLoginEntity> queryPage(LogLoginQueryDTO dto, long current, long size);

    /**
     * 保存登录日志
     *
     * @param log 登录日志实体
     */
    void saveLog(LogLoginEntity log);

    /**
     * 清理过期登录日志
     *
     * @param days 保留天数
     */
    void cleanExpiredLogs(int days);
}
