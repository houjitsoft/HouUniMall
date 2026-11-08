package com.houjiang.merchant.common.exception;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.common.enums.ErrorEnum;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionTest {

    private final GlobalException handler = new GlobalException();

    @Test
    void merchantBusinessErrorsTakePriorityOverSharedRuntimeFallback() {
        com.houjiang.commons.exception.GlobalExceptionHandler sharedHandler =
                new com.houjiang.commons.exception.GlobalExceptionHandler();

        assertTrue(AnnotationAwareOrderComparator.INSTANCE.compare(handler, sharedHandler) < 0,
                "merchant advice must handle BaseException before the shared RuntimeException advice");
    }

    @Test
    void hidesRuntimeExceptionDetailsAndLogsFullException() {
        RuntimeException exception = new RuntimeException(
                "SELECT password FROM hou_merchant_admin at Mapper.java:41");
        Logger logger = (Logger) LoggerFactory.getLogger(GlobalException.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            AjaxResult<Object> result = handler.handleException(exception);

            assertSystemError(result);
            ILoggingEvent event = appender.list.get(appender.list.size() - 1);
            assertEquals("系统异常", event.getFormattedMessage());
            assertNotNull(event.getThrowableProxy());
            assertEquals(exception.getClass().getName(), event.getThrowableProxy().getClassName());
            assertEquals(exception.getMessage(), event.getThrowableProxy().getMessage());
            assertTrue(event.getThrowableProxy().getStackTraceElementProxyArray().length > 0);
            assertFalse(event.getFormattedMessage().contains("password"));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void hidesNullPointerExceptionDetails() {
        AjaxResult<Object> result = handler.handleException(
                new NullPointerException("com.houjiang.merchant.service.SecretService"));

        assertSystemError(result);
    }

    @Test
    void hidesMybatisPlusExceptionSqlDetails() {
        AjaxResult<Object> result = handler.handleMybatisPlusException(
                new MybatisPlusException("SELECT password FROM hou_merchant_admin at Mapper.java:41"));

        assertSystemError(result);
    }

    @Test
    void preservesBaseExceptionBusinessContract() {
        BaseException exception = new BaseException(27, "库存不足", 0);

        AjaxResult<Object> result = handler.handleException(exception);

        assertEquals(27, result.getCode());
        assertEquals("库存不足", result.getMsg());
        assertEquals(0, result.getShow());
        assertEquals(List.of(), result.getData());
    }

    @Test
    void preservesOperateAndLoginExceptionMessages() {
        AjaxResult<Object> operate = handler.handleException(new OperateException("商品已下架"));
        AjaxResult<Object> login = handler.handleException(new LoginException(0, "账号或密码错误"));

        assertEquals("商品已下架", operate.getMsg());
        assertEquals(ErrorEnum.SHOW_MSG.getCode(), operate.getShow());
        assertEquals("账号或密码错误", login.getMsg());
        assertEquals(ErrorEnum.SHOW_MSG.getCode(), login.getShow());
    }

    @Test
    void hidesJsonParserInternalMessage() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Cannot deserialize value of type com.houjiang.SecretPayload from String password");

        AjaxResult<Object> result = handler.handleHttpMessageNotReadableException(exception);

        assertEquals(ErrorEnum.PARAMS_TYPE_ERROR.getCode(), result.getCode());
        assertEquals(ErrorEnum.PARAMS_TYPE_ERROR.getMsg(), result.getMsg());
        assertEquals(ErrorEnum.SHOW_MSG.getCode(), result.getShow());
    }

    @Test
    void hidesIllegalArgumentInternalMessage() {
        AjaxResult<Object> result = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Mapper.java:41 password column is invalid"));

        assertEquals(ErrorEnum.ASSERT_ARGUMENT_ERROR.getCode(), result.getCode());
        assertEquals(ErrorEnum.ASSERT_ARGUMENT_ERROR.getMsg(), result.getMsg());
        assertEquals(ErrorEnum.SHOW_MSG.getCode(), result.getShow());
    }

    private static void assertSystemError(AjaxResult<Object> result) {
        assertEquals(ErrorEnum.SYSTEM_ERROR.getCode(), result.getCode());
        assertEquals(ErrorEnum.SYSTEM_ERROR.getMsg(), result.getMsg());
        assertEquals(ErrorEnum.SHOW_MSG.getCode(), result.getShow());
        assertEquals(List.of(), result.getData());

        String response = result.getMsg();
        assertFalse(response.contains("SELECT"));
        assertFalse(response.contains("Mapper"));
        assertFalse(response.contains("password"));
        assertFalse(response.contains("Exception"));
        assertTrue(response.chars().allMatch(character -> character < 128 || Character.isLetter(character)));
    }
}
