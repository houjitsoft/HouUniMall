package com.houjiang.job.job;

import com.houjiang.job.feign.WebPosServiceClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Resource;

/**
 * @author Amos
 */
public class UpdateOrderStatusJob implements BaseJob {
    private static final Logger logger = LoggerFactory.getLogger(UpdateOrderStatusJob.class);

    @Resource
    private WebPosServiceClient webPosServiceClient;


    /**
     * 执行作业的逻辑方法。
     * 该方法旨在定时检查并更新支付宝或微信支付的订单状态。
     *
     * @param context 作业执行的上下文，包含作业相关的所有信息和配置。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 记录作业开始执行的信息
        logger.info("updateOrderStatus start");

        // webpos 是可选商业插件（方案 §1.4.4）：未安装时服务不可达。
        // 这里按「插件缺失」降级跳过，不得抛 JobExecutionException 造成定时任务反复报错。
        try {
            // 调用WebPOS服务客户端的updateOrderStatus方法，更新订单状态
            webPosServiceClient.updateOrderStatus();
        } catch (Exception e) {
            logger.warn("webpos 插件未安装或不可用，跳过本次订单状态更新: {}", e.getMessage());
            return;
        }

        // 记录作业执行结束的信息
        logger.info("updateOrderStatus end");
    }
}
