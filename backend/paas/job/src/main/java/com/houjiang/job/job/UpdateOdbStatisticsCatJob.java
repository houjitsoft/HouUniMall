package com.houjiang.job.job;


import com.houjiang.commons.util.ResponseResult;
import com.houjiang.job.dto.CategoryMonthlySalesDTO;
import com.houjiang.job.service.JobStatisticsCatService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Amos
 * 按商品类型每月统计销售额
 */
public class UpdateOdbStatisticsCatJob implements BaseJob {
    private static final Logger logger = LoggerFactory.getLogger(SalesPromotionsJob.class);
    @Autowired
    private JobStatisticsCatService jobStatisticsCatService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 获取上个月的第一天
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        // 将小时至0
        cal.set(Calendar.HOUR_OF_DAY, 0);
        // 将分钟至0
        cal.set(Calendar.MINUTE, 0);
        // 将秒至0
        cal.set(Calendar.SECOND, 0);
        // 将毫秒至0
        cal.set(Calendar.MILLISECOND, 0);
        // 将当前月加1；
        cal.add(Calendar.MONTH, -1);

        // 获取上个月的最后一天
        Calendar call = Calendar.getInstance();
        // 设置上个月最后一天
        call.set(Calendar.DAY_OF_MONTH, 0);
        // 将小时至0
        call.set(Calendar.HOUR_OF_DAY, 23);
        // 将分钟至0
        call.set(Calendar.MINUTE, 59);
        // 将秒至0
        call.set(Calendar.SECOND, 59);
        // 将毫秒至0
        call.set(Calendar.MILLISECOND, 59);

        String startTime = sm.format(cal.getTime());
        logger.info("上月第一天:" + startTime);
        String endTime = sm.format(call.getTime());
        logger.info("上月最后一天:" + endTime);
        CategoryMonthlySalesDTO cms = new CategoryMonthlySalesDTO();
        cms.setStartTime(startTime);
        cms.setEndTime(endTime);
        ResponseResult resp = jobStatisticsCatService.insertOdbStatisticsCat(cms);
        logger.info("本次插入销售记录，开始时间:" + startTime + "结束时间:" + endTime);
    }

}
