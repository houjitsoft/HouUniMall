package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.thirdparty.entity.message.JPushHistoryEntity;
import com.houjiang.thirdparty.mapper.message.JPushHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 极光推送历史服务
 *
 * @author Amos
 * @since 2025-07-02
 */
@Slf4j
@Service
public class JPushHistoryService {

    @Autowired
    private JPushHistoryMapper jPushHistoryMapper;

    /**
     * 分页查询推送历史
     */
    public Map<String, Object> getHistoryPage(String orgId, Integer page, Integer size,
                                               String pushType, Integer sendStatus) {
        LambdaQueryWrapper<JPushHistoryEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orgId)) {
            wrapper.eq(JPushHistoryEntity::getOrgId, orgId);
        }
        if (StringUtils.hasText(pushType)) {
            wrapper.eq(JPushHistoryEntity::getPushType, pushType);
        }
        if (sendStatus != null) {
            wrapper.eq(JPushHistoryEntity::getSendStatus, sendStatus);
        }
        wrapper.orderByDesc(JPushHistoryEntity::getCreateTime);

        Page<JPushHistoryEntity> pageResult = jPushHistoryMapper.selectPage(
                new Page<>(page != null ? page : 1, size != null ? size : 20), wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());
        data.put("page", pageResult.getCurrent());
        data.put("size", pageResult.getSize());
        return data;
    }

    /**
     * 获取推送统计
     */
    public Map<String, Object> getStatistics(String orgId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<JPushHistoryEntity> baseWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orgId)) {
            baseWrapper.eq(JPushHistoryEntity::getOrgId, orgId);
        }
        long total = jPushHistoryMapper.selectCount(baseWrapper);

        // 今日推送数
        LambdaQueryWrapper<JPushHistoryEntity> todayWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orgId)) {
            todayWrapper.eq(JPushHistoryEntity::getOrgId, orgId);
        }
        todayWrapper.ge(JPushHistoryEntity::getCreateTime, LocalDateTime.now().toLocalDate().atStartOfDay());
        long todayCount = jPushHistoryMapper.selectCount(todayWrapper);

        // 成功数
        LambdaQueryWrapper<JPushHistoryEntity> successWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orgId)) {
            successWrapper.eq(JPushHistoryEntity::getOrgId, orgId);
        }
        successWrapper.eq(JPushHistoryEntity::getSendStatus, 1);
        long successCount = jPushHistoryMapper.selectCount(successWrapper);

        // 失败数
        LambdaQueryWrapper<JPushHistoryEntity> failWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orgId)) {
            failWrapper.eq(JPushHistoryEntity::getOrgId, orgId);
        }
        failWrapper.eq(JPushHistoryEntity::getSendStatus, 2);
        long failCount = jPushHistoryMapper.selectCount(failWrapper);

        stats.put("total", total);
        stats.put("todayCount", todayCount);
        stats.put("successCount", successCount);
        stats.put("failCount", failCount);
        stats.put("successRate", total > 0 ? Math.round(successCount * 10000.0 / total) / 100.0 : 0);
        return stats;
    }

    /**
     * 根据ID获取推送历史详情
     *
     * @param id 记录ID
     * @return 推送历史实体
     */
    public JPushHistoryEntity getById(String orgId, Long id) {
        if (!StringUtils.hasText(orgId) || id == null) {
            return null;
        }
        LambdaQueryWrapper<JPushHistoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JPushHistoryEntity::getId, id)
                .eq(JPushHistoryEntity::getOrgId, orgId);
        return jPushHistoryMapper.selectOne(wrapper);
    }

    /**
     * 保存推送历史
     */
    public boolean saveHistory(JPushHistoryEntity entity) {
        entity.setCreateTime(LocalDateTime.now());
        return jPushHistoryMapper.insert(entity) > 0;
    }

    /**
     * 更新推送状态
     */
    public boolean updateSendStatus(String orgId, Long id, Integer sendStatus, String jpushSendId,
                                     Integer successCount, Integer failCount, String errorMsg) {
        if (!StringUtils.hasText(orgId) || id == null) {
            return false;
        }
        JPushHistoryEntity entity = new JPushHistoryEntity();
        entity.setId(id);
        entity.setSendStatus(sendStatus);
        entity.setJpushSendId(jpushSendId);
        entity.setSendTime(LocalDateTime.now());
        if (successCount != null) entity.setSuccessCount(successCount);
        if (failCount != null) entity.setFailCount(failCount);
        if (StringUtils.hasText(errorMsg)) entity.setErrorMsg(errorMsg);
        LambdaQueryWrapper<JPushHistoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JPushHistoryEntity::getId, id)
                .eq(JPushHistoryEntity::getOrgId, orgId);
        return jPushHistoryMapper.update(entity, wrapper) > 0;
    }
}
