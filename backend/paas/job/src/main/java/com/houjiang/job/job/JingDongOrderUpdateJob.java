package com.houjiang.job.job;

import com.houjiang.job.feign.GoodsServiceClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 京东订单处理定时任务
 * @ClassName JingDongCancelOrderJob
 * @Author Amos
 * @Date 2025/6/11 14:10
 * @Version 1.0
 * @author Amos
 */
public class JingDongOrderUpdateJob implements BaseJob{

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 京东取消订单
        goodsServiceClient.jingDongCancelOrderJob();
        // 京东发货
        goodsServiceClient.jingDongShipmentsJob();
        // 京东拆单
        goodsServiceClient.jingDongSplitOrderJob();
        // 京东确认订单
        goodsServiceClient.jingDongConfirmOrderJob();
        // 京东售后单修改
        goodsServiceClient.jingDongReurenOrderJob();
    }
}
