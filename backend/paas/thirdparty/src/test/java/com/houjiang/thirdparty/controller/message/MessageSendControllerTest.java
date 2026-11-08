package com.houjiang.thirdparty.controller.message;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.service.message.MessageSendService;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSendControllerTest {

    @Mock
    private MessageSendService messageSendService;

    private MessageSendController controller;

    @BeforeEach
    void setUp() {
        controller = new MessageSendController();
        ReflectionTestUtils.setField(controller, "messageSendService", messageSendService);
    }

    @Test
    void byChannelUsesStrongIdempotentContractWhenKeyIsPresent() {
        when(messageSendService.sendIdempotently(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new IdempotentMessageSendResponse(
                        true, "provider-81", true, null, null));

        ResponseResult<SendResult> response = controller.sendByChannel(Map.of(
                "idempotencyKey", "PM:81:key:USER-501:INBOX",
                "channelCode", "INBOX",
                "receiver", "501",
                "receiverName", "李四",
                "title", "任务通知",
                "content", "内容",
                "params", Map.of("orgId", "7", "bizType", "PM_TASK", "bizId", "42")));

        ArgumentCaptor<IdempotentMessageSendRequest> request =
                ArgumentCaptor.forClass(IdempotentMessageSendRequest.class);
        verify(messageSendService).sendIdempotently(request.capture());
        verify(messageSendService, never()).sendByChannel(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
        assertEquals("PM:81:key:USER-501:INBOX", request.getValue().idempotencyKey());
        assertEquals("李四", request.getValue().receiverName());
        assertEquals("7", request.getValue().params().get("orgId"));
        assertTrue(response.getData().isSuccess());
        assertTrue(response.getData().isIdempotentReplay());
        assertEquals("provider-81", response.getData().getMessageId());
    }

    @Test
    void byChannelKeepsLegacyPathWhenKeyIsAbsent() {
        when(messageSendService.sendByChannel(
                "SMS", "13800000000", "标题", "内容", Map.of("orgId", "7")))
                .thenReturn(SendResult.success("legacy-1"));

        ResponseResult<SendResult> response = controller.sendByChannel(Map.of(
                "channelCode", "SMS",
                "receiver", "13800000000",
                "title", "标题",
                "content", "内容",
                "params", Map.of("orgId", "7")));

        assertTrue(response.getData().isSuccess());
        assertEquals("legacy-1", response.getData().getMessageId());
        verify(messageSendService, never()).sendIdempotently(
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void idempotentFailureKeepsStableErrorInTheResponseBody() {
        when(messageSendService.sendIdempotently(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new IdempotentMessageSendResponse(
                        false, null, false, "CHANNEL_DISABLED", "渠道未配置或未启用"));

        ResponseResult<SendResult> response = controller.sendByChannel(Map.of(
                "idempotencyKey", "PM:82:key:USER-501:EMAIL",
                "channelCode", "EMAIL",
                "receiver", "user@example.com",
                "title", "任务通知",
                "content", "内容"));

        assertFalse(response.getData().isSuccess());
        assertEquals("CHANNEL_DISABLED", response.getData().getErrorCode());
        assertEquals("渠道未配置或未启用", response.getData().getErrorMessage());
    }
}
