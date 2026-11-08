package com.houjiang.thirdparty.service.message;

import com.houjiang.thirdparty.service.message.sender.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件异步队列：配置开启后异步入队发送，避免阻塞业务线程。
 *
 * <p>依赖 {@link MessageDispatchService} 而非 {@code MessageSendService} 接口：后者会与
 * {@code MessageSendServiceImpl} 形成循环依赖，叠加本 bean 的 {@code @Async} 代理后直接让容器
 * 启动失败。详见 {@link MessageDispatchService} 类注释。
 *
 * @author Amos
 */
@Slf4j
@Service
public class EmailQueueService {

    @Autowired
    private MessageDispatchService dispatchService;

    /**
     * 异步入队发送邮件（内部强制同步直发，避免递归入队）。
     *
     * @param channelCode 渠道编码
     * @param receiver    收件人
     * @param title       标题
     * @param content     正文
     * @param params      扩展参数
     */
    @Async
    public void enqueue(String channelCode, String receiver, String title, String content, Map<String, Object> params) {
        try {
            Map<String, Object> sendParams = params != null ? new HashMap<>(params) : new HashMap<>();
            sendParams.put("sync", true);
            SendResult result = dispatchService.sendDirectly(channelCode, receiver, title, content, sendParams);
            if (!result.isSuccess()) {
                log.warn("邮件队列发送失败: to={}, reason={}", receiver, result.getErrorMessage());
            } else {
                log.info("邮件队列发送成功: to={}, messageId={}", receiver, result.getMessageId());
            }
        } catch (Exception e) {
            log.error("邮件队列发送异常: to={}", receiver, e);
        }
    }
}
