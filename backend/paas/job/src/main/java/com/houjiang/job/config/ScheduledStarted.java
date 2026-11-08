package com.houjiang.job.config;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.job.entity.JobPlatformEntity;
import com.houjiang.job.service.JobPlatformService;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Amos
 */
@Component
public class ScheduledStarted implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledStarted.class);

	/** 加入Qulifier注解，通过名称注入bean */
	@Resource
	@Qualifier("Scheduler")
	private Scheduler scheduler;

	@Resource
	private JobPlatformService jobPlatformService;

	@Override
	public void run(String... args) throws Exception {
		logger.info("start last left scheduled");

        //从表中取定时任务名称和时间
		List<JobPlatformEntity> jobs = jobPlatformService.list(new LambdaQueryWrapper<JobPlatformEntity>()
							.eq(JobPlatformEntity::getStatus,0));
        // 检查任务列表是否为空，如果为空则不执行任何操作
        if(jobs.size() == 0) {
        	return;
        }

        // 遍历任务列表中的每个任务
        for (JobPlatformEntity job : jobs) {
        	String invokeTarget = job.getInvokeTarget();
        	String cronExpression = job.getCronExpression();
        	try {
        		addJob(invokeTarget, "2", cronExpression);
        		logger.info("定时任务 {} 已注册，cron={}", invokeTarget, cronExpression);
        	} catch (ClassNotFoundException e) {
        		logger.error("定时任务 {} 注册失败：实现类不存在，请重新部署 job 服务或禁用该任务（job_id={}）",
        				invokeTarget, job.getJobId(), e);
        	} catch (Exception e) {
        		logger.error("定时任务 {} 注册失败，cron={}，job_id={}", invokeTarget, cronExpression, job.getJobId(), e);
        	}
        }

		//从Redis中取定时任务调度时间
//		Set<String> schedulerdSet = RedisUtil.smembers("scheduler");
//		if (0 ==  schedulerdSet.size()) {
//			return;
//		}
//		for(String scheculer:schedulerdSet){
//			String trigger = RedisUtil.get(scheculer);
//			addJob(scheculer,"2",trigger);
//			logger.info("定时任务" + scheculer + "开始执行" + trigger );
//		}


	}

	public void addJob(String jobClassName, String jobGroupName, String cronExpression) throws Exception {

		// 启动调度器
		scheduler.start();

		Class<? extends Job> jobClass = resolveJobClass(jobClassName);
		JobDetail jobDetail = JobBuilder.newJob(jobClass)
				.withIdentity(jobClassName, jobGroupName).build();

		// 表达式调度构建器(即任务执行的时间)     cronExpression

		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

		// 按新的cronExpression表达式构建一个新的trigger
		CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobClassName, jobGroupName)
				.withSchedule(scheduleBuilder).build();

		try {
			scheduler.scheduleJob(jobDetail, trigger);

		} catch (SchedulerException e) {
			System.out.println("创建定时任务失败" + e);
			throw new Exception("创建定时任务失败");
		}
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends Job> resolveJobClass(String classname) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(classname);
		if (!Job.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("任务类未实现 Quartz Job 接口: " + classname);
		}
		return (Class<? extends Job>) clazz;
	}

}
