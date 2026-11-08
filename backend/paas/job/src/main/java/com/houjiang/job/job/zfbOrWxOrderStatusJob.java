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
public class zfbOrWxOrderStatusJob implements BaseJob {
    private static final Logger logger = LoggerFactory.getLogger(zfbOrWxOrderStatusJob.class);

    @Resource
    private WebPosServiceClient webPosServiceClient;


    /**
     * 执行支付宝或微信订单状态检查任务。
     * 该方法作为作业执行器的一部分，负责定期检查支付宝和微信支付的订单状态，
     * 以确保系统与第三方支付平台保持同步。
     *
     * @param context 作业执行上下文，提供作业执行时的环境信息和状态。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 记录任务开始信息
        logger.info("zfbOrWxOrderStatus start");

        // webpos 是可选商业插件（方案 §1.4.4）：未安装时服务不可达。
        // 这里按「插件缺失」降级跳过，不得抛 JobExecutionException 造成定时任务反复报错。
        try {
            // 调用WebPOS服务客户端的zfbOrWxOrderStatus方法，检查支付宝或微信订单状态
            webPosServiceClient.zfbOrWxOrderStatus();
        } catch (Exception e) {
            logger.warn("webpos 插件未安装或不可用，跳过本次支付宝/微信订单状态检查: {}", e.getMessage());
            return;
        }

        // 记录任务结束信息
        logger.info("zfbOrWxOrderStatus end");
    }
}
