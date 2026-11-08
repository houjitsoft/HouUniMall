package com.houjiang.job.job;

import com.houjiang.job.feign.UserServiceClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @param
 * @author Amos
 * @description 删除已注册7天的临时账号
 * @return
 * @date 2023/4/17 16:02
 */

public class DeleteTemporaryAccountJob implements BaseJob {

    /**
     * 自动注入UserServiceClient，用于执行与用户服务相关的操作。
     * UserServiceClient负责与用户服务的交互，提供删除临时账户等操作。
     */
    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 执行定时任务，删除临时账户。
     * 该方法作为Spring Batch作业的一部分，被调度执行。其目的是清理系统中过期或临时的用户账户，
     * 以保持系统数据的清洁和安全性。
     *
     * @param context 作业执行上下文，提供作业执行时的环境信息和状态。
     *                可以通过上下文获取作业执行的相关参数和状态，但本方法中未使用。
     * @throws JobExecutionException 如果作业执行过程中发生错误。
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        userServiceClient.deleteTemporaryAccount();
    }
}
