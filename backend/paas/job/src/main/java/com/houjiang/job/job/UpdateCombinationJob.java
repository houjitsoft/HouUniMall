package com.houjiang.job.job;

import com.alibaba.fastjson.JSONObject;
import com.houjiang.job.feign.ActivityServiceClient;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName UpdateRebateJob
 * @Description 组合商品状态修改定时任务
 * @Author Amos
 * @Date 2023/10/11 14:30
 * @Version 1.0
 * @author Amos
 */
@DisallowConcurrentExecution
public class UpdateCombinationJob implements BaseJob {
    /**
     * 定时任务日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(UpdateCombinationJob.class);

    /**
     * 活动服务客户端，用于调用活动相关服务
     */
    @Autowired
    private ActivityServiceClient activityServiceClient;

    /**
     * 执行定时任务，更新组合商品活动状态。
     *
     * @param jobExecutionContext 作业执行上下文，包含作业相关的上下文信息。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 记录任务开始信息
        logger.info("开始更新组合商品活动状态---------");
        try {
            // 调用活动服务客户端，更新组合商品活动的基础状态
            activityServiceClient.editCombinationBaseStatusByJob();
            // 记录任务结束信息
            logger.info("结束更新组合商品活动状态------");
        } catch (Exception e) {
            // 获取异常堆栈信息
            StackTraceElement[] stackTrace = e.getStackTrace();
            // 将堆栈信息转换为JSON字符串，便于日志记录
            String jsonArrayStr = JSONObject.toJSONString(stackTrace);
            // 打印异常堆栈信息到标准错误输出
            e.printStackTrace();
            // 记录异常信息到日志
            logger.error("定时任务执行异常---日志输出，ERROR日志---{}", jsonArrayStr);
        }

    }

}

