package com.houjiang.job.job;


import com.houjiang.job.service.FXService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Amos
 * @date 2021-06-0721:18
 * @Description: 微客用户佣金结算
 **/
@DisallowConcurrentExecution
public class VkOrderCommissionJob implements BaseJob {

    private static final Logger logger = LoggerFactory.getLogger(VkOrderCommissionJob.class);

    @Autowired
    private FXService fxServiceImpl;

    /**
     * 执行 VkOrderCommission 任务。
     * 此方法定义了作业执行的逻辑。它被作业调度器调用，以执行特定的业务逻辑：VkOrderCommission。
     *
     * @param context 提供有关作业执行上下文的信息，包括作业名称、作业实例ID等。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 记录任务开始信息
        logger.info("VkOrderCommission start");

        // 调用 vkOrderCommission 方法执行具体的业务逻辑
        fxServiceImpl.vkOrderCommission();

        // 记录任务结束信息
        logger.info("VkOrderCommission end");
    }

}
