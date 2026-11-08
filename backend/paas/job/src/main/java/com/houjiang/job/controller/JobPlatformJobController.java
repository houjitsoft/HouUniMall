package com.houjiang.job.controller;


import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.dto.JobDTO;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Amos
 */
@RestController
@RequestMapping("job")
public class JobPlatformJobController extends BaseController {


    /**
     * 查询平台所有定时任务
     *
     * @param paraMap
     * @return
     */
    @PostMapping("context/job/getAllJob")
    public ResponseResult queryJob(@RequestBody Map<String, String> paraMap) {
        // 调用服务层方法，查询所有定时任务
        return jobPlatformService.queryJob(paraMap);
    }

    /**
     * 查询启动中的定时任务
     *
     * @param job
     * @return
     */
    @PostMapping("/monitor/job/list")
    public ResponseResult list(@RequestBody JobDTO job) {
        // 调用平台服务的查询方法，传入对象，返回所有匹配的记录列表
        return jobPlatformService.selectJobAll(job);
    }

    /**
     * 添加 定时任务
     *
     * @param job
     * @return
     */
    @PostMapping("/monitor/job/add")
    @SysLog(value = "新增定时任务", module = "job")
    public ResponseResult addJob(@RequestBody JobDTO job) {
        // 验证作业名称是否为空或仅包含空格，如果是，则返回错误响应，指示未设置作业名称
        if (job.getJobName() == null || "".equals(job.getJobName().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_NAME);
        }
        // 验证调用目标是否为空或仅包含空格，如果是，则返回错误响应，指示未设置作业调用目标
        else if (job.getInvokeTarget() == null || "".equals(job.getInvokeTarget().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_INVOKETARGET);
        }
        // 如果设置了月执行计划但内容为空或仅包含空格，返回错误响应，指示未设置作业的月执行计划
        else if (job.getMouth() != null && "".equals(job.getMouth().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_MOUTH);
        }
        // 如果设置了周执行计划但内容为空或仅包含空格，返回错误响应，指示未设置作业的周执行计划
        else if (job.getWeek() != null && "".equals(job.getWeek().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_WEEK);
        }
        // 如果设置了日执行计划但内容为空或仅包含空格，返回错误响应，指示未设置作业的日执行计划
        else if (job.getDay() != null && "".equals(job.getDay().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_DAY);
        }
        // 如果设置了小时执行计划但内容为空或仅包含空格，返回错误响应，指示未设置作业的小时执行计划
        else if (job.getHour() != null && "".equals(job.getHour().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_HOUR);
        }
        // 如果设置了分钟执行计划但内容为空或仅包含空格，返回错误响应，指示未设置作业的分钟执行计划
        else if (job.getMin() != null && "".equals(job.getMin().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_MIN);
        }
        // 如果所有验证通过，则添加作业到作业平台
        else {
            return jobPlatformService.addJob(job);
        }

    }

    /**
     * 修改定时任务
     *
     * @param job
     * @return
     */
    @PostMapping("/monitor/job/editJobById")
    @SysLog(value = "修改定时任务", module = "job")
    public ResponseResult editJobById(@RequestBody JobDTO job) throws SchedulerException {
        // 验证任务ID是否为空，如果为空，则任务参数不合法
        if (job.getJobId() == null) {
            return ResponseResult.fail(BizCodeEnume.INVALID_PARAMETER);
        }
        // 验证任务名称是否为空或仅包含空格，如果为空，则任务名称未设置
        else if (job.getJobName() == null || "".equals(job.getJobName().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_NAME);
        }
        // 验证调用目标是否为空或仅包含空格，如果为空，则调用目标未设置
        else if (job.getInvokeTarget() == null || "".equals(job.getInvokeTarget().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_INVOKETARGET);
        }
        // 如果任务执行周期类型为月，并且周期值为空或仅包含空格，则任务月周期未设置
        else if (job.getMouth() != null && "".equals(job.getMouth().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_MOUTH);
        }
        // 如果任务执行周期类型为周，并且周期值为空或仅包含空格，则任务周周期未设置
        else if (job.getWeek() != null && "".equals(job.getWeek().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_WEEK);
        }
        // 如果任务执行周期类型为日，并且周期值为空或仅包含空格，则任务日周期未设置
        else if (job.getDay() != null && "".equals(job.getDay().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_DAY);
        }
        // 如果任务执行周期类型为小时，并且周期值为空或仅包含空格，则任务小时周期未设置
        else if (job.getHour() != null && "".equals(job.getHour().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_HOUR);
        }
        // 如果任务执行周期类型为分钟，并且周期值为空或仅包含空格，则任务分钟周期未设置
        else if (job.getMin() != null && "".equals(job.getMin().trim())) {
            return ResponseResult.fail(BizCodeEnume.NO_SET_JOB_MIN);
        }
        // 所有验证通过后，调用服务层方法修改任务信息
        else {
            return jobPlatformService.editJobById(job);
        }

    }

    /**
     * 删除定时任务
     *
     * @param job
     * @return
     */
    @PostMapping("/monitor/job/deleteJobById")
    @SysLog(value = "删除定时任务", module = "job")
    public ResponseResult deleteJobById(@RequestBody JobDTO job) throws Exception {
        // 调用平台服务，通过ID删除定时任务
        return jobPlatformService.deleteJobById(job);
    }

    /**
     * 暂停 / 恢复 定时任务
     *
     * @param job
     * @return
     */
    @PostMapping("/monitor/job/stopJobById")
    @SysLog(value = "启停定时任务", module = "job")
    public ResponseResult stopJobById(@RequestBody JobDTO job) throws Exception {
        // 调用平台服务，暂停 / 恢复 定时任务
        return jobPlatformService.stopJobById(job);
    }

}
