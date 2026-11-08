package com.houjiang.logging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.logging.dto.LogOperationQueryDTO;
import com.houjiang.logging.entity.LogOperationEntity;
import com.houjiang.logging.mapper.LogOperationMapper;
import com.houjiang.logging.service.LogOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 操作日志 Service 实现类
 *
 * <p>实现操作日志的分页查询、保存、及过期数据清理功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@Service
public class LogOperationServiceImpl extends ServiceImpl<LogOperationMapper, LogOperationEntity>
        implements LogOperationService {

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
    @Override
    public IPage<LogOperationEntity> queryPage(LogOperationQueryDTO dto, long current, long size) {
        QueryWrapper<LogOperationEntity> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getUsername())) {
            wrapper.like("username", dto.getUsername());
        }
        if (StringUtils.hasText(dto.getModule())) {
            wrapper.eq("module", dto.getModule());
        }
        if (StringUtils.hasText(dto.getOperation())) {
            wrapper.like("operation", dto.getOperation());
        }
        if (dto.getStatus() != null) {
            wrapper.eq("status", dto.getStatus());
        }
        if (dto.getStartTime() != null) {
            wrapper.ge("create_time", dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            wrapper.le("create_time", dto.getEndTime());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like("request_url", dto.getKeyword())
                    .or().like("operation", dto.getKeyword()));
        }
        wrapper.orderByDesc("create_time");
        return page(new Page<>(current, size), wrapper);
    }

    /**
     * 保存操作日志
     *
     * @param log 操作日志实体
     */
    @Override
    public void saveLog(LogOperationEntity log) {
        save(log);
    }

    /**
     * 清理过期操作日志
     *
     * <p>删除创建时间早于当前时间减去指定天数的日志记录。</p>
     *
     * @param days 保留天数，超过此天数的记录将被删除
     */
    @Override
    public void cleanExpiredLogs(int days) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);
        QueryWrapper<LogOperationEntity> wrapper = new QueryWrapper<>();
        wrapper.lt("create_time", expireTime);
        int count = baseMapper.delete(wrapper);
        log.info("[LogOperationService] 清理过期操作日志 {} 条（保留{}天内）", count, days);
    }
}
