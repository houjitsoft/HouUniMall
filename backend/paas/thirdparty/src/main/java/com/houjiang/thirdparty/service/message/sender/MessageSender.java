package com.houjiang.thirdparty.service.message.sender;

import java.util.Map;

/**
 * 消息发送器接口
 * 
 * @author Amos
 * @since 2025-12-26
 */
public interface MessageSender {
    
    /**
     * 获取渠道编码
     * 
     * @return 渠道编码
     */
    String getChannelCode();
    
    /**
     * 发送消息
     * 
     * @param receiver 接收者（手机号/邮箱/用户ID等）
     * @param title 标题
     * @param content 内容
     * @param config 渠道配置
     * @param params 额外参数
     * @return 发送结果
     */
    SendResult send(String receiver, String title, String content, 
                    Map<String, Object> config, Map<String, Object> params);
    
    /**
     * 检查渠道配置是否有效
     * 
     * @param config 渠道配置
     * @return 是否有效
     */
    boolean validateConfig(Map<String, Object> config);
}
