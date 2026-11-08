package com.houjiang.job.job;


import com.houjiang.job.service.JobMoneyStaticsService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Amos
 * 查询每月激活未充值的总金额
 */
public class PdbMoneyStatisticsJob implements BaseJob {
    /**
     * 定义一个日志器，用于记录作业执行的相关信息。
     */
    private static final Logger logger = LoggerFactory.getLogger(SalesPromotionsJob.class);
    /**
     * 注入作业金额统计服务，用于执行具体的统计任务。
     */
    @Autowired
    private JobMoneyStaticsService jobMoneyStaticsService;

    /**
     * 执行作业的具体逻辑。该方法会在定时任务触发时被调用，用于统计当前月份的销售金额。
     *
     * @param context 作业执行上下文，包含作业执行的相关信息。
     * @throws JobExecutionException 如果作业执行过程中出现异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 格式化当前日期为"yyyy-MM"格式，用于统计当月的销售金额
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM");
        String finishTime = dateformat.format(new Date());
        // 记录任务开始执行的时间
        logger.info("本次插入统计金额，开始时间:" + finishTime);
        // 调用作业金额统计服务，插入当前月份的销售金额统计数据
        jobMoneyStaticsService.InsertMoneyStatistport(finishTime);
    }

}
