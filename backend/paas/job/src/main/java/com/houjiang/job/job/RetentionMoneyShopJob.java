package com.houjiang.job.job;


import com.houjiang.job.service.RetentionmoneyService;
import lombok.SneakyThrows;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

//更新店铺质保金基础额度定时任务

/**
 * @author Amos
 */
public class RetentionMoneyShopJob implements BaseJob {

    @Autowired
    RetentionmoneyService retentionmoneyService;

    @SneakyThrows
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        //质保金基础额度更新
        retentionmoneyService.updatebasicquota();
    }
}
