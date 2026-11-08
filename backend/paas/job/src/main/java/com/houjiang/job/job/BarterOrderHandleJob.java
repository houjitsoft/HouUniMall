package com.houjiang.job.job;

import com.houjiang.commons.util.DateUtils;
import com.houjiang.job.feign.OrderServiceClient;
import jakarta.annotation.Resource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 处理超时的换货订单
 * @author Amos
 */
@DisallowConcurrentExecution
public class BarterOrderHandleJob implements BaseJob {

    private static final Logger logger = LoggerFactory.getLogger(BarterOrderHandleJob.class);

    @Resource
    private OrderServiceClient orderServiceClient;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("开始处理超时的换货订单---------");
        orderServiceClient.handleBarterOrderToJob();
        logger.info("更新处理超时的换货订单  更新时间{}", DateUtils.getNowDate());
    }

}
