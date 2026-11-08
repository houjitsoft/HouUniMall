package com.houjiang.job.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义的JobFactory，用于在Quartz作业实例化时进行Spring Bean的自动装配。
 * 继承自AdaptableJobFactory，重写createJobInstance方法以应用自动装配。
 * @author Amos
 */
@Component
public class JobFactory extends AdaptableJobFactory {
    /**
     * Spring的AutowireCapableBeanFactory，用于自动装配作业实例。
     */
    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    /**
     * 创建作业实例，并对其进行自动装配。
     *
     * @param bundle 触发器触发作业时的相关信息集合。
     * @return 经过自动装配的作业实例。
     * @throws Exception 如果创建或自动装配作业实例时发生错误。
     */
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        // 调用父类方法创建作业实例
        Object jobInstance = super.createJobInstance(bundle);
        // 使用AutowireCapableBeanFactory对作业实例进行自动装配
        capableBeanFactory.autowireBean(jobInstance);
        return jobInstance;
    }
}
