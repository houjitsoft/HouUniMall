package com.houjiang.logging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.logging.dto.LogBusinessQueryDTO;
import com.houjiang.logging.entity.LogBusinessEntity;
import com.houjiang.logging.mapper.LogBusinessMapper;
import com.houjiang.logging.service.LogBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 业务日志 Service 实现类
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@Service
public class LogBusinessServiceImpl extends ServiceImpl<LogBusinessMapper, LogBusinessEntity>
        implements LogBusinessService {

    /**
     * 分页查询业务日志
     *
     * @param dto     查询条件 DTO
     * @param current 当前页码
     * @param size    每页记录数
     * @return 分页结果
     */
    @Override
    public IPage<LogBusinessEntity> queryPage(LogBusinessQueryDTO dto, long current, long size) {
        QueryWrapper<LogBusinessEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like("username", dto.getUsername());
        }
        if (StringUtils.hasText(dto.getModule())) {
            wrapper.eq("module", dto.getModule());
        }
        if (StringUtils.hasText(dto.getAction())) {
            wrapper.eq("action", dto.getAction());
        }
        if (StringUtils.hasText(dto.getTargetType())) {
            wrapper.eq("target_type", dto.getTargetType());
        }
        if (dto.getStartTime() != null) {
            wrapper.ge("create_time", dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le("create_time", dto.getEndTime());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like("detail", dto.getKeyword());
        }
        wrapper.orderByDesc("create_time");
        return page(new Page<>(current, size), wrapper);
    }

    /**
     * 保存业务日志
     *
     * @param log 业务日志实体
     */
    @Override
    public void saveLog(LogBusinessEntity log) {
        save(log);
    }

    /**
     * 清理过期业务日志
     *
     * @param days 保留天数
     */
    @Override
    public void cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        QueryWrapper<LogBusinessEntity> wrapper = new QueryWrapper<>();
        wrapper.lt("create_time", expireTime);
        int count = baseMapper.delete(wrapper);
        log.info("[LogBusinessService] 清理过期业务日志 {} 条（保留{}天内）", count, days);
    }
}
