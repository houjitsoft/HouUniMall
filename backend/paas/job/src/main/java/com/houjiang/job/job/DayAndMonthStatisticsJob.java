package com.houjiang.job.job;


import com.houjiang.commons.util.DateUtils;
import com.houjiang.commons.util.ParseMapUtils;
import com.houjiang.job.entity.JobDayOrderStaticsEntity;
import com.houjiang.job.entity.JobMonthOrderStaticsEntity;
import com.houjiang.job.service.JobDayOrderStaticsService;
import com.houjiang.job.service.JobMonthOrderStaticsService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author Amos
 */
public class DayAndMonthStatisticsJob implements BaseJob {
    /** 日志对象，用于记录程序运行时的调试信息、错误信息等 */
    private static final Logger logger = LoggerFactory.getLogger(SalesPromotionsJob.class);

    /** 注入JobDayOrderStaticsService，用于处理日常订单统计相关业务 */
    @Autowired
    private JobDayOrderStaticsService jobDayOrderStaticsService;

    /** 注入JobMonthOrderStaticsService，用于处理月度订单统计相关业务 */
    @Autowired
    private JobMonthOrderStaticsService jobMonthOrderStaticsService;


    /**
     * 执行作业，计算并保存日和月的订单统计信息。
     * 此方法在每天的凌晨执行，用于统计前一天的订单数量和金额，并保存到数据库中。
     * 同时，如果当前不是本月的第一天，也会统计上一个月的订单统计信息。
     *
     * @param context 作业执行上下文，提供作业相关的信息和功能。
     * @throws JobExecutionException 如果作业执行过程中出现异常。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 获取当前时间并设置为当天的零点，用于计算昨天的结束时间和今天的开始时间
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 今天零点的时间点
        Date end = calendar.getTime();
        // 将日期向前推一天，得到昨天零点的时间点
        // 昨天天0：0：0
        calendar.add(Calendar.DATE, -1);
        Date start = calendar.getTime();

        // 统计昨天的订单信息
        //进行插入日统计操作
        Map<String, Double> dayMap = jobDayOrderStaticsService.countStatics(start, end);
        JobDayOrderStaticsEntity entity = ParseMapUtils.parseMap2Double(dayMap, JobDayOrderStaticsEntity.class);
        entity.setCreateTime(new Date());
        Boolean torf = jobDayOrderStaticsService.save(entity);
        // 记录日统计信息的插入结果
        logger.info("本次插入销售记录，开始时间:" + start + " 结束时间" + end + " 记录：" + dayMap.toString() + " 插入成功与否:" + torf);

        // 如果当前不是本月的第一天，则统计上一个月的订单信息
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        if (cal.get(Calendar.DAY_OF_MONTH) != 1) {
            // 设置为本月第一天的零点，用于计算上一个月的结束时间和本月的开始时间
            Calendar monthCalendar = Calendar.getInstance();
            monthCalendar.setTime(new Date());
            monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
            monthCalendar.set(Calendar.HOUR_OF_DAY, 0);
            monthCalendar.set(Calendar.MINUTE, 0);
            monthCalendar.set(Calendar.SECOND, 0);
            Date endMonth = monthCalendar.getTime();
            // 将日期向前推一个月，得到上一个月的第一天的时间点
            monthCalendar.add(Calendar.MONTH, -1);
            monthCalendar.set(Calendar.HOUR_OF_DAY, 0);
            monthCalendar.set(Calendar.MINUTE, 0);
            monthCalendar.set(Calendar.SECOND, 0);
            monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
            Date startMonth = monthCalendar.getTime();

            // 统计上一个月的订单信息
            //进行插入月统计操作
            Map<String, Double> monthMap = jobDayOrderStaticsService.countStatics(startMonth, endMonth);
            JobMonthOrderStaticsEntity monthOrderStaticsEntity = ParseMapUtils.parseMap2Double(dayMap, JobMonthOrderStaticsEntity.class);
            monthOrderStaticsEntity.setCreateTime(DateUtils.formateForSdfCn(new Date()));
            Boolean monthTorf = jobMonthOrderStaticsService.save(monthOrderStaticsEntity);
            // 记录月统计信息的插入结果
            logger.info("本次插入销售记录，开始时间:" + start + " 结束时间" + end + " 记录：" + monthMap.toString() + " 插入成功与否:" + monthTorf);
        }
    }


}