package com.houjiang.thirdparty.controller.message;

import com.houjiang.thirdparty.service.message.MessageInboxService;
import com.houjiang.thirdparty.service.message.MessageRequestContext;
import com.houjiang.thirdparty.service.message.MessageRequestContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageInboxControllerTest {

    @Mock
    private MessageInboxService inboxService;
    @Mock
    private MessageRequestContextResolver contextResolver;

    private MessageInboxController controller;
    private MessageRequestContext context;

    @BeforeEach
    void setUp() {
        context = new MessageRequestContext("501", "7");
        controller = new MessageInboxController(inboxService, contextResolver);
        when(contextResolver.resolveCurrent()).thenReturn(context);
    }

    @Test
    void everyDataOperationUsesTheResolvedCurrentContext() {
        when(inboxService.list(context, 1, 20, 0, 2)).thenReturn(Map.of());

        controller.list(1, 20, 0, 2);
        controller.unreadCount();
        controller.detail(91L);
        controller.markAsRead(91L);
        controller.markAllAsRead();
        controller.delete(91L);

        verify(inboxService).list(context, 1, 20, 0, 2);
        verify(inboxService).unreadCount(context);
        verify(inboxService).detail(context, 91L);
        verify(inboxService).markAsRead(context, 91L);
        verify(inboxService).markAllAsRead(context);
        verify(inboxService).delete(context, 91L);
    }
}
