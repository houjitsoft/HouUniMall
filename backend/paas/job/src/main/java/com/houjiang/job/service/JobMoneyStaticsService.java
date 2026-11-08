package com.houjiang.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.job.entity.JobMoneyStaticsEntity;

/**
 * @author Amos
 */
public interface JobMoneyStaticsService extends IService<JobMoneyStaticsEntity> {

    /**
     * 查询每月激活未充值的总金额
     * @param mapHash
     */
    void InsertMoneyStatistport(String mapHash);
}
