package com.houjiang.job.job;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.service.DcenterJobService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * @ClassName DcenterJob
 * @Description
 * @Author Amos
 * @Date 2025/1/13 13:32
 * @Version 1.0
 * @author Amos
 */
@DisallowConcurrentExecution
public class DcenterJob implements BaseJob{

    @Autowired
    private DcenterJobService dcenterJobService;


    private static final Logger logger = LoggerFactory.getLogger(DcenterJob.class);
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info("开始更新数据大屏数据库数据---------");
        CompletableFuture<ResponseResult<?>> future = dcenterJobService.updateDcenterData();
        future.thenAccept(responseResult -> {
            // 处理异步任务完成后的结果
            logger.info("异步任务完成: {}", responseResult);
        }).exceptionally(ex -> {
            // 处理异常
            logger.error("异步任务发生异常: ", ex);
            return null;
        });
//        dcenterJobService.updateDcenterData();
        logger.info("结束更新数据大屏数据库数据---------");


    }
}
