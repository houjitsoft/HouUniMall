package com.houjiang.thirdparty.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AliyunSmsUtilsDeliveryStatusTest {

    @Test
    void pendingDeliveryMustNotBeReportedAsDelivered() throws Exception {
        Method statusMethod = AliyunSmsUtils.class.getDeclaredMethod("toDeliveryStatusText", Long.class);
        Method errorMethod = AliyunSmsUtils.class.getDeclaredMethod("toFriendlyDeliveryError", Long.class, String.class);
        statusMethod.setAccessible(true);
        errorMethod.setAccessible(true);

        assertEquals("等待运营商回执", statusMethod.invoke(null, 1L));
        assertEquals("等待运营商回执，暂未确认送达", errorMethod.invoke(null, 1L, null));
    }
}
