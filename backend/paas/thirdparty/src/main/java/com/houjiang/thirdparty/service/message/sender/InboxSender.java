package com.houjiang.thirdparty.service.message.sender;

import com.houjiang.thirdparty.entity.message.MessageInbox;
import com.houjiang.thirdparty.service.message.MessageInboxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 站内信发送器
 * 将消息保存到用户收件箱
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Slf4j
@Component
public class InboxSender implements MessageSender {
    
    @Autowired
    private MessageInboxService inboxService;
    
    @Override
    public String getChannelCode() {
        return "INBOX";
    }
    
    @Override
    public SendResult send(String receiver, String title, String content, 
                           Map<String, Object> config, Map<String, Object> params) {
        try {
            // 创建收件箱消息
            MessageInbox inbox = new MessageInbox();
            inbox.setUserId(receiver);
            inbox.setTitle(title);
            inbox.setContent(content);
            inbox.setIsRead(0);
            inbox.setOrgId(value(params, "orgId"));
            inbox.setIdempotencyKey(value(params, "idempotencyKey"));
            
            // 设置消息类型
            if (params != null && params.get("msgType") != null) {
                inbox.setMsgType(Integer.parseInt(params.get("msgType").toString()));
            } else {
                // 默认系统消息
                inbox.setMsgType(1);
            }
            
            // 设置业务类型
            if (params != null && params.get("bizType") != null) {
                inbox.setBizType(params.get("bizType").toString());
            }
            
            // 设置业务ID
            if (params != null && params.get("bizId") != null) {
                inbox.setBizId(params.get("bizId").toString());
            }
            
            // 保存到收件箱
            MessageInbox saved = inboxService.createIdempotently(inbox);
            
            if (saved != null && saved.getId() != null) {
                String messageId = saved.getId().toString();
                log.info("站内信发送成功: messageId={}", messageId);
                return SendResult.success(messageId);
            } else {
                log.error("站内信发送失败");
                return SendResult.fail("SAVE_ERROR", "保存到收件箱失败");
            }
            
        } catch (Exception e) {
            log.error("站内信发送异常: errorType={}", e.getClass().getSimpleName());
            return SendResult.fail("SYSTEM_ERROR", "系统异常");
        }
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        // 站内信不需要特殊配置
        return true;
    }

    private String value(Map<String, Object> params, String key) {
        if (params == null || params.get(key) == null) {
            return null;
        }
        return params.get(key).toString();
    }
}
