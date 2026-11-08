package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.entity.message.MessageRecord;
import com.houjiang.thirdparty.mapper.message.MessageRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageRecordServiceAtomicTest {

    @Mock
    private MessageRecordMapper recordMapper;

    private MessageRecordService service;

    @BeforeEach
    void setUp() {
        service = new MessageRecordService();
        ReflectionTestUtils.setField(service, "recordMapper", recordMapper);
    }

    @Test
    void createPendingPersistsOnlyTheDeliverySnapshot() {
        IdempotentMessageSendRequest request = request();
        when(recordMapper.insert(any(MessageRecord.class))).thenAnswer(invocation -> {
            MessageRecord record = invocation.getArgument(0);
            record.setId(41L);
            return 1;
        });

        MessageRecord record = service.createPending(request);

        assertEquals(41L, record.getId());
        assertEquals(request.idempotencyKey(), record.getIdempotencyKey());
        assertEquals("INBOX", record.getChannel());
        assertEquals("501", record.getReceiverId());
        assertEquals("501", record.getReceiverContact());
        assertEquals("PM_TASK", record.getBizType());
        assertEquals("42", record.getBizId());
        assertEquals("TASK_ASSIGNED", record.getEventType());
        assertEquals("7", record.getOrgId());
        assertEquals(0, record.getSendStatus());
        assertEquals(0, record.getRetryCount());
        assertTrue(record.getCreateTime() > 0);
    }

    @Test
    void duplicateInsertReturnsTheWinningRecord() {
        MessageRecord winner = new MessageRecord();
        winner.setId(42L);
        when(recordMapper.insert(any(MessageRecord.class)))
                .thenThrow(new DuplicateKeyException("duplicate"));
        when(recordMapper.selectOne(any())).thenReturn(winner);

        MessageRecord result = service.createPending(request());

        assertSame(winner, result);
    }

    @Test
    void claimUsesOneConditionalUpdateForPendingOrFailedRecords() {
        when(recordMapper.update(any(MessageRecord.class), any(Wrapper.class))).thenReturn(1);

        assertTrue(service.claimForSend(43L));

        ArgumentCaptor<MessageRecord> update = ArgumentCaptor.forClass(MessageRecord.class);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Wrapper> where = ArgumentCaptor.forClass(Wrapper.class);
        verify(recordMapper).update(update.capture(), where.capture());
        assertEquals(1, update.getValue().getSendStatus());
        String condition = where.getValue().getSqlSegment().toLowerCase();
        assertTrue(condition.contains("id"));
        assertTrue(condition.contains("send_status"));
        assertTrue(condition.contains(" in "));
    }

    @Test
    void terminalUpdatesKeepStableProviderAndErrorFields() {
        when(recordMapper.updateById(any(MessageRecord.class))).thenReturn(1);

        service.markSucceeded(44L, "provider-44");
        service.markFailed(45L, "SAVE_ERROR", "保存失败");

        ArgumentCaptor<MessageRecord> updates = ArgumentCaptor.forClass(MessageRecord.class);
        verify(recordMapper, org.mockito.Mockito.times(2)).updateById(updates.capture());
        MessageRecord success = updates.getAllValues().get(0);
        MessageRecord failure = updates.getAllValues().get(1);
        assertEquals(2, success.getSendStatus());
        assertEquals("provider-44", success.getProviderMessageId());
        assertTrue(success.getSendTime() > 0);
        assertEquals(3, failure.getSendStatus());
        assertEquals("SAVE_ERROR", failure.getErrorCode());
        assertEquals("保存失败", failure.getFailReason());
    }

    private static IdempotentMessageSendRequest request() {
        return new IdempotentMessageSendRequest(
                "PM:41:abc:USER-501:INBOX",
                "INBOX",
                "501",
                "李四",
                "任务通知",
                "内容",
                Map.of(
                        "orgId", "7",
                        "bizType", "PM_TASK",
                        "bizId", "42",
                        "eventType", "TASK_ASSIGNED",
                        "webhook", "must-not-be-persisted"));
    }
}
