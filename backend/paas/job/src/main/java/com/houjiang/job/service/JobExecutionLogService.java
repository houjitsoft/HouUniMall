package com.houjiang.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.entity.JobExecutionLogEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface JobExecutionLogService extends IService<JobExecutionLogEntity> {

    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    ResponseResult list(Map<String, Object> params);

    /**
     * TODO 补充方法说明
     * @param id TODO
     * @return TODO
     */
    ResponseResult detail(Long id);

    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    ResponseResult clear(Map<String, Object> params);

    /**
     * TODO 补充方法说明
     * @param jobName TODO
     * @param invokeTarget TODO
     * @param jobId TODO
     */
    void recordStart(String jobName, String invokeTarget, Long jobId);

    /**
     * TODO 补充方法说明
     * @param invokeTarget TODO
     * @param success TODO
     * @param errorMsg TODO
     */
    void recordFinish(String invokeTarget, boolean success, String errorMsg);
}
