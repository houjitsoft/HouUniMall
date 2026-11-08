package com.houjiang.job.job;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.service.JobOrderNotCompletedService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Amos
 * 统计为完成订单 月度订单
 */
public class NotCompletedJobimplementsJob implements BaseJob {
    /**
     * 日志记录器，用于记录作业执行的相关信息。
     */
    private static final Logger logger = LoggerFactory.getLogger(SalesPromotionsJob.class);

    /**
     * 未完成订单服务，用于处理未完成的订单操作。
     */
    @Autowired
    private JobOrderNotCompletedService jobOrderNotCompletedService;

    /**
     * 执行作业的具体逻辑。
     * 此方法旨在每月初插入上个月未完成的订单数据。
     *
     * @param context 作业执行的上下文，包含作业执行的相关信息。
     * @throws JobExecutionException 如果作业执行过程中出现异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 格式化日期为"yyyy-MM"格式，用于后续处理
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM");
        // 获取当前日期时间
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        // 设置时间为上个月的最后一天
        c.add(Calendar.MONTH, -1);
        Date m3 = c.getTime();
        // 根据日期格式化上个月的最后一天日期字符串
        String finishTime = dateformat.format(m3);
        // 记录作业开始执行的时间
        logger.info("本次插入未完成订单，开始时间:" + finishTime);
        // 调用服务插入上个月未完成的订单数据
        ResponseResult staut = jobOrderNotCompletedService.insertNotCompletedJobimplements(finishTime);
    }


}
