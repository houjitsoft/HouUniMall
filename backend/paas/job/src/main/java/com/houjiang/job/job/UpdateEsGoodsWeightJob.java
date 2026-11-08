package com.houjiang.job.job;

import com.houjiang.job.feign.GoodsServiceClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName UpdateEsGoodsWeightJob
 * @Description 更新ES索引库
 * @Author Amos
 * @Date 2023/5/17 14:45
 * @Version 1.0
 * @author Amos
 */
public class UpdateEsGoodsWeightJob implements BaseJob {

    @Autowired
    private GoodsServiceClient goodsServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        //导入商品数据
        goodsServiceClient.importData();

        //导入店铺数据
        goodsServiceClient.importShopData();

        //导入敏感词数据
        goodsServiceClient.importSensitiveData();
    }
}
