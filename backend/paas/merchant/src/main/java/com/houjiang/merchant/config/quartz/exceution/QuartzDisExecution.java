package com.houjiang.merchant.config.quartz.exceution;

import com.houjiang.merchant.config.quartz.InvokeUtils;
import com.houjiang.merchant.common.entity.DevCrontab;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 禁止并发任务
 * @author Amos
 */
@DisallowConcurrentExecution
public class QuartzDisExecution extends AbstractQuartzJob {

    @Override
    protected void doExecute(JobExecutionContext context, DevCrontab crontab) throws Exception {
        InvokeUtils.invokeMethod(crontab);
    }

}