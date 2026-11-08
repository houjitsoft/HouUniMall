package com.houjiang.job.feign;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @ClassName ActivityServiceClient
 * @Description
 * @Author Amos
 * @Date 2023/9/5 9:25
 * @Version 1.0
 * @author Amos
 */
@FeignClient("im")
public interface ImServiceClient {
    /**
     * 处理待过期的会话的定时任务接口
     * 该接口通过Feign客户端调用，旨在定期清理系统中的过期会话
     *
     * @return 返回执行结果，指示会话清理任务的状态
     */
    @PostMapping("/imConversation/updateTimeOutConversationJob")
    ResponseResult updateTimeOutConversationJob();

    /**
     * 删除 6 个月前 聊天记录
     * @return
     */
    @PostMapping("/imChatHistory/deleteImChatHistory")
    ResponseResult deleteImChatHistory();
}
