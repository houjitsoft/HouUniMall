package com.houjiang.merchant.config.quartz.exceution;

import com.houjiang.merchant.config.quartz.InvokeUtils;
import com.houjiang.merchant.common.entity.DevCrontab;
import org.quartz.JobExecutionContext;

/**
 * 允许并发任务
 * @author Amos
 */
public class QuartzJobExecution extends AbstractQuartzJob {

    @Override
    protected void doExecute(JobExecutionContext context, DevCrontab crontab) throws Exception {
        InvokeUtils.invokeMethod(crontab);
    }

}
