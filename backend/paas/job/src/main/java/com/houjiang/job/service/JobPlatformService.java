package com.houjiang.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.dto.JobDTO;
import com.houjiang.job.entity.JobPlatformEntity;
import org.quartz.SchedulerException;

import java.util.Map;

/**
 * @author Amos
 */
public interface JobPlatformService extends IService<JobPlatformEntity> {

    /**
     * 查询平台所有定时任务
     *
     * @param paraMap
     * @return
     */
    ResponseResult queryJob(Map<String, String> paraMap);

    /**
     * 查询启动中的定时任务
     *
     * @param job
     * @return
     */
    ResponseResult selectJobAll(JobDTO job);

    /**
     * 添加 定时任务
     *
     * @param job
     * @return
     */
    ResponseResult addJob(JobDTO job);

    /**
     * 修改定时任务
     *
     * @param job
     * @return
     */
    ResponseResult editJobById(JobDTO job) throws SchedulerException;

    /**
     * 删除定时任务
     *
     * @param job
     * @return
     * @throws Exception
     */
    ResponseResult deleteJobById(JobDTO job) throws Exception;

    /**
     * 暂停 / 恢复 定时任务
     *
     * @param job
     * @return
     * @throws Exception
     */
    ResponseResult stopJobById(JobDTO job) throws Exception;
}
