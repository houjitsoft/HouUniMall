package com.houjiang.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.job.entity.SysQueueEntity;
import com.houjiang.job.mapper.SysQueueMapper;
import com.houjiang.job.service.SysQueueService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class SysQueueServiceImpl extends ServiceImpl<SysQueueMapper, SysQueueEntity> implements SysQueueService {

    @Override
    public ResponseResult stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("running", countByStatus("running"));
        data.put("pending", countByStatus("pending"));
        data.put("completed", countByStatus("completed"));
        data.put("failed", countByStatus("failed"));
        return ResponseResult.success(data);
    }

    private long countByStatus(String status) {
        return this.count(new LambdaQueryWrapper<SysQueueEntity>().eq(SysQueueEntity::getStatus, status));
    }

    @Override
    public ResponseResult list(Map<String, Object> params) {
        int page = parseInt(params.get("page"), 1);
        int limit = parseInt(params.get("limit"), 20);
        String module = params.get("module") != null ? String.valueOf(params.get("module")) : null;
        String status = params.get("status") != null ? String.valueOf(params.get("status")) : null;
        String priority = params.get("priority") != null ? String.valueOf(params.get("priority")) : null;
        String keyword = params.get("keyword") != null ? String.valueOf(params.get("keyword")) : null;

        LambdaQueryWrapper<SysQueueEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(module)) {
            wrapper.eq(SysQueueEntity::getModule, module);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SysQueueEntity::getStatus, status);
        }
        if (StringUtils.hasText(priority)) {
            wrapper.eq(SysQueueEntity::getPriority, priority);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysQueueEntity::getName, keyword);
        }
        wrapper.orderByDesc(SysQueueEntity::getCreatedAt);

        IPage<SysQueueEntity> pageResult = this.page(new Page<>(page, limit), wrapper);
        return ResponseResult.success(PageUtils.getPage(pageResult));
    }

    @Override
    public ResponseResult cancel(Long id) {
        SysQueueEntity entity = this.getById(id);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "队列不存在");
        }
        if (!"running".equals(entity.getStatus()) && !"pending".equals(entity.getStatus())) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "仅待执行或执行中的队列可取消");
        }
        entity.setStatus("cancelled");
        entity.setUpdatedAt(new Date());
        this.updateById(entity);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult retry(Long id) {
        SysQueueEntity entity = this.getById(id);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "队列不存在");
        }
        if (!"failed".equals(entity.getStatus())) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "仅失败的队列可重试");
        }
        entity.setStatus("pending");
        entity.setProgress(0);
        entity.setErrorMsg("");
        entity.setRetryCount(entity.getRetryCount() != null ? entity.getRetryCount() + 1 : 1);
        entity.setUpdatedAt(new Date());
        entity.setStartedAt(null);
        entity.setFinishedAt(null);
        this.updateById(entity);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult executeNow(Long id) {
        SysQueueEntity entity = this.getById(id);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "队列不存在");
        }
        if (!"pending".equals(entity.getStatus())) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "仅待执行的队列可立即执行");
        }
        Date now = new Date();
        entity.setStatus("running");
        entity.setStartedAt(now);
        entity.setUpdatedAt(now);
        entity.setProgress(10);
        this.updateById(entity);
        // 模拟异步执行完成（实际业务由各模块入队后自行更新状态）
        simulateProgress(entity.getId());
        return ResponseResult.success();
    }

    @Override
    public ResponseResult batchCancel(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "请选择要取消的队列");
        }
        List<SysQueueEntity> list = this.listByIds(ids);
        Date now = new Date();
        for (SysQueueEntity entity : list) {
            if ("running".equals(entity.getStatus()) || "pending".equals(entity.getStatus())) {
                entity.setStatus("cancelled");
                entity.setUpdatedAt(now);
            }
        }
        this.updateBatchById(list);
        return ResponseResult.success();
    }

    private void simulateProgress(Long id) {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                SysQueueEntity entity = this.getById(id);
                if (entity != null && "running".equals(entity.getStatus())) {
                    entity.setStatus("completed");
                    entity.setProgress(100);
                    entity.setFinishedAt(new Date());
                    entity.setUpdatedAt(new Date());
                    this.updateById(entity);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private int parseInt(Object val, int defaultVal) {
        if (val == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
