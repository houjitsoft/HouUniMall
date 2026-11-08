package com.houjiang.job.job;

import com.houjiang.job.feign.GoodsServiceClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 京东商品信息处理定时任务
 * @ClassName JingDongCancelOrderJob
 * @Author Amos
 * @Date 2025/6/11 14:10
 * @Version 1.0
 * @author Amos
 */
public class JingDongGoodsUpdateJob implements BaseJob{

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 京东商品信息修改
        goodsServiceClient.jingDongGoodsInfopdateJob();
        // 京东新增或删除商品
        goodsServiceClient.jingDongAddOrDeleteGoodsJob();
        // 京东商品价格更新
        goodsServiceClient.jingDongUpdatePriceJob();
        // 京东SKU 上下架
        goodsServiceClient.jingDongGoodsSKUSaleStatusJob();
    }
}
