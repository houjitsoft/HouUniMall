package com.houjiang.job.job;

import com.houjiang.job.feign.ImServiceClient;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 更新im超时会话状态
 * @ClassName UpdateImTimeOutJob
 * @Author Amos
 * @Date 2024/11/12 15:35
 * @Version 1.0
 * @author Amos
 */
public class UpdateImTimeOutJob implements BaseJob {

    private static final Logger logger = LoggerFactory.getLogger(UpdateImTimeOutJob.class);

    @Autowired
    private ImServiceClient imServiceClient;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("====im会话超时处理开始====");
        // 处理 用户 客服 未及时响应会话  导致会话 即将超时 或 已经超时 并发送im消息
        imServiceClient.updateTimeOutConversationJob();
        // 删除超6个月的聊天记录
        imServiceClient.deleteImChatHistory();
        // 更新Im 超时会话 状态
        logger.info("====im会话超时处理结束====");
    }
}
