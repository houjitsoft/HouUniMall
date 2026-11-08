package com.houjiang.logging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.logging.dto.LogAccessQueryDTO;
import com.houjiang.logging.entity.LogAccessEntity;
import com.houjiang.logging.mapper.LogAccessMapper;
import com.houjiang.logging.service.LogAccessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 访问日志 Service 实现类
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@Service
public class LogAccessServiceImpl extends ServiceImpl<LogAccessMapper, LogAccessEntity>
        implements LogAccessService {

    /**
     * 分页查询访问日志
     *
     * @param dto     查询条件 DTO
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页结果
     */
    @Override
    public IPage<LogAccessEntity> queryPage(LogAccessQueryDTO dto, long current, long size) {
        QueryWrapper<LogAccessEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like("username", dto.getUsername());
        }
        if (StringUtils.hasText(dto.getMethod())) {
            wrapper.eq("method", dto.getMethod());
        }
        if (StringUtils.hasText(dto.getPath())) {
            wrapper.like("path", dto.getPath());
        }
        if (dto.getStatusCode() != null) {
            wrapper.eq("status_code", dto.getStatusCode());
        }
        if (dto.getStartTime() != null) {
            wrapper.ge("create_time", dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le("create_time", dto.getEndTime());
        }
        if (StringUtils.hasText(dto.getIp())) {
            wrapper.eq("ip", dto.getIp());
        }
        wrapper.orderByDesc("create_time");
        return page(new Page<>(current, size), wrapper);
    }

    /**
     * 保存访问日志
     *
     * @param log 访问日志实体
     */
    @Override
    public void saveLog(LogAccessEntity log) {
        save(log);
    }

    /**
     * 清理过期访问日志
     *
     * @param days 保留天数
     */
    @Override
    public void cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        QueryWrapper<LogAccessEntity> wrapper = new QueryWrapper<>();
        wrapper.lt("create_time", expireTime);
        int count = baseMapper.delete(wrapper);
        log.info("[LogAccessService] 清理过期访问日志 {} 条（保留{}天内）", count, days);
    }
}
