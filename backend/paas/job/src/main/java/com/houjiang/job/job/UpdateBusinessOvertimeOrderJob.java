package com.houjiang.job.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ClassName UpdateBusinessOvertimeOrderJob
 * @Description 待发货退款超24小时商家未处理自动处理退款（废弃，已合并到RefundOrderHandleJob）
 * @Author Amos
 * @Date 2023/5/17 16:59
 * @Version 1.0
 * @author Amos
 */
public class UpdateBusinessOvertimeOrderJob implements BaseJob {
    /**
     * 日志器实例，用于记录更新业务超时订单作业的执行情况。
     */
    private static final Logger logger = LoggerFactory.getLogger(UpdateBusinessOvertimeOrderJob.class);

    /**
     * 注入更新订单状态服务，该服务负责实际的订单状态更新操作。
     */
    @Autowired
    private UpdateOrderStatusService updateOrderStatusService;

    /**
     * 执行更新业务超时订单作业的具体逻辑。
     * 此方法被Quartz调度器调用，用于定期检查并更新超时的业务订单状态。
     *
     * @param context 作业执行上下文，提供与当前作业执行相关的信息和功能。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 调用更新订单状态服务，执行更新业务超时订单的操作。
        updateOrderStatusService.updateBusinessOvertimeOrder();
    }

}
