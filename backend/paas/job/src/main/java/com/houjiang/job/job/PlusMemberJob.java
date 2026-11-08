package com.houjiang.job.job;


import com.houjiang.job.service.JobUsermsService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

//plus会员定时任务

/**
 * @author Amos
 */
public class PlusMemberJob implements BaseJob {

    @Autowired
    JobUsermsService usermsService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        //会员到期更新
        usermsService.updateplus();

        //plus会员门槛红包发放
        usermsService.redgrant();

        //plus会员每月消费额度更新
        usermsService.updatelimit();

    }
}
