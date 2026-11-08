package com.houjiang.thirdparty.service.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.thirdparty.entity.message.MessageInbox;
import com.houjiang.thirdparty.feign.UserServiceClient;
import com.houjiang.thirdparty.mapper.message.MessageInboxMapper;
import com.houjiang.thirdparty.service.message.sender.InboxSender;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageInboxServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-24T08:00:00Z");

    @Mock
    private MessageInboxMapper mapper;
    @Mock
    private UserServiceClient userServiceClient;

    private MessageInboxService service;

    @BeforeEach
    void setUp() {
        service = new MessageInboxService(mapper, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void listReadDeleteAndReadAllAlwaysUseAuthenticatedOwner() {
        MessageRequestContext context = new MessageRequestContext("501", "7");
        when(mapper.selectOwnedPage(any(), eq("501"), eq("7"), eq(0), eq(2)))
                .thenReturn(new Page<>(1, 20));
        when(mapper.markOwnedRead(91L, "501", "7", (int) NOW.getEpochSecond())).thenReturn(1);
        when(mapper.markAllOwnedRead("501", "7", (int) NOW.getEpochSecond())).thenReturn(3);
        when(mapper.softDeleteOwned(91L, "501", "7", (int) NOW.getEpochSecond())).thenReturn(1);

        service.list(context, 1, 20, 0, 2);
        service.markAsRead(context, 91L);
        assertEquals(3, service.markAllAsRead(context));
        service.delete(context, 91L);

        verify(mapper).selectOwnedPage(any(), eq("501"), eq("7"), eq(0), eq(2));
        verify(mapper).markOwnedRead(91L, "501", "7", (int) NOW.getEpochSecond());
        verify(mapper).markAllOwnedRead("501", "7", (int) NOW.getEpochSecond());
        verify(mapper).softDeleteOwned(91L, "501", "7", (int) NOW.getEpochSecond());
    }

    @Test
    void zeroOwnedRowsReturnNotFoundInsteadOfLeakingExistence() {
        MessageRequestContext context = new MessageRequestContext("501", "7");
        when(mapper.markOwnedRead(91L, "501", "7", (int) NOW.getEpochSecond())).thenReturn(0);
        when(mapper.softDeleteOwned(92L, "501", "7", (int) NOW.getEpochSecond())).thenReturn(0);

        ResponseStatusException read = assertThrows(
                ResponseStatusException.class, () -> service.markAsRead(context, 91L));
        ResponseStatusException delete = assertThrows(
                ResponseStatusException.class, () -> service.delete(context, 92L));

        assertEquals(HttpStatus.NOT_FOUND, read.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, delete.getStatusCode());
    }

    @Test
    void resolverRejectsOrganizationOutsideAuthenticatedMembership() {
        MessageRequestContextResolver resolver = new MessageRequestContextResolver(userServiceClient);
        UserInfoVO user = new UserInfoVO();
        user.setUserId("501");
        when(userServiceClient.checkUserOrgExists("501", "8"))
                .thenReturn(ResponseResult.success(false));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> resolver.resolve(user, "8"));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void recordAdministrationRequiresExactPermission() {
        MessageRequestContextResolver resolver = new MessageRequestContextResolver(userServiceClient);
        UserInfoVO user = new UserInfoVO();
        user.setUserId("501");
        user.setLimits("shell:setting:message:view,shell:setting:message:retry");
        when(userServiceClient.checkUserOrgExists("501", "7"))
                .thenReturn(ResponseResult.success(true));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class, () -> resolver.requireRecordAdmin(user, "7"));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
    }

    @Test
    void inboxSenderPersistsOrganizationBusinessAndIdempotencyFields() {
        MessageInboxService inboxService = org.mockito.Mockito.mock(MessageInboxService.class);
        InboxSender sender = new InboxSender();
        ReflectionTestUtils.setField(sender, "inboxService", inboxService);
        when(inboxService.createIdempotently(any(MessageInbox.class))).thenAnswer(invocation -> {
            MessageInbox inbox = invocation.getArgument(0);
            inbox.setId(3021L);
            return inbox;
        });

        SendResult result = sender.send("501", "任务通知", "内容", Map.of(), Map.of(
                "orgId", "7",
                "bizType", "PM_TASK",
                "bizId", "42",
                "msgType", 2,
                "idempotencyKey", "PM:91:key"));

        ArgumentCaptor<MessageInbox> inbox = ArgumentCaptor.forClass(MessageInbox.class);
        verify(inboxService).createIdempotently(inbox.capture());
        assertEquals("7", inbox.getValue().getOrgId());
        assertEquals("42", inbox.getValue().getBizId());
        assertEquals("PM:91:key", inbox.getValue().getIdempotencyKey());
        assertTrue(result.isSuccess());
        assertEquals("3021", result.getMessageId());
    }

    @Test
    void duplicateInboxInsertReturnsTheOriginalMessage() {
        MessageInbox original = new MessageInbox();
        original.setId(3022L);
        original.setIdempotencyKey("PM:92:key");
        when(mapper.insert(any(MessageInbox.class)))
                .thenThrow(new DuplicateKeyException("duplicate"));
        when(mapper.selectByIdempotencyKey("PM:92:key")).thenReturn(original);
        MessageInbox attempted = new MessageInbox();
        attempted.setIdempotencyKey("PM:92:key");

        MessageInbox result = service.createIdempotently(attempted);

        assertSame(original, result);
    }
}
