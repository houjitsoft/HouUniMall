package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.dto.message.MessageRecordDetailResponse;
import com.houjiang.thirdparty.dto.message.MessageRecordFilter;
import com.houjiang.thirdparty.dto.message.MessageRecordListItemResponse;
import com.houjiang.thirdparty.entity.message.MessageRecord;
import com.houjiang.thirdparty.mapper.message.MessageRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 消息发送记录服务类
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Service
public class MessageRecordService {

    @Autowired
    private MessageRecordMapper recordMapper;

    /**
     * 重投需要的投递能力。
     *
     * <p>注入 {@link MessageDeliveryService} 而不是 {@code MessageSendService} 接口：重投链路
     * 不读写本服务的记录状态，用叶子 bean 即可完成，从而断开
     * "MessageRecordService ⇄ MessageSendServiceImpl" 循环依赖，不再需要 {@code @Lazy}。
     */
    @Autowired
    private MessageDeliveryService deliveryService;

    @Autowired
    private MessageNotificationMetrics metrics;

    public MessageRecord findByIdempotencyKey(String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            return null;
        }
        LambdaQueryWrapper<MessageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MessageRecord::getIdempotencyKey, idempotencyKey.trim());
        return recordMapper.selectOne(wrapper);
    }

    public MessageRecord createPending(IdempotentMessageSendRequest request) {
        MessageRecord record = new MessageRecord();
        record.setIdempotencyKey(request.idempotencyKey());
        record.setChannel(request.channelCode());
        record.setReceiverId(request.receiver());
        record.setReceiverName(request.receiverName());
        record.setReceiverContact(request.receiver());
        record.setTitle(request.title());
        record.setContent(request.content());
        record.setTemplateCode(parameter(request, "templateCode"));
        record.setBizType(parameter(request, "bizType"));
        record.setBizId(parameter(request, "bizId"));
        record.setEventType(parameter(request, "eventType"));
        record.setOrgId(parameter(request, "orgId"));
        record.setSendStatus(0);
        record.setRetryCount(0);
        record.setCreateTime(currentEpochSeconds());
        try {
            recordMapper.insert(record);
            return record;
        } catch (DuplicateKeyException duplicate) {
            return findByIdempotencyKey(request.idempotencyKey());
        }
    }

    public boolean claimForSend(Long id) {
        if (id == null) {
            return false;
        }
        MessageRecord update = new MessageRecord();
        update.setSendStatus(1);
        UpdateWrapper<MessageRecord> where = new UpdateWrapper<>();
        where.eq("id", id).in("send_status", 0, 3);
        return recordMapper.update(update, where) == 1;
    }

    public void markSucceeded(Long id, String providerMessageId) {
        MessageRecord record = new MessageRecord();
        record.setId(id);
        record.setSendStatus(2);
        record.setProviderMessageId(providerMessageId);
        record.setSendTime(currentEpochSeconds());
        recordMapper.updateById(record);
    }

    public void markFailed(Long id, String errorCode, String errorMessage) {
        MessageRecord record = new MessageRecord();
        record.setId(id);
        record.setSendStatus(3);
        record.setErrorCode(errorCode);
        record.setFailReason(errorMessage);
        record.setSendTime(currentEpochSeconds());
        recordMapper.updateById(record);
    }

    private String parameter(IdempotentMessageSendRequest request, String name) {
        Object value = request.params().get(name);
        return value == null ? null : value.toString();
    }

    private int currentEpochSeconds() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public Map<String, Object> list(
            MessageRequestContext context, int page, int size, MessageRecordFilter filter) {
        MessageRecordFilter safeFilter = filter == null
                ? new MessageRecordFilter(null, null, null, null, null, null, null)
                : filter;
        Page<MessageRecord> result = recordMapper.selectFilteredPage(
                new Page<>(page, size), context.orgId(), safeFilter);
        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords().stream().map(this::toListItem).toList());
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("size", size);
        return data;
    }

    public Map<String, Object> statistics(
            MessageRequestContext context, MessageRecordFilter filter) {
        MessageRecordFilter safeFilter = filter == null
                ? new MessageRecordFilter(null, null, null, null, null, null, null)
                : filter;
        return recordMapper.selectFilteredStatistics(context.orgId(), safeFilter);
    }

    public MessageRecordDetailResponse detail(MessageRequestContext context, Long id) {
        MessageRecord record = recordMapper.selectOwnedById(id, context.orgId());
        if (record == null) {
            throw new ResponseStatusException(NOT_FOUND, "记录不存在");
        }
        return new MessageRecordDetailResponse(
                record.getId(), record.getTemplateCode(), record.getBizType(), record.getBizId(),
                record.getEventType(), record.getChannel(), record.getReceiverName(),
                mask(record.getReceiverContact()), record.getTitle(), record.getSendStatus(),
                record.getSendTime(), record.getFailReason(), record.getErrorCode(),
                record.getProviderMessageId(), record.getRetryCount(), record.getCreateTime(),
                digest(record.getIdempotencyKey()));
    }

    public IdempotentMessageSendResponse retry(MessageRequestContext context, Long id) {
        MessageRecord record = recordMapper.selectOwnedById(id, context.orgId());
        if (record == null) {
            throw new ResponseStatusException(NOT_FOUND, "记录不存在");
        }
        if (!Integer.valueOf(3).equals(record.getSendStatus())) {
            throw new ResponseStatusException(CONFLICT, "仅失败记录可重投");
        }
        if (!StringUtils.hasText(record.getIdempotencyKey())
                || !StringUtils.hasText(record.getChannel())
                || !StringUtils.hasText(record.getReceiverContact())) {
            throw new ResponseStatusException(CONFLICT, "记录缺少可重投快照");
        }
        if (recordMapper.claimFailedForRetry(id, context.orgId()) != 1) {
            throw new ResponseStatusException(CONFLICT, "记录状态已变化");
        }
        Map<String, Object> params = new HashMap<>();
        put(params, "orgId", record.getOrgId());
        put(params, "bizType", record.getBizType());
        put(params, "bizId", record.getBizId());
        put(params, "eventType", record.getEventType());
        IdempotentMessageSendResponse result;
        try {
            result = deliveryService.retry(
                    new IdempotentMessageSendRequest(
                            record.getIdempotencyKey(), record.getChannel(), record.getReceiverContact(),
                            record.getReceiverName(), record.getTitle(), record.getContent(), params));
        } catch (RuntimeException ignored) {
            result = new IdempotentMessageSendResponse(
                    false, null, false, "SEND_ERROR", "发送失败");
        }
        if (result.success()) {
            recordMapper.markRetrySucceeded(
                    id, context.orgId(), result.messageId(), currentEpochSeconds());
            metrics.recordRetry("succeeded");
        } else {
            recordMapper.markRetryFailed(
                    id, context.orgId(), result.errorCode(), result.errorMessage(), currentEpochSeconds());
            metrics.recordRetry("failed");
        }
        return result;
    }

    private void put(Map<String, Object> params, String key, String value) {
        if (value != null) {
            params.put(key, value);
        }
    }

    private MessageRecordListItemResponse toListItem(MessageRecord record) {
        return new MessageRecordListItemResponse(
                record.getId(), record.getBizType(), record.getBizId(), record.getEventType(),
                record.getChannel(), record.getReceiverName(), record.getTitle(),
                record.getSendStatus(), record.getSendTime(), record.getErrorCode(),
                record.getRetryCount(), record.getCreateTime());
    }

    private String mask(String value) {
        if (value == null || value.length() <= 4) {
            return value == null ? null : "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    private String digest(String value) {
        if (value == null) {
            return null;
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 4);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    /**
     * 创建发送记录
     * 
     * @param record 记录信息
     * @return 是否成功
     */
    public boolean createRecord(MessageRecord record) {
        record.setCreateTime((int)(System.currentTimeMillis() / 1000));
        // 保持传入的状态，如果没有设置则默认为待发送
        if (record.getSendStatus() == null) {
            // 待发送
            record.setSendStatus(0);
        }
        if (record.getRetryCount() == null) {
            record.setRetryCount(0);
        }
        return recordMapper.insert(record) > 0;
    }

    /**
     * 更新发送状态
     * 
     * @param id 记录ID
     * @param sendStatus 发送状态
     * @param failReason 失败原因
     * @return 是否成功
     */
    public boolean updateSendStatus(Long id, Integer sendStatus, String failReason) {
        MessageRecord record = new MessageRecord();
        record.setId(id);
        record.setSendStatus(sendStatus);
        record.setFailReason(failReason);
        // 发送成功
        if (sendStatus == 2) {
            record.setSendTime((int)(System.currentTimeMillis() / 1000));
        }
        return recordMapper.updateById(record) > 0;
    }

}
