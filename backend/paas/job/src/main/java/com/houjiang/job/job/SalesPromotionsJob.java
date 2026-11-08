package com.houjiang.job.job;


import com.houjiang.job.service.UpdateSalesPromStateService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


/**
 * @author Amos
 * 代金券红包修改
 */
public class SalesPromotionsJob implements BaseJob {
    /**
     * 定义一个定时任务类，用于执行促销状态更新作业。
     * 该类负责调度和执行促销状态的更新逻辑。
     */
    private static final Logger logger = LoggerFactory.getLogger(SalesPromotionsJob.class);

    /**
     * 注入更新促销状态的服务，用于在作业执行时调用状态更新逻辑。
     */
    @Autowired
    UpdateSalesPromStateService updateSalesPromStateService;

    /**
     * 默认构造函数，用于实例化促销状态更新作业类。
     */
    public SalesPromotionsJob() {

    }

    /**
     * 实现Job接口的execute方法，定义促销状态更新作业的具体执行逻辑。
     *
     * @param context 提供作业执行上下文的信息，包括作业名称、作业执行者等。
     * @throws JobExecutionException 如果作业执行过程中发生异常。
     */
    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        // 调用更新促销状态服务，获取状态更新结果字符串
        String receiveString = updateSalesPromStateService.updateState();
        // 记录当前时间及状态更新结果到日志
        logger.info(new Date() + receiveString);
    }
}  