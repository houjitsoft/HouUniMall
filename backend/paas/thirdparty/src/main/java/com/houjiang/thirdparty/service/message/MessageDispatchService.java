package com.houjiang.thirdparty.service.message;

import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.entity.message.MessageRecord;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 幂等发信管道：负责"创建/抢占/更新发送记录 + 调用渠道投递"这条同步链路。
 *
 * <p>独立成一个 bean 是为了断开 Bean 循环依赖。原先 {@code EmailQueueService}（异步邮件队列）、
 * {@code EmailSettingsService}（邮件设置测试发信）都注入 {@code MessageSendService} 接口反向调用
 * 发送，而该接口的实现 {@code MessageSendServiceImpl} 又反过来依赖它们：
 * <pre>
 *   messageSendServiceImpl → emailQueueService → messageSendService(messageSendServiceImpl)
 *   messageSendServiceImpl → emailSettingsService → messageSendService(messageSendServiceImpl)
 * </pre>
 * 由于 {@code EmailQueueService} 带 {@code @Async} 会被 AOP 代理，而 Spring 的
 * {@code allowRawInjectionDespiteWrapping} 默认为 false，一旦发生循环引用且 Bean 最终被包装，
 * 容器会直接抛 {@code BeanCurrentlyInCreationException}（"has been injected into other beans
 * in its raw version"），thirdparty 服务因此启动即崩溃。
 *
 * <p>抽出本 bean 后，上述反向依赖改指向它，而它的依赖
 * （{@link MessageRecordService}、{@link MessageDeliveryService}、{@link MessageNotificationMetrics}）
 * 都不回指任何发送编排 bean，闭环彻底消除，无需 {@code @Lazy} 兜底。
 *
 * @author Amos
 */
@Slf4j
@Service
public class MessageDispatchService {

    @Autowired
    private MessageRecordService recordService;

    @Autowired
    private MessageDeliveryService deliveryService;

    @Autowired(required = false)
    private MessageNotificationMetrics metrics;

    /**
     * 执行带幂等记录的一次发送：已成功则重放，未创建则创建，抢占后再投递并回写状态。
     *
     * @param request 幂等发送请求
     * @return 幂等发送响应
     */
    public IdempotentMessageSendResponse dispatch(IdempotentMessageSendRequest request) {
        MessageRecord record = recordService.findByIdempotencyKey(request.idempotencyKey());
        if (isSucceeded(record)) {
            return replay(record);
        }
        if (record == null) {
            record = recordService.createPending(request);
            if (record == null) {
                return failure("RECORD_CREATE_FAILED", "创建发送记录失败");
            }
            if (isSucceeded(record)) {
                return replay(record);
            }
        }
        if (!recordService.claimForSend(record.getId())) {
            MessageRecord latest = recordService.findByIdempotencyKey(request.idempotencyKey());
            if (isSucceeded(latest)) {
                return replay(latest);
            }
            return failure("SEND_IN_PROGRESS", "消息正在发送中");
        }

        Map<String, Object> deliveryParams = new HashMap<>(request.params());
        deliveryParams.put("idempotencyKey", request.idempotencyKey());
        SendResult result = deliveryService.deliver(
                request.channelCode(),
                request.receiver(),
                request.title(),
                request.content(),
                deliveryParams);
        if (result.isSuccess()) {
            recordService.markSucceeded(record.getId(), result.getMessageId());
            return new IdempotentMessageSendResponse(
                    true, result.getMessageId(), false, null, null);
        }

        String errorCode = hasText(result.getErrorCode()) ? result.getErrorCode() : "SEND_ERROR";
        String errorMessage = hasText(result.getErrorMessage()) ? result.getErrorMessage() : "发送失败";
        recordService.markFailed(record.getId(), errorCode, errorMessage);
        return failure(errorCode, errorMessage);
    }

    /**
     * 对已存在的记录重投一次（不新建记录）。
     *
     * @param request 发送快照
     * @return 幂等发送响应
     */
    public IdempotentMessageSendResponse retry(IdempotentMessageSendRequest request) {
        return deliveryService.retry(request);
    }

    /**
     * 同步直发：跳过队列判断，直接走幂等管道。
     *
     * <p>供异步邮件队列和邮件设置测试回调使用，保证这两处拿到的结果与
     * {@code sendByChannel} 在 sync=true 分支下的行为完全一致。
     *
     * @param channelCode 渠道编码
     * @param receiver    接收者
     * @param title       标题
     * @param content     内容
     * @param params      发送参数
     * @return 发送结果
     */
    public SendResult sendDirectly(String channelCode, String receiver, String title,
                                   String content, Map<String, Object> params) {
        Map<String, Object> safeParams = params == null ? Map.of() : params;
        String idempotencyKey = value(safeParams, "idempotencyKey");
        if (!hasText(idempotencyKey)) {
            idempotencyKey = "LEGACY:" + UUID.randomUUID();
        }
        IdempotentMessageSendResponse response = dispatch(new IdempotentMessageSendRequest(
                idempotencyKey,
                channelCode,
                receiver,
                value(safeParams, "receiverName"),
                title,
                content,
                safeParams));
        SendResult result = response.success()
                ? SendResult.success(response.messageId())
                : SendResult.fail(response.errorCode(), response.errorMessage());
        result.setIdempotentReplay(response.idempotentReplay());
        return result;
    }

    private boolean isSucceeded(MessageRecord record) {
        return record != null && Integer.valueOf(2).equals(record.getSendStatus());
    }

    private IdempotentMessageSendResponse replay(MessageRecord record) {
        if (metrics != null) {
            metrics.recordIdempotentReplay();
        }
        return new IdempotentMessageSendResponse(
                true, record.getProviderMessageId(), true, null, null);
    }

    private IdempotentMessageSendResponse failure(String errorCode, String errorMessage) {
        return new IdempotentMessageSendResponse(
                false, null, false, errorCode, errorMessage);
    }

    private String value(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value == null ? null : value.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
