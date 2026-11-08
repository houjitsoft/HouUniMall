package com.houjiang.thirdparty.service.message;

import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.service.message.sender.SendResult;

import java.util.List;
import java.util.Map;

/**
 * 统一消息发送服务接口
 * 
 * @author Amos
 * @since 2025-12-26
 */
public interface MessageSendService {

    /**
     * TODO 补充方法说明
     * @param request TODO
     * @return TODO
     */
    IdempotentMessageSendResponse sendIdempotently(IdempotentMessageSendRequest request);

    /**
     * TODO 补充方法说明
     * @param request TODO
     * @return TODO
     */
    IdempotentMessageSendResponse retryExisting(IdempotentMessageSendRequest request);
    
    /**
     * 发送消息（使用模板）
     * 
     * @param templateCode 模板编码
     * @param receiver 接收者
     * @param params 模板参数
     * @return 发送结果列表（每个渠道一个结果）
     */
    List<SendResult> sendByTemplate(String templateCode, String receiver, Map<String, Object> params);
    
    /**
     * 发送消息（指定渠道）
     * 
     * @param channelCode 渠道编码
     * @param receiver 接收者
     * @param title 标题
     * @param content 内容
     * @param params 额外参数
     * @return 发送结果
     */
    SendResult sendByChannel(String channelCode, String receiver, String title, String content, Map<String, Object> params);
    
    /**
     * 批量发送消息
     * 
     * @param templateCode 模板编码
     * @param receivers 接收者列表
     * @param params 模板参数
     * @return 发送结果列表
     */
    List<SendResult> batchSend(String templateCode, List<String> receivers, Map<String, Object> params);
    
    /**
     * 测试渠道配置
     * 
     * @param channelCode 渠道编码
     * @param testReceiver 测试接收者
     * @return 测试结果
     */
    SendResult testChannel(String channelCode, String testReceiver);
}
