package com.houjiang.job.handler;

import com.houjiang.job.job.BaseJob;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * xxl-job 通用处理器
 * 通过 jobParam 传入 Quartz Job 的全限定类名，反射实例化并注入 Spring 依赖后执行
 *
 * 使用方式：在 xxl-job 管理后台创建任务时，JobHandler 填 "quartzJobHandler"，
 *          任务参数（jobParam）填 Job 类的全限定名，如：com.houjiang.job.job.AfterSalesJob
 *
 * @author Amos
 */
@Slf4j
@Component
public class XxlJobGenericHandler {

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    /**
     * 通用 Quartz Job 执行器
     * jobParam: Quartz Job 的全限定类名
     */
    @XxlJob("quartzJobHandler")
    public void execute() throws Exception {
        String jobClassName = XxlJobHelper.getJobParam();
        if (jobClassName == null || jobClassName.trim().isEmpty()) {
            XxlJobHelper.handleFail("任务参数为空，请传入 Quartz Job 的全限定类名");
            return;
        }

        log.info("xxl-job 执行 Quartz 任务: {}", jobClassName);
        long startTime = System.currentTimeMillis();

        try {
            // 反射创建 Job 实例
            Class<?> clazz = Class.forName(jobClassName.trim());
            BaseJob jobInstance = (BaseJob) clazz.getDeclaredConstructor().newInstance();

            // 注入 Spring 依赖（@Autowired 字段）
            beanFactory.autowireBean(jobInstance);

            // 执行任务（现有 Job 均未使用 context 参数，传 null 即可）
            jobInstance.execute(null);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("xxl-job 任务执行成功: {}, 耗时: {}ms", jobClassName, costTime);
            XxlJobHelper.handleSuccess("执行成功, 耗时: " + costTime + "ms");
        } catch (JobExecutionException e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("xxl-job 任务执行失败: {}, 耗时: {}ms", jobClassName, costTime, e);
            XxlJobHelper.handleFail("执行失败: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("找不到 Job 类: {}", jobClassName, e);
            XxlJobHelper.handleFail("找不到 Job 类: " + jobClassName);
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("xxl-job 任务执行异常: {}, 耗时: {}ms", jobClassName, costTime, e);
            XxlJobHelper.handleFail("执行异常: " + e.getMessage());
        }
    }
}
