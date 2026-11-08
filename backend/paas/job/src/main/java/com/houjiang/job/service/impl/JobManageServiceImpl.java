package com.houjiang.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.querypage.PageUtils;
import com.houjiang.job.dto.JobDTO;
import com.houjiang.job.entity.JobExecutionLogEntity;
import com.houjiang.job.entity.JobPlatformEntity;
import com.houjiang.job.mapper.JobPlatformMapper;
import com.houjiang.job.service.JobExecutionLogService;
import com.houjiang.job.service.JobManageService;
import com.houjiang.job.service.JobPlatformService;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Amos
 */
@Service
public class JobManageServiceImpl implements JobManageService {

    private static final List<String> MODULE_ORDER = List.of(
            "订单模块", "支付模块", "商品模块", "用户模块", "消息模块", "物流模块", "系统模块"
    );

    @Autowired
    private JobPlatformMapper jobPlatformMapper;

    @Autowired
    private JobPlatformService jobPlatformService;

    @Autowired
    private JobPlatformServiceImpl jobPlatformServiceImpl;

    @Autowired
    private JobExecutionLogService jobExecutionLogService;

    @Autowired
    @Qualifier("Scheduler")
    private Scheduler scheduler;

    @Override
    public ResponseResult stats() {
        Map<String, Object> data = new HashMap<>();
        long total = jobPlatformMapper.selectCount(null);
        long enabled = jobPlatformMapper.selectCount(new LambdaQueryWrapper<JobPlatformEntity>().eq(JobPlatformEntity::getStatus, "0"));
        data.put("running", enabled);
        data.put("scheduled", total);
        data.put("todayCompleted", countTodayLog("success"));
        data.put("todayFailed", countTodayLog("failed"));
        return ResponseResult.success(data);
    }

    private long countTodayLog(String status) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return jobExecutionLogService.count(new LambdaQueryWrapper<JobExecutionLogEntity>()
                .eq(JobExecutionLogEntity::getStatus, status)
                .ge(JobExecutionLogEntity::getStartTime, cal.getTime()));
    }

    @Override
    public ResponseResult list(Map<String, Object> params) {
        int page = parseInt(params.get("page"), 1);
        int limit = parseInt(params.get("limit"), 20);
        String keyword = params.get("keyword") != null ? String.valueOf(params.get("keyword")) : null;
        String statusFilter = params.get("status") != null ? String.valueOf(params.get("status")) : null;
        String moduleFilter = params.get("module") != null ? String.valueOf(params.get("module")).trim() : null;
        boolean filterByModule = StringUtils.hasText(moduleFilter)
                && !"全部".equals(moduleFilter)
                && !"ALL".equalsIgnoreCase(moduleFilter);

        LambdaQueryWrapper<JobPlatformEntity> wrapper = buildListWrapper(keyword, statusFilter);

        if (filterByModule) {
            List<Map<String, Object>> filtered = jobPlatformService.list(wrapper).stream()
                    .map(this::toTaskVo)
                    .filter(vo -> moduleFilter.equals(vo.get("module")))
                    .collect(Collectors.toList());
            return ResponseResult.success(buildPageResult(filtered, page, limit));
        }

        IPage<JobPlatformEntity> pageResult = jobPlatformService.page(new Page<>(page, limit), wrapper);
        List<Map<String, Object>> list = pageResult.getRecords().stream()
                .map(this::toTaskVo)
                .collect(Collectors.toList());

        Map<String, Object> result = buildPageResult(list, page, limit);
        result.put("totalCount", pageResult.getTotal());
        result.put("totalPage", (int) Math.ceil((double) pageResult.getTotal() / limit));
        return ResponseResult.success(result);
    }

    @Override
    public ResponseResult modules() {
        List<JobPlatformEntity> all = jobPlatformService.list(
                new LambdaQueryWrapper<JobPlatformEntity>().orderByDesc(JobPlatformEntity::getJobId));
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (String module : MODULE_ORDER) {
            countMap.put(module, 0L);
        }
        for (JobPlatformEntity entity : all) {
            String module = deriveModule(entity.getInvokeTarget());
            countMap.merge(module, 1L, Long::sum);
        }

        List<Map<String, Object>> modules = new ArrayList<>();
        Map<String, Object> allTab = new HashMap<>();
        allTab.put("name", "全部");
        allTab.put("value", "ALL");
        allTab.put("count", all.size());
        modules.add(allTab);
        for (String module : MODULE_ORDER) {
            long count = countMap.getOrDefault(module, 0L);
            if (count <= 0) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("name", module);
            item.put("value", module);
            item.put("count", count);
            modules.add(item);
        }
        return ResponseResult.success(modules);
    }

    private LambdaQueryWrapper<JobPlatformEntity> buildListWrapper(String keyword, String statusFilter) {
        LambdaQueryWrapper<JobPlatformEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(JobPlatformEntity::getJobName, keyword)
                    .or().like(JobPlatformEntity::getInvokeTarget, keyword));
        }
        if ("running".equals(statusFilter)) {
            wrapper.eq(JobPlatformEntity::getStatus, "0");
        } else if ("paused".equals(statusFilter)) {
            wrapper.eq(JobPlatformEntity::getStatus, "1");
        }
        wrapper.orderByDesc(JobPlatformEntity::getJobId);
        return wrapper;
    }

    private Map<String, Object> buildPageResult(List<Map<String, Object>> allRows, int page, int limit) {
        long total = allRows.size();
        int from = Math.max(0, (page - 1) * limit);
        int to = Math.min(from + limit, allRows.size());
        List<Map<String, Object>> pageList = from < allRows.size()
                ? new ArrayList<>(allRows.subList(from, to))
                : Collections.emptyList();

        Map<String, Object> result = new HashMap<>();
        result.put("list", pageList);
        result.put("totalCount", total);
        result.put("pageSize", limit);
        result.put("currPage", page);
        result.put("totalPage", limit > 0 ? (int) Math.ceil((double) total / limit) : 0);
        return result;
    }

    @Override
    public ResponseResult detail(Map<String, Object> params) {
        Long jobId = Long.valueOf(String.valueOf(params.get("jobId")));
        JobPlatformEntity entity = jobPlatformMapper.selectById(jobId);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "任务不存在");
        }
        return ResponseResult.success(toTaskVo(entity));
    }

    @Override
    public ResponseResult create(Map<String, Object> params) {
        String name = String.valueOf(params.get("name"));
        String invokeTarget = String.valueOf(params.get("invokeTarget"));
        String cron = String.valueOf(params.get("cron"));
        if (!StringUtils.hasText(name) || !StringUtils.hasText(invokeTarget) || !StringUtils.hasText(cron)) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "任务名称、调用目标和Cron表达式不能为空");
        }
        long exists = jobPlatformMapper.selectCount(new LambdaQueryWrapper<JobPlatformEntity>()
                .eq(JobPlatformEntity::getInvokeTarget, invokeTarget));
        if (exists > 0) {
            return ResponseResult.fail(BizCodeEnume.JOB_IS_EXIST);
        }
        JobDTO job = new JobDTO();
        job.setJobName(name);
        job.setJobGroup("1");
        job.setInvokeTarget(invokeTarget);
        job.setCronExpression(cron);
        job.setStatus("0");
        job.setRemark(params.get("description") != null ? String.valueOf(params.get("description")) : "");
        job.setCreateBy("系统管理员");
        job.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        jobPlatformMapper.insertJob(job);
        try {
            jobPlatformServiceImpl.addJob(invokeTarget, "1", cron);
        } catch (Exception e) {
            return ResponseResult.fail(BizCodeEnume.JOB_NO_YET_CLOSED);
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult update(Map<String, Object> params) {
        Long jobId = Long.valueOf(String.valueOf(params.get("id")));
        JobPlatformEntity entity = jobPlatformMapper.selectById(jobId);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "任务不存在");
        }
        if (params.get("name") != null) {
            entity.setJobName(String.valueOf(params.get("name")));
        }
        if (params.get("cron") != null) {
            entity.setCronExpression(String.valueOf(params.get("cron")));
        }
        if (params.get("description") != null) {
            entity.setRemark(String.valueOf(params.get("description")));
        }
        jobPlatformMapper.updateById(entity);
        return ResponseResult.success();
    }

    @Override
    public ResponseResult delete(Long jobId) {
        JobPlatformEntity entity = jobPlatformMapper.selectById(jobId);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "任务不存在");
        }
        JobDTO job = new JobDTO();
        job.setJobId(jobId);
        jobPlatformMapper.deleteJobById(job);
        try {
            jobPlatformServiceImpl.deleteJob(entity.getInvokeTarget(), entity.getJobGroup());
        } catch (Exception e) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), e.getMessage());
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult pause(Long jobId) {
        return changeJobStatus(jobId, true);
    }

    @Override
    public ResponseResult resume(Long jobId) {
        return changeJobStatus(jobId, false);
    }

    @Override
    public ResponseResult trigger(Long jobId) {
        JobPlatformEntity entity = jobPlatformMapper.selectById(jobId);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "任务不存在");
        }
        try {
            JobKey jobKey = JobKey.jobKey(entity.getInvokeTarget(), entity.getJobGroup());
            scheduler.triggerJob(jobKey);
            return ResponseResult.success();
        } catch (Exception e) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "触发执行失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseResult toggleEnabled(Long jobId, Boolean enabled) {
        if (Boolean.TRUE.equals(enabled)) {
            return resume(jobId);
        }
        return pause(jobId);
    }

    private ResponseResult changeJobStatus(Long jobId, boolean pause) {
        JobPlatformEntity entity = jobPlatformMapper.selectById(jobId);
        if (entity == null) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), "任务不存在");
        }
        JobDTO job = new JobDTO();
        job.setJobId(jobId);
        job.setInvokeTarget(entity.getInvokeTarget());
        job.setJobGroup(entity.getJobGroup());
        job.setCronExpression(entity.getCronExpression());
        job.setToken("internal");
        try {
            if (pause && "0".equals(entity.getStatus())) {
                return jobPlatformService.stopJobById(job);
            }
            if (!pause && "1".equals(entity.getStatus())) {
                return jobPlatformService.stopJobById(job);
            }
            return ResponseResult.success();
        } catch (Exception e) {
            return ResponseResult.fail(BizCodeEnume.VALID_EXCEPTION.getCode(), e.getMessage());
        }
    }

    private Map<String, Object> toTaskVo(JobPlatformEntity entity) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", entity.getJobId());
        vo.put("name", entity.getJobName());
        vo.put("module", deriveModule(entity.getInvokeTarget()));
        vo.put("type", "cron");
        vo.put("status", "0".equals(entity.getStatus()) ? "running" : "paused");
        vo.put("cron", entity.getCronExpression());
        vo.put("invokeTarget", entity.getInvokeTarget());
        vo.put("description", entity.getRemark() != null ? entity.getRemark() : "");
        vo.put("enabled", "0".equals(entity.getStatus()));
        vo.put("lastExecuteTime", getLastExecuteTime(entity.getInvokeTarget()));
        vo.put("nextExecuteTime", getNextExecuteTime(entity));
        vo.put("createdAt", entity.getCreateTime());
        vo.put("updatedAt", entity.getCreateTime());
        return vo;
    }

    private String deriveModule(String invokeTarget) {
        if (invokeTarget == null) {
            return "系统模块";
        }
        String lower = invokeTarget.toLowerCase();
        if (lower.contains("order")) return "订单模块";
        if (lower.contains("pay") || lower.contains("ali") || lower.contains("wx")) return "支付模块";
        if (lower.contains("goods") || lower.contains("es")) return "商品模块";
        if (lower.contains("user") || lower.contains("member") || lower.contains("plus")) return "用户模块";
        if (lower.contains("im")) return "消息模块";
        if (lower.contains("express") || lower.contains("logistics")) return "物流模块";
        return "系统模块";
    }

    private String getLastExecuteTime(String invokeTarget) {
        JobExecutionLogEntity log = jobExecutionLogService.getOne(new LambdaQueryWrapper<JobExecutionLogEntity>()
                .eq(JobExecutionLogEntity::getInvokeTarget, invokeTarget)
                .in(JobExecutionLogEntity::getStatus, "success", "failed")
                .orderByDesc(JobExecutionLogEntity::getStartTime)
                .last("LIMIT 1"));
        if (log == null || log.getStartTime() == null) {
            return "-";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(log.getStartTime());
    }

    private String getNextExecuteTime(JobPlatformEntity entity) {
        if (!"0".equals(entity.getStatus())) {
            return "-";
        }
        try {
            Trigger trigger = scheduler.getTrigger(org.quartz.TriggerKey.triggerKey(entity.getInvokeTarget(), entity.getJobGroup()));
            if (trigger instanceof CronTrigger cronTrigger) {
                Date next = cronTrigger.getNextFireTime();
                if (next != null) {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(next);
                }
            }
        } catch (Exception ignored) {
        }
        return "-";
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
