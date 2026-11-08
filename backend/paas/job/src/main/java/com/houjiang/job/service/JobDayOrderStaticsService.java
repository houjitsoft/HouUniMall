package com.houjiang.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.job.entity.JobDayOrderStaticsEntity;

import java.util.Date;
import java.util.Map;

/**
 * @author Amos
 */
public interface JobDayOrderStaticsService extends IService<JobDayOrderStaticsEntity> {

    /**
     * 订单日统计
     * @param start
     * @param end
     * @return
     */
    Map<String,Double> countStatics(Date start, Date end);
}
