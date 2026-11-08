package com.houjiang.thirdparty.service.message.impl;

import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.entity.message.MessageRecord;
import com.houjiang.thirdparty.entity.message.MessageTemplate;
import com.houjiang.thirdparty.service.message.EmailQueueService;
import com.houjiang.thirdparty.service.message.EmailSettingsService;
import com.houjiang.thirdparty.service.message.MessageChannelConfigResolver;
import com.houjiang.thirdparty.service.message.MessageChannelService;
import com.houjiang.thirdparty.service.message.MessageDeliveryService;
import com.houjiang.thirdparty.service.message.MessageDispatchService;
import com.houjiang.thirdparty.service.message.MessageRecordService;
import com.houjiang.thirdparty.service.message.MessageTemplateService;
import com.houjiang.thirdparty.service.message.sender.MessageSender;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSendServiceImplTest {

    @Mock
    private MessageChannelService channelService;
    @Mock
    private MessageTemplateService templateService;
    @Mock
    private MessageRecordService recordService;
    @Mock
    private MessageChannelConfigResolver channelConfigResolver;
    @Mock
    private EmailSettingsService emailSettingsService;
    @Mock
    private EmailQueueService emailQueueService;
    @Mock
    private MessageSender inboxSender;

    private MessageSendServiceImpl service;
    private MessageDispatchService dispatchService;
    private MessageDeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new MessageDeliveryService();
        ReflectionTestUtils.setField(deliveryService, "channelConfigResolver", channelConfigResolver);
        ReflectionTestUtils.setField(deliveryService, "senders", List.of(inboxSender));
        dispatchService = new MessageDispatchService();
        ReflectionTestUtils.setField(dispatchService, "recordService", recordService);
        ReflectionTestUtils.setField(dispatchService, "deliveryService", deliveryService);
        service = new MessageSendServiceImpl();
        ReflectionTestUtils.setField(service, "channelService", channelService);
        ReflectionTestUtils.setField(service, "templateService", templateService);
        ReflectionTestUtils.setField(service, "emailSettingsService", emailSettingsService);
        ReflectionTestUtils.setField(service, "emailQueueService", emailQueueService);
        ReflectionTestUtils.setField(service, "dispatchService", dispatchService);
        lenient().when(inboxSender.getChannelCode()).thenReturn("INBOX");
    }

    @Test
    void successfulIdempotencyKeyReplaysOriginalMessageWithoutCallingSenderAgain() {
        IdempotentMessageSendRequest request = request("PM:91:abc:USER-501:INBOX");
        MessageRecord pending = record(91L, 0, null);
        MessageRecord succeeded = record(91L, 2, "3021");
        when(recordService.findByIdempotencyKey(request.idempotencyKey()))
                .thenReturn(null, succeeded);
        when(recordService.createPending(request)).thenReturn(pending);
        when(recordService.claimForSend(91L)).thenReturn(true);
        when(inboxSender.send(anyString(), anyString(), anyString(), anyMap(), anyMap()))
                .thenReturn(SendResult.success("3021"));

        IdempotentMessageSendResponse first = service.sendIdempotently(request);
        IdempotentMessageSendResponse replay = service.sendIdempotently(request);

        assertTrue(first.success());
        assertFalse(first.idempotentReplay());
        assertEquals("3021", replay.messageId());
        assertTrue(replay.idempotentReplay());
        verify(inboxSender, times(1))
                .send(eq("501"), eq("任务通知"), eq("内容"), eq(Map.of()),
                        org.mockito.ArgumentMatchers.argThat(params ->
                                request.idempotencyKey().equals(params.get("idempotencyKey"))));
        verify(recordService).markSucceeded(91L, "3021");
        verify(channelConfigResolver, never()).isChannelAvailable(eq("INBOX"), any());
    }

    @Test
    void failedRecordCanBeClaimedAgainWithTheSameKey() {
        IdempotentMessageSendRequest request = request("PM:92:retry:USER-501:INBOX");
        when(recordService.findByIdempotencyKey(request.idempotencyKey()))
                .thenReturn(record(92L, 3, null));
        when(recordService.claimForSend(92L)).thenReturn(true);
        when(inboxSender.send(anyString(), anyString(), anyString(), anyMap(), anyMap()))
                .thenReturn(SendResult.success("3022"));

        IdempotentMessageSendResponse response = service.sendIdempotently(request);

        assertTrue(response.success());
        assertFalse(response.idempotentReplay());
        verify(recordService).markSucceeded(92L, "3022");
    }

    @Test
    void senderFailureUpdatesTheOwnedRecordWithStableError() {
        IdempotentMessageSendRequest request = request("PM:93:failure:USER-501:INBOX");
        when(recordService.findByIdempotencyKey(request.idempotencyKey())).thenReturn(null);
        when(recordService.createPending(request)).thenReturn(record(93L, 0, null));
        when(recordService.claimForSend(93L)).thenReturn(true);
        when(inboxSender.send(anyString(), anyString(), anyString(), anyMap(), anyMap()))
                .thenReturn(SendResult.fail("SAVE_ERROR", "保存失败"));

        IdempotentMessageSendResponse response = service.sendIdempotently(request);

        assertFalse(response.success());
        assertEquals("SAVE_ERROR", response.errorCode());
        verify(recordService).markFailed(93L, "SAVE_ERROR", "保存失败");
    }

    @Test
    void templateSendDelegatesToTheSingleRecordOwningDirectSend() {
        MessageTemplate template = new MessageTemplate();
        template.setTemplateCode("PM_TASK");
        template.setStatus(1);
        template.setChannels("INBOX");
        template.setTitleTemplate("任务 ${name}");
        template.setContentTemplate("内容 ${name}");
        template.setBizType("PM_TASK");
        when(templateService.getTemplateByCode("PM_TASK")).thenReturn(template);
        when(recordService.findByIdempotencyKey("template-key")).thenReturn(null);
        when(recordService.createPending(any(IdempotentMessageSendRequest.class)))
                .thenReturn(record(94L, 0, null));
        when(recordService.claimForSend(94L)).thenReturn(true);
        when(inboxSender.send(anyString(), anyString(), anyString(), anyMap(), anyMap()))
                .thenReturn(SendResult.success("3023"));

        List<SendResult> results = service.sendByTemplate(
                "PM_TASK", "501", Map.of("name", "验收", "idempotencyKey", "template-key"));

        assertEquals(1, results.size());
        assertTrue(results.getFirst().isSuccess());
        ArgumentCaptor<IdempotentMessageSendRequest> request =
                ArgumentCaptor.forClass(IdempotentMessageSendRequest.class);
        verify(recordService, times(1)).createPending(request.capture());
        assertEquals("PM_TASK", request.getValue().params().get("templateCode"));
        assertEquals("PM_TASK", request.getValue().params().get("bizType"));
        verify(recordService, never()).createRecord(any(MessageRecord.class));
    }

    @Test
    void requestRejectsMissingDeliveryIdentity() {
        assertThrows(IllegalArgumentException.class, () -> new IdempotentMessageSendRequest(
                " ", "INBOX", "501", "李四", "任务通知", "内容", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new IdempotentMessageSendRequest(
                "key", " ", "501", "李四", "任务通知", "内容", Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new IdempotentMessageSendRequest(
                "key", "INBOX", " ", "李四", "任务通知", "内容", Map.of()));
    }

    @Test
    void concurrentClaimReturnsStableInProgressErrorWithoutCallingSender() {
        IdempotentMessageSendRequest request = request("PM:95:busy:USER-501:INBOX");
        when(recordService.findByIdempotencyKey(request.idempotencyKey()))
                .thenReturn(record(95L, 1, null), record(95L, 1, null));
        when(recordService.claimForSend(95L)).thenReturn(false);

        IdempotentMessageSendResponse response = service.sendIdempotently(request);

        assertFalse(response.success());
        assertEquals("SEND_IN_PROGRESS", response.errorCode());
        verify(inboxSender, never()).send(anyString(), anyString(), anyString(), anyMap(), anyMap());
    }

    @Test
    void groupDeliveryUsesOnlyRequestScopedConfigAndSkipsStoredChannelConfig() {
        MessageSender groupSender = org.mockito.Mockito.mock(MessageSender.class);
        ReflectionTestUtils.setField(deliveryService, "senders", List.of(inboxSender, groupSender));
        when(groupSender.getChannelCode()).thenReturn("WECOM_GROUP");
        Map<String, Object> transientConfig = Map.of(
                "webhook", "https://example.invalid/secret-hook",
                "chatId", "group-88");
        IdempotentMessageSendRequest request = new IdempotentMessageSendRequest(
                "PM:96:group:GROUP-wecom-88:WECOM_GROUP",
                "WECOM_GROUP",
                "group-88",
                "项目群",
                "项目通知",
                "内容",
                Map.of("orgId", "7", "groupPlatform", "wecom", "groupConfig", transientConfig));
        when(recordService.findByIdempotencyKey(request.idempotencyKey())).thenReturn(null);
        when(recordService.createPending(request)).thenReturn(record(96L, 0, null));
        when(recordService.claimForSend(96L)).thenReturn(true);
        when(groupSender.send(anyString(), anyString(), anyString(), anyMap(), anyMap()))
                .thenReturn(SendResult.success("group-message-1"));

        IdempotentMessageSendResponse response = service.sendIdempotently(request);

        assertTrue(response.success());
        verify(groupSender).send(
                eq("group-88"),
                eq("项目通知"),
                eq("内容"),
                eq(transientConfig),
                org.mockito.ArgumentMatchers.argThat(params ->
                        request.idempotencyKey().equals(params.get("idempotencyKey"))
                                && transientConfig.equals(params.get("groupConfig"))));
        verify(channelConfigResolver, never()).resolve(eq("WECOM_GROUP"), any());
        verify(channelConfigResolver, never()).isChannelAvailable(eq("WECOM_GROUP"), any());
    }

    @Test
    void legacyResultExposesIdempotentReplay() {
        Map<String, Object> params = Map.of("idempotencyKey", "legacy-replay-key");
        when(recordService.findByIdempotencyKey("legacy-replay-key"))
                .thenReturn(record(97L, 2, "3024"));

        SendResult result = service.sendByChannel(
                "INBOX", "501", "任务通知", "内容", params);

        assertTrue(result.isSuccess());
        assertTrue(result.isIdempotentReplay());
        assertEquals("3024", result.getMessageId());
        verify(inboxSender, never()).send(anyString(), anyString(), anyString(), anyMap(), anyMap());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CHANNEL_DISABLED", "CHANNEL_CONFIG_EMPTY"})
    void externalChannelConfigurationFailuresUseStableErrors(String expectedCode) {
        MessageSender emailSender = org.mockito.Mockito.mock(MessageSender.class);
        ReflectionTestUtils.setField(deliveryService, "senders", List.of(inboxSender, emailSender));
        when(emailSender.getChannelCode()).thenReturn("EMAIL");
        IdempotentMessageSendRequest request = new IdempotentMessageSendRequest(
                "PM:98:" + expectedCode + ":USER-501:EMAIL",
                "EMAIL",
                "user@example.com",
                "李四",
                "任务通知",
                "内容",
                Map.of("orgId", "7"));
        when(recordService.findByIdempotencyKey(request.idempotencyKey())).thenReturn(null);
        when(recordService.createPending(request)).thenReturn(record(98L, 0, null));
        when(recordService.claimForSend(98L)).thenReturn(true);
        boolean available = "CHANNEL_CONFIG_EMPTY".equals(expectedCode);
        when(channelConfigResolver.isChannelAvailable("EMAIL", "7")).thenReturn(available);
        if (available) {
            when(channelConfigResolver.resolve("EMAIL", "7")).thenReturn(Map.of());
        }

        IdempotentMessageSendResponse response = service.sendIdempotently(request);

        assertFalse(response.success());
        assertEquals(expectedCode, response.errorCode());
        verify(recordService).markFailed(eq(98L), eq(expectedCode), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString(), anyMap(), anyMap());
    }

    private static IdempotentMessageSendRequest request(String key) {
        return new IdempotentMessageSendRequest(
                key,
                "INBOX",
                "501",
                "李四",
                "任务通知",
                "内容",
                Map.of("orgId", "7", "bizType", "PM_TASK", "bizId", "42"));
    }

    private static MessageRecord record(Long id, int status, String providerMessageId) {
        MessageRecord record = new MessageRecord();
        record.setId(id);
        record.setSendStatus(status);
        record.setProviderMessageId(providerMessageId);
        return record;
    }
}
