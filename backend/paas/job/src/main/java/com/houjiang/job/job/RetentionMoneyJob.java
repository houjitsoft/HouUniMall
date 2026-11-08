package com.houjiang.job.job;


import com.houjiang.job.service.RetentionmoneyService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

//质保金定时任务

/**
 * @author Amos
 */
public class RetentionMoneyJob implements BaseJob {
    @Autowired
    RetentionmoneyService retentionmoneyService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        // 提醒商家处理失效定时任务
        retentionmoneyService.merchantprocessing();

        //商家超时未处理投诉单
        retentionmoneyService.merchantprocessingouttime();

        //更新商家是否需要缴纳质保金
        retentionmoneyService.updateshopmoney();

        //限权店铺
        retentionmoneyService.restrictedstore();

    }
}
