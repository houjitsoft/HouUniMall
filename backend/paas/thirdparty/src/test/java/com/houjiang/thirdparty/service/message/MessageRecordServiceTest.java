package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.dto.message.MessageRecordDetailResponse;
import com.houjiang.thirdparty.dto.message.MessageRecordFilter;
import com.houjiang.thirdparty.dto.message.MessageRecordListItemResponse;
import com.houjiang.thirdparty.entity.message.MessageRecord;
import com.houjiang.thirdparty.mapper.message.MessageRecordMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageRecordServiceTest {

    @Mock
    private MessageRecordMapper mapper;
    @Mock
    private MessageDeliveryService deliveryService;

    private MessageRecordService service;
    private SimpleMeterRegistry registry;
    private MessageRequestContext context;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        service = new MessageRecordService();
        ReflectionTestUtils.setField(service, "recordMapper", mapper);
        ReflectionTestUtils.setField(service, "deliveryService", deliveryService);
        ReflectionTestUtils.setField(service, "metrics", new MessageNotificationMetrics(registry));
        context = new MessageRequestContext("501", "7");
    }

    @Test
    void listAndStatisticsShareTheSameOrganizationFilters() {
        MessageRecordFilter filter = new MessageRecordFilter(
                "PM_TASK", "TASK_ASSIGNED", "INBOX", 3, "李", 1784800000, 1784890000);
        when(mapper.selectFilteredPage(any(), eq("7"), eq(filter))).thenReturn(new Page<>(1, 20));
        when(mapper.selectFilteredStatistics("7", filter)).thenReturn(Map.of("total", 1));

        service.list(context, 1, 20, filter);
        service.statistics(context, filter);

        verify(mapper).selectFilteredPage(any(), eq("7"), eq(filter));
        verify(mapper).selectFilteredStatistics("7", filter);
    }

    @Test
    void mapperTreatsPmPresetAsTaskAndProjectGroup() throws NoSuchMethodException {
        Select listQuery = MessageRecordMapper.class.getMethod(
                "selectFilteredPage", Page.class, String.class, MessageRecordFilter.class)
                .getAnnotation(Select.class);
        Select statisticsQuery = MessageRecordMapper.class.getMethod(
                "selectFilteredStatistics", String.class, MessageRecordFilter.class)
                .getAnnotation(Select.class);

        for (Select query : List.of(listQuery, statisticsQuery)) {
            String sql = String.join("\n", query.value());
            assertTrue(sql.contains("filter.bizType.equals(\"PM\")"));
            assertTrue(sql.contains("biz_type IN ('PM_TASK', 'PM_PROJECT')"));
        }
    }

    @Test
    void listReturnsDiagnosticRowsWithoutContactContentOrIdempotencyKey() {
        MessageRecord record = failedRecord();
        record.setReceiverContact("13800138000");
        Page<MessageRecord> page = new Page<>(1, 20);
        page.setRecords(List.of(record));
        when(mapper.selectFilteredPage(any(), eq("7"), any())).thenReturn(page);

        Map<String, Object> result = service.list(context, 1, 20, null);

        @SuppressWarnings("unchecked")
        List<MessageRecordListItemResponse> rows =
                (List<MessageRecordListItemResponse>) result.get("list");
        assertEquals(1, rows.size());
        assertEquals("李四", rows.getFirst().receiverName());
        assertEquals("TASK_ASSIGNED", rows.getFirst().eventType());
    }

    @Test
    void retryOnlyFailedOwnedRecordAndReusesSnapshotAndIdempotencyKey() {
        MessageRecord record = failedRecord();
        when(mapper.selectOwnedById(91L, "7")).thenReturn(record);
        when(mapper.claimFailedForRetry(91L, "7")).thenReturn(1);
        when(deliveryService.retry(any())).thenReturn(new IdempotentMessageSendResponse(
                true, "3021", false, null, null));
        when(mapper.markRetrySucceeded(eq(91L), eq("7"), eq("3021"), anyInt())).thenReturn(1);

        IdempotentMessageSendResponse result = service.retry(context, 91L);

        assertTrue(result.success());
        ArgumentCaptor<IdempotentMessageSendRequest> request =
                ArgumentCaptor.forClass(IdempotentMessageSendRequest.class);
        verify(deliveryService).retry(request.capture());
        assertEquals("PM:91:key", request.getValue().idempotencyKey());
        assertEquals("原始内容", request.getValue().content());
        assertEquals(1.0, registry.get("message.record.retry")
                .tag("result", "succeeded").counter().count());
    }

    @Test
    void successfulOrOtherOrganizationRecordCannotBeRetried() {
        MessageRecord succeeded = failedRecord();
        succeeded.setSendStatus(2);
        when(mapper.selectOwnedById(91L, "7")).thenReturn(succeeded);
        when(mapper.selectOwnedById(92L, "7")).thenReturn(null);

        ResponseStatusException conflict = assertThrows(
                ResponseStatusException.class, () -> service.retry(context, 91L));
        ResponseStatusException missing = assertThrows(
                ResponseStatusException.class, () -> service.retry(context, 92L));

        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        verify(deliveryService, never()).retry(any());
    }

    @Test
    void detailReturnsMaskedDiagnosticContractWithoutRawContentOrIdempotencyKey() {
        MessageRecord record = failedRecord();
        record.setReceiverContact("13800138000");
        when(mapper.selectOwnedById(91L, "7")).thenReturn(record);

        MessageRecordDetailResponse detail = service.detail(context, 91L);

        assertEquals("13****00", detail.receiverContact());
        assertEquals("a1afa82a", detail.idempotencyKeyDigest());
        assertEquals("TASK_ASSIGNED", detail.eventType());
    }

    @Test
    void unexpectedRetryFailureRestoresFailedStateAndReturnsStableError() {
        MessageRecord record = failedRecord();
        when(mapper.selectOwnedById(91L, "7")).thenReturn(record);
        when(mapper.claimFailedForRetry(91L, "7")).thenReturn(1);
        when(deliveryService.retry(any())).thenThrow(new IllegalStateException("secret provider detail"));
        when(mapper.markRetryFailed(eq(91L), eq("7"), eq("SEND_ERROR"), eq("发送失败"), anyInt()))
                .thenReturn(1);

        IdempotentMessageSendResponse result = service.retry(context, 91L);

        assertEquals(false, result.success());
        assertEquals("SEND_ERROR", result.errorCode());
        assertEquals("发送失败", result.errorMessage());
        verify(mapper).markRetryFailed(eq(91L), eq("7"), eq("SEND_ERROR"), eq("发送失败"), anyInt());
        assertEquals(1.0, registry.get("message.record.retry")
                .tag("result", "failed").counter().count());
    }

    @Test
    void incompleteLegacySnapshotIsRejectedBeforeClaiming() {
        MessageRecord record = failedRecord();
        record.setIdempotencyKey(null);
        when(mapper.selectOwnedById(91L, "7")).thenReturn(record);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> service.retry(context, 91L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(mapper, never()).claimFailedForRetry(any(), any());
        verify(deliveryService, never()).retry(any());
    }

    private MessageRecord failedRecord() {
        MessageRecord record = new MessageRecord();
        record.setId(91L);
        record.setOrgId("7");
        record.setIdempotencyKey("PM:91:key");
        record.setChannel("INBOX");
        record.setReceiverContact("501");
        record.setReceiverName("李四");
        record.setTitle("原始标题");
        record.setContent("原始内容");
        record.setBizType("PM_TASK");
        record.setBizId("42");
        record.setEventType("TASK_ASSIGNED");
        record.setSendStatus(3);
        record.setRetryCount(0);
        return record;
    }
}
