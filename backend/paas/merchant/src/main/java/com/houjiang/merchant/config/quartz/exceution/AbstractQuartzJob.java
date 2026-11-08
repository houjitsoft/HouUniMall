package com.houjiang.merchant.config.quartz.exceution;

import com.houjiang.merchant.config.quartz.TaskConstants;
import com.houjiang.merchant.common.entity.DevCrontab;
import com.houjiang.merchant.common.mapper.DevCrontabMapper;
import com.houjiang.merchant.common.util.SpringUtils;
import com.houjiang.merchant.common.util.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * @author Amos
 */
public abstract class AbstractQuartzJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    /**
     * 线程本地变量
     */
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 执行
     *
     * @param context 上下文
     */
    @Override
    public void execute(JobExecutionContext context) {
        DevCrontab crontab = new DevCrontab();
        BeanUtils.copyProperties(context.getMergedJobDataMap().get(TaskConstants.TASK_PROPERTIES), crontab);
        try {
            before();
            doExecute(context, crontab);
            after(crontab, null);
        } catch (Exception e) {
            log.error("任务执行异常：", e);
            after(crontab, e);
        }
    }

    /**
     * 执行前
     */
    protected void before() {
        threadLocal.set(System.currentTimeMillis());
    }

    /**
     * 执行后
     *
     * @param crontab 系统计划任务
     */
    protected void after(DevCrontab crontab, Exception e)
    {
        long startTime = threadLocal.get();
        long endTime = System.currentTimeMillis();
        threadLocal.remove();

        crontab.setError("");
//        crontab.set(startTime / 1000);
        crontab.setLastTime(endTime / 1000);
        crontab.setMaxTime(endTime - startTime);
        crontab.setTime(endTime - startTime);
        if (StringUtils.isNotNull(e)) {
            crontab.setError(e.getMessage());
            crontab.setStatus(TaskConstants.STATUS_FAIL);
        }

        SpringUtils.getBean(DevCrontabMapper.class).updateById(crontab);
    }

    /**
     * 执行方法
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, DevCrontab sysJob) throws Exception;

}
