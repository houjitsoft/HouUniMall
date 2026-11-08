package com.houjiang.job.config;

import org.quartz.Scheduler;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Amos
 */
@Configuration
public class SchedulerConfig {

    /**
     * 自动装配的作业工厂，用于创建并管理Quartz作业。
     */
    @Autowired
    private JobFactory jobFactory;

    /**
     * 配置并返回SchedulerFactoryBean，用于创建和管理Quartz调度器。
     *
     * @return SchedulerFactoryBean实例，配置了作业工厂和Quartz属性。
     * @throws IOException 如果配置文件读取失败。
     */
    @Bean(name="SchedulerFactory")
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        // 设置自定义的作业工厂
        factory.setJobFactory(jobFactory);
        // 设置Quartz的属性
        factory.setQuartzProperties(quartzProperties());
        return factory;

    }


    @Bean
    public Properties quartzProperties() throws IOException {
        // 创建PropertiesFactoryBean实例，用于加载和管理属性文件
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();

        // 设置属性文件的位置，确保可以从类路径中加载quartz2.properties文件
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz2.properties"));

        // 初始化PropertiesFactoryBean，此时会加载指定的属性文件
        // 在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();

        // 返回加载后的属性文件对象，供其他部分使用
        return propertiesFactoryBean.getObject();

    }

    /*
/**
     * quartz初始化监听器
     */
    @Bean
    public QuartzInitializerListener executorListener() {
        return new QuartzInitializerListener();
    }

    /*
/**
     * 通过SchedulerFactoryBean获取Scheduler的实例
     */
    @Bean(name="Scheduler")
    public Scheduler scheduler() throws IOException {
        return schedulerFactoryBean().getScheduler();
    }




}
