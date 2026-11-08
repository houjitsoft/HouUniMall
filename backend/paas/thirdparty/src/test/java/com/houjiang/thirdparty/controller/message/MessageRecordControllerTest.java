package com.houjiang.thirdparty.controller.message;

import com.houjiang.thirdparty.dto.message.MessageRecordFilter;
import com.houjiang.thirdparty.service.message.MessageRecordService;
import com.houjiang.thirdparty.service.message.MessageRequestContext;
import com.houjiang.thirdparty.service.message.MessageRequestContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageRecordControllerTest {

    @Mock private MessageRecordService recordService;
    @Mock private MessageRequestContextResolver contextResolver;
    private MessageRecordController controller;
    private MessageRequestContext context;

    @BeforeEach
    void setUp() {
        context = new MessageRequestContext("501", "7");
        controller = new MessageRecordController(recordService, contextResolver);
        when(contextResolver.requireRecordAdminCurrent()).thenReturn(context);
    }

    @Test
    void listStatisticsDetailAndRetryUseAuthorizedOrganizationContext() {
        MessageRecordFilter filter = new MessageRecordFilter(
                "PM_TASK", "TASK_ASSIGNED", "INBOX", 3, "李", 1, 2);

        controller.list(1, 20, "PM_TASK", "TASK_ASSIGNED", "INBOX", 3, "李", 1, 2);
        controller.statistics("PM_TASK", "TASK_ASSIGNED", "INBOX", 3, "李", 1, 2);
        controller.detail(91L);
        controller.retry(91L);

        verify(recordService).list(context, 1, 20, filter);
        verify(recordService).statistics(context, filter);
        verify(recordService).detail(context, 91L);
        verify(recordService).retry(context, 91L);
    }
}
