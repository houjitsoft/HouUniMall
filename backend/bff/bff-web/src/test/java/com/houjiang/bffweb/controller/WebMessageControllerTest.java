package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.MessageServiceFeign;
import com.houjiang.bffweb.feign.MessageServiceFeignFallback;
import com.houjiang.bffweb.util.ResponseResult;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebMessageControllerTest {

    @Test
    void inboxRoutesNeverAcceptClientUserIdAndPreserveDownstreamErrors() {
        MessageServiceFeign feign = mock(MessageServiceFeign.class);
        WebMessageController controller = controllerWith(feign);
        ResponseResult<Object> forbidden = ResponseResult.fail(403, "无权访问当前组织收件箱");
        when(feign.getInboxList(1, 20, null, null)).thenReturn(forbidden);
        when(feign.getUnreadCount()).thenReturn(forbidden);
        when(feign.markAsRead(91L)).thenReturn(forbidden);
        when(feign.markAllAsRead()).thenReturn(forbidden);
        when(feign.deleteInboxMessage(91L)).thenReturn(forbidden);

        assertSame(forbidden, controller.getInboxList(1, 20, null, null));
        assertSame(forbidden, controller.getUnreadCount());
        assertSame(forbidden, controller.markAsRead(91L));
        assertSame(forbidden, controller.markAllAsRead());
        assertSame(forbidden, controller.deleteInboxMessage(91L));

        verify(feign).getInboxList(1, 20, null, null);
        verify(feign).getUnreadCount();
        verify(feign).markAsRead(91L);
        verify(feign).markAllAsRead();
        verify(feign).deleteInboxMessage(91L);
    }

    @Test
    void recordFiltersReachThirdpartyUnchangedForListAndStatistics() {
        MessageServiceFeign feign = mock(MessageServiceFeign.class);
        WebMessageController controller = controllerWith(feign);

        controller.getRecordList(1, 20, "INBOX", 3, "PM_TASK",
                "TASK_ASSIGNED", "李", 1_784_800_000, 1_784_890_000);
        controller.getRecordStatistics("INBOX", 3, "PM_TASK",
                "TASK_ASSIGNED", "李", 1_784_800_000, 1_784_890_000);

        verify(feign).getRecordList(1, 20, "INBOX", 3, "PM_TASK",
                "TASK_ASSIGNED", "李", 1_784_800_000, 1_784_890_000);
        verify(feign).getRecordStatistics("INBOX", 3, "PM_TASK",
                "TASK_ASSIGNED", "李", 1_784_800_000, 1_784_890_000);
    }

    @Test
    void fallbackReturnsExplicitCodesForPermissionMissingRateLimitServerAndTimeout() {
        MessageServiceFeignFallback factory = new MessageServiceFeignFallback();

        assertEquals(403, factory.create(feignFailure(403)).getUnreadCount().getCode());
        assertEquals(404, factory.create(feignFailure(404)).getUnreadCount().getCode());
        assertEquals(429, factory.create(feignFailure(429)).getUnreadCount().getCode());
        assertEquals(503, factory.create(feignFailure(500)).getUnreadCount().getCode());
        assertEquals(504, factory.create(new RuntimeException(
                new SocketTimeoutException("read timed out"))).getUnreadCount().getCode());
    }

    @Test
    void controllerMapsFeignErrorsWhenCircuitBreakerIsDisabled() {
        MessageServiceFeignFallback fallback = new MessageServiceFeignFallback();
        WebMessageController controller = controllerWith(mock(MessageServiceFeign.class));
        ReflectionTestUtils.setField(controller, "messageServiceFeignFallback", fallback);

        assertEquals(403, controller.handleMessageServiceFailure(feignFailure(403)).getCode());
        assertEquals(429, controller.handleMessageServiceFailure(feignFailure(429)).getCode());
    }

    private static WebMessageController controllerWith(MessageServiceFeign feign) {
        WebMessageController controller = new WebMessageController();
        ReflectionTestUtils.setField(controller, "messageServiceFeign", feign);
        return controller;
    }

    private static FeignException feignFailure(int status) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "/message/inbox/unread-count",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null);
        Response response = Response.builder()
                .status(status)
                .reason("failure")
                .request(request)
                .build();
        return FeignException.errorStatus("MessageServiceFeign#getUnreadCount", response);
    }
}
