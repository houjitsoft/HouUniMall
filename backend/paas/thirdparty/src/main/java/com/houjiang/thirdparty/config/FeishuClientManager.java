package com.houjiang.thirdparty.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.CustomEventHandler;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.core.request.EventReq;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.enums.CreateMessageReceiveIdTypeEnum;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.lark.oapi.ws.Client;
import com.houjiang.thirdparty.entity.message.MessageInbox;
import com.houjiang.thirdparty.entity.message.MessageRecord;
import com.houjiang.thirdparty.service.message.MessageInboxService;
import com.houjiang.thirdparty.service.message.MessageRecordService;
import com.houjiang.thirdparty.service.message.MessageSubscribeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 飞书长连接客户端管理器
 * 负责管理飞书应用的长连接客户端，支持事件接收
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Slf4j
@Component
public class FeishuClientManager {
    
    /** 存储每个应用的长连接客户端，key为appId */
    private final Map<String, Client> clients = new ConcurrentHashMap<>();
    
    /** 存储每个应用的API客户端（用于发送回复消息），key为appId */
    private final Map<String, com.lark.oapi.Client> apiClients = new ConcurrentHashMap<>();
    
    @Autowired(required = false)
    private MessageRecordService messageRecordService;
    
    @Autowired(required = false)
    private MessageInboxService messageInboxService;
    
    @Autowired(required = false)
    private MessageSubscribeService messageSubscribeService;
    
    /**
     * 创建或更新飞书长连接客户端
     * 
     * @param appId 应用ID
     * @param appSecret 应用密钥
     * @return 是否成功启动客户端
     */
    public boolean startClient(String appId, String appSecret) {
        try {
            // 如果已存在客户端，先停止
            stopClient(appId);
            
            // 创建API客户端（用于发送回复消息）
            com.lark.oapi.Client apiClient = com.lark.oapi.Client.newBuilder(appId, appSecret)
                    .logReqAtDebug(false)
                    .build();
            apiClients.put(appId, apiClient);
            log.info("飞书API客户端创建成功: appId={}", appId);
            
            // 创建事件处理器
            EventDispatcher eventHandler = createEventHandler(appId);
            
            // 创建长连接客户端
            Client client = new Client.Builder(appId, appSecret)
                    .eventHandler(eventHandler)
                    .build();
            
            // 启动客户端（异步启动，不阻塞）
            new Thread(() -> {
                try {
                    client.start();
                    log.info("飞书长连接客户端启动成功: appId={}", appId);
                } catch (Exception e) {
                    log.error("飞书长连接客户端启动失败: appId={}, error={}", appId, e.getMessage(), e);
                }
            }).start();
            
            // 存储客户端
            clients.put(appId, client);
            
            return true;
        } catch (Exception e) {
            log.error("创建飞书长连接客户端失败: appId={}, error={}", appId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 停止飞书长连接客户端
     * 
     * @param appId 应用ID
     */
    public void stopClient(String appId) {
        try {
            Client client = clients.remove(appId);
            if (client != null) {
                // 注意：SDK的Client没有提供stop方法，这里只是从Map中移除
                // 实际的长连接会在进程结束时自动关闭
                log.info("飞书长连接客户端已移除: appId={}", appId);
            }
            
            // 同时移除API客户端
            apiClients.remove(appId);
            log.info("飞书API客户端已移除: appId={}", appId);
        } catch (Exception e) {
            log.error("停止飞书长连接客户端失败: appId={}, error={}", appId, e.getMessage(), e);
        }
    }
    
    /**
     * 获取飞书客户端
     * 
     * @param appId 应用ID
     * @return 客户端实例，如果不存在返回null
     */
    public Client getClient(String appId) {
        return clients.get(appId);
    }
    
    /**
     * 创建事件处理器
     * 处理飞书推送的各种事件
     * 
     * @param appId 应用ID，用于发送回复消息
     */
    private EventDispatcher createEventHandler(String appId) {
        return EventDispatcher.newBuilder("", "")
                // 处理接收消息事件（v2.0）
                .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                    @Override
                    public void handle(P2MessageReceiveV1 event) throws Exception {
                        log.info("收到飞书消息事件: requestId={}, appId={}, data={}", 
                                event.getRequestId(), 
                                appId,
                                Jsons.DEFAULT.toJson(event.getEvent()));
                        
                        // 处理消息接收事件
                        handleMessageEvent(event, appId);
                    }
                })
                // 处理自定义事件（v1.0）
                .onCustomizedEvent("*", new CustomEventHandler() {
                    @Override
                    public void handle(EventReq event) throws Exception {
                        String eventBody = new String(event.getBody(), StandardCharsets.UTF_8);
                        log.info("收到飞书自定义事件: appId={}, data={}", 
                                appId,
                                eventBody);
                        
                        // 处理自定义事件
                        handleCustomEvent(event, appId);
                    }
                })
                .build();
    }
    
    /**
     * 处理消息接收事件
     * 
     * @param event 消息事件
     * @param appId 应用ID
     */
    private void handleMessageEvent(P2MessageReceiveV1 event, String appId) {
        try {
            // 获取消息内容
            var messageEvent = event.getEvent();
            if (messageEvent == null) {
                log.warn("消息事件为空，跳过处理");
                return;
            }
            
            // 使用JSON序列化获取数据，然后解析
            String eventJson = Jsons.DEFAULT.toJson(messageEvent);
            JSONObject messageData = JSON.parseObject(eventJson);
            
            String messageId = messageData.getString("message_id");
            String chatId = messageData.getString("chat_id");
            String messageType = messageData.getString("message_type");
            String content = messageData.getString("content");
            
            // 获取发送者信息
            String senderId = null;
            String senderType = null;
            try {
                JSONObject sender = messageData.getJSONObject("sender");
                if (sender != null) {
                    JSONObject senderIdObj = sender.getJSONObject("sender_id");
                    if (senderIdObj != null) {
                        senderId = senderIdObj.getString("user_id");
                    }
                    senderType = sender.getString("sender_type");
                }
            } catch (Exception e) {
                log.debug("获取发送者信息失败: {}", e.getMessage());
            }
            
            log.info("处理飞书消息: messageId={}, chatId={}, type={}, senderId={}, senderType={}, content={}", 
                    messageId, chatId, messageType, senderId, senderType, content);
            
            // 解析消息内容（JSON格式）
            String messageText = parseMessageContent(content, messageType);
            
            // 1. 保存消息到收件箱（如果发送者是系统用户）
            if (senderId != null && messageInboxService != null) {
                try {
                    saveMessageToInbox(senderId, messageId, chatId, messageType, messageText);
                } catch (Exception e) {
                    log.error("保存消息到收件箱失败: error={}", e.getMessage(), e);
                }
            }
            
            // 2. 保存消息发送记录
            if (messageRecordService != null) {
                try {
                    saveMessageRecord(messageId, chatId, senderId, messageType, messageText, appId);
                } catch (Exception e) {
                    log.error("保存消息记录失败: error={}", e.getMessage(), e);
                }
            }
            
            // 3. 根据消息内容触发业务逻辑（可选）
            // 例如：关键词回复、命令处理等
            if (messageText != null && !messageText.trim().isEmpty()) {
                processMessageBusinessLogic(senderId, chatId, messageText, messageType, appId);
            }
            
        } catch (Exception e) {
            log.error("处理飞书消息事件失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 解析消息内容
     * 
     * @param content 原始内容（JSON格式）
     * @param messageType 消息类型
     * @return 解析后的文本内容
     */
    private String parseMessageContent(String content, String messageType) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        try {
            // 解析JSON内容
            JSONObject contentJson = JSON.parseObject(content);
            
            // 根据消息类型提取文本
            if ("text".equals(messageType) || "MSG_TYPE_TEXT".equals(messageType)) {
                // 文本消息：{"text":"消息内容"}
                return contentJson.getString("text");
            } else if ("post".equals(messageType) || "MSG_TYPE_POST".equals(messageType)) {
                // 富文本消息：{"post":{...}}
                // 简化处理，返回JSON字符串
                return contentJson.toJSONString();
            } else if ("card".equals(messageType) || "MSG_TYPE_INTERACTIVE".equals(messageType)) {
                // 卡片消息：{"card":{...}}
                // 简化处理，返回JSON字符串
                return contentJson.toJSONString();
            } else {
                // 其他类型，返回原始内容
                return content;
            }
        } catch (Exception e) {
            log.debug("解析消息内容失败，返回原始内容: error={}", e.getMessage());
            return content;
        }
    }
    
    /**
     * 保存消息到收件箱
     */
    private void saveMessageToInbox(String userId, String messageId, String chatId, 
                                    String messageType, String content) {
        try {
            MessageInbox inbox = new MessageInbox();
            inbox.setUserId(userId);
            inbox.setTitle("飞书消息");
            inbox.setContent(content != null && content.length() > 500 ? content.substring(0, 500) : content);
            // 业务提醒
            inbox.setMsgType(2);
            inbox.setBizType("FEISHU_MESSAGE");
            inbox.setBizId(messageId);
            inbox.setIsRead(0);
            inbox.setOrgId(getDefaultOrgId());
            
            boolean saved = messageInboxService.createInbox(inbox);
            if (saved) {
                log.debug("消息已保存到收件箱: userId={}, messageId={}", userId, messageId);
            }
        } catch (Exception e) {
            log.error("保存消息到收件箱异常: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 保存消息发送记录
     */
    private void saveMessageRecord(String messageId, String chatId, String senderId, 
                                   String messageType, String content, String appId) {
        try {
            MessageRecord record = new MessageRecord();
            record.setTemplateCode("FEISHU_RECEIVE");
            record.setBizType("FEISHU_MESSAGE");
            record.setBizId(messageId);
            record.setChannel("FEISHU");
            record.setReceiverId(senderId);
            // 可以后续通过API获取用户名称
            record.setReceiverName(senderId);
            record.setReceiverContact(chatId);
            record.setTitle("飞书消息接收");
            record.setContent(content != null && content.length() > 1000 ? content.substring(0, 1000) : content);
            // 成功（接收成功）
            record.setSendStatus(2);
            record.setSendTime((int)(System.currentTimeMillis() / 1000));
            record.setRetryCount(0);
            record.setOrgId(getDefaultOrgId());
            
            boolean saved = messageRecordService.createRecord(record);
            if (saved) {
                log.debug("消息记录已保存: messageId={}", messageId);
            }
        } catch (Exception e) {
            log.error("保存消息记录异常: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理消息业务逻辑
     * 根据消息内容触发相应的业务逻辑，例如关键词回复、命令处理等
     */
    private void processMessageBusinessLogic(String senderId, String chatId, String messageText, 
                                            String messageType, String appId) {
        try {
            // 只处理文本消息
            if (!"text".equals(messageType) && !"MSG_TYPE_TEXT".equals(messageType)) {
                return;
            }
            
            String trimmedText = messageText.trim().toLowerCase();
            
            // 示例：关键词回复
            if (trimmedText.contains("帮助") || trimmedText.contains("help")) {
                sendAutoReply(appId, chatId, "您好！我是系统助手，可以为您提供以下服务：\n1. 查询信息\n2. 提交工单\n3. 其他帮助\n\n请输入具体需求。");
            } else if (trimmedText.contains("你好") || trimmedText.contains("hello")) {
                sendAutoReply(appId, chatId, "您好！有什么可以帮助您的吗？");
            }
            
            // 可以在这里添加更多业务逻辑
            // 例如：
            // - 命令处理（如：/查询订单 123456）
            // - 关键词触发（如：提到"紧急"时通知管理员）
            // - 智能回复（集成AI服务）
            
        } catch (Exception e) {
            log.error("处理消息业务逻辑异常: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 发送自动回复消息
     */
    private void sendAutoReply(String appId, String chatId, String replyText) {
        try {
            com.lark.oapi.Client apiClient = apiClients.get(appId);
            if (apiClient == null) {
                log.warn("API客户端不存在，无法发送回复: appId={}", appId);
                return;
            }
            
            // 构建消息内容（JSON格式）
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("text", replyText);
            String contentJson = Jsons.DEFAULT.toJson(contentMap);
            
            // 构建请求
            CreateMessageReq req = CreateMessageReq.newBuilder()
                    .receiveIdType(CreateMessageReceiveIdTypeEnum.CHAT_ID)
                    .createMessageReqBody(CreateMessageReqBody.newBuilder()
                            .receiveId(chatId)
                            .msgType("text")
                            .content(contentJson)
                            .build())
                    .build();
            
            // 发送消息
            CreateMessageResp resp = apiClient.im().message().create(req);
            
            if (resp.success()) {
                log.info("自动回复发送成功: chatId={}, messageId={}", chatId, resp.getData().getMessageId());
            } else {
                log.warn("自动回复发送失败: chatId={}, code={}, msg={}", 
                        chatId, resp.getCode(), resp.getMsg());
            }
        } catch (Exception e) {
            log.error("发送自动回复异常: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理自定义事件
     * 
     * @param event 自定义事件
     * @param appId 应用ID
     */
    private void handleCustomEvent(EventReq event, String appId) {
        try {
            String eventBody = new String(event.getBody(), StandardCharsets.UTF_8);
            
            log.info("处理飞书自定义事件: appId={}, body={}", appId, eventBody);
            
            // 解析事件体
            JSONObject eventData = null;
            String eventType = null;
            try {
                eventData = JSON.parseObject(eventBody);
                if (eventData != null) {
                    // 从事件体中提取事件类型
                    eventType = eventData.getString("type");
                    if (eventType == null) {
                        // 尝试从其他字段获取
                        JSONObject eventObj = eventData.getJSONObject("event");
                        if (eventObj != null) {
                            eventType = eventObj.getString("type");
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("解析事件体失败，可能不是JSON格式: error={}", e.getMessage());
            }
            
            if (eventType == null) {
                log.warn("无法从事件体中提取事件类型，跳过处理");
                return;
            }
            
            // 根据事件类型处理
            switch (eventType) {
                case "im.chat.member.user.added":
                    // 用户加入群聊
                    handleChatMemberAdded(eventData, appId);
                    break;
                case "im.chat.member.user.withdrawn":
                    // 用户离开群聊
                    handleChatMemberWithdrawn(eventData, appId);
                    break;
                case "approval.approval":
                    // 审批流程事件
                    handleApprovalEvent(eventData, appId);
                    break;
                case "calendar.calendar.event.changed":
                    // 日程变更事件
                    handleCalendarEventChanged(eventData, appId);
                    break;
                default:
                    log.debug("未处理的自定义事件类型: type={}", eventType);
                    // 可以在这里添加其他事件类型的处理逻辑
            }
            
        } catch (Exception e) {
            log.error("处理飞书自定义事件失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户加入群聊事件
     */
    private void handleChatMemberAdded(JSONObject eventData, String appId) {
        try {
            if (eventData == null) {
                return;
            }
            
            String chatId = eventData.getString("chat_id");
            JSONObject operator = eventData.getJSONObject("operator");
            JSONObject user = eventData.getJSONObject("user");
            
            String operatorId = operator != null ? operator.getString("user_id") : null;
            String userId = user != null ? user.getString("user_id") : null;
            
            log.info("用户加入群聊: chatId={}, operatorId={}, userId={}", chatId, operatorId, userId);
            
            // 可以在这里实现业务逻辑，例如：
            // 1. 发送欢迎消息
            // 2. 记录加入日志
            // 3. 触发相关业务通知等
            
        } catch (Exception e) {
            log.error("处理用户加入群聊事件失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理用户离开群聊事件
     */
    private void handleChatMemberWithdrawn(JSONObject eventData, String appId) {
        try {
            if (eventData == null) {
                return;
            }
            
            String chatId = eventData.getString("chat_id");
            JSONObject operator = eventData.getJSONObject("operator");
            JSONObject user = eventData.getJSONObject("user");
            
            String operatorId = operator != null ? operator.getString("user_id") : null;
            String userId = user != null ? user.getString("user_id") : null;
            
            log.info("用户离开群聊: chatId={}, operatorId={}, userId={}", chatId, operatorId, userId);
            
            // 可以在这里实现业务逻辑
            
        } catch (Exception e) {
            log.error("处理用户离开群聊事件失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理审批流程事件
     */
    private void handleApprovalEvent(JSONObject eventData, String appId) {
        try {
            if (eventData == null) {
                return;
            }
            
            String approvalCode = eventData.getString("approval_code");
            String instanceCode = eventData.getString("instance_code");
            String status = eventData.getString("status");
            
            log.info("审批流程事件: approvalCode={}, instanceCode={}, status={}", 
                    approvalCode, instanceCode, status);
            
            // 可以在这里实现业务逻辑，例如：
            // 1. 同步审批状态到系统
            // 2. 触发后续业务流程
            // 3. 发送通知等
            
        } catch (Exception e) {
            log.error("处理审批流程事件失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理日程变更事件
     */
    private void handleCalendarEventChanged(JSONObject eventData, String appId) {
        try {
            if (eventData == null) {
                return;
            }
            
            String calendarId = eventData.getString("calendar_id");
            String eventId = eventData.getString("event_id");
            
            log.info("日程变更事件: calendarId={}, eventId={}", calendarId, eventId);
            
            // 可以在这里实现业务逻辑，例如：
            // 1. 同步日程到系统
            // 2. 发送提醒通知等
            
        } catch (Exception e) {
            log.error("处理日程变更事件失败: error={}", e.getMessage(), e);
        }
    }
    
    /**
     * 未提供认证组织时不猜测默认组织，调用方必须显式传入组织上下文。
     */
    private String getDefaultOrgId() {
        return null;
    }
}
