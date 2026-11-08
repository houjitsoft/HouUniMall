package com.houjiang.job.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.entity.JobOrderNotCompletedEntity;

/**
 * @author Amos
 */
public interface JobOrderNotCompletedService extends IService<JobOrderNotCompletedEntity> {

    /**
     * 保存未完成订单
     * @param finishTime
     * @return
     */
    ResponseResult insertNotCompletedJobimplements(String finishTime);
}
