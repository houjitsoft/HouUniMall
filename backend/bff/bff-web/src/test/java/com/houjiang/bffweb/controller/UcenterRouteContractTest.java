package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.OrderServiceFeign;
import com.houjiang.bffweb.feign.UserServiceFeign;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UcenterRouteContractTest {

    @Test
    void exposesConsumerOrderListAndUserSettings() {
        assertTrue(postRoutes(WebOrderController.class).contains("/order/base/queryOrdersByPC"));
        assertTrue(postRoutes(OrderServiceFeign.class).contains("/order/base/queryOrdersByPC"));
        assertTrue(postRoutes(WebUserController.class).contains("/user/ms/userSetting/getInfo"));
        for (String path : Set.of("/user/ms/information/getUserInfo", "/user/ms/updateUserInfo",
                "/user/address/get", "/user/address/add", "/user/address/update", "/user/address/delete")) {
            assertTrue(postRoutes(WebUserController.class).contains(path), path);
        }
        assertTrue(postRoutes(UserServiceFeign.class).contains("/userSetting/getInfo"));
    }

    @Test
    void exposesOnlyConfirmedConsumerServiceRoutes() {
        for (String route : Set.of("/web/pay/payment/userResource/balanceDisplay",
                "/web/pay/pay/assetDetails/queryList", "/web/pay/pay/assetDetails/queryDetail",
                "/web/pay/pay/cardInfo/rechargeCard", "/web/pay/pay/entrance/userPay",
                "/web/pay/pay/WXPay/queryWxPayBalanceRecharge", "/web/pay/pay/userBase/setPaymentPassword")) {
            assertTrue(postRoutes(UcenterPaymentRouteController.class).contains(route), route);
        }
        for (String route : Set.of("/web/base/base/district/all/district/query",
                "/web/base/base/express/query/noToken")) {
            assertTrue(postRoutes(UcenterBaseRouteController.class).contains(route), route);
        }
        assertTrue(getRoutes(UcenterBaseRouteController.class).contains("/web/base/base/district/query"));
        for (String route : Set.of("/web/thirdparty/sms/verifiPhoneCode2",
                "/web/thirdparty/sms/sendPhoneCodeByToken", "/web/thirdparty/sms/checkoldphone",
                "/web/thirdparty/sms/checkNewphone")) {
            assertTrue(postRoutes(UcenterSmsRouteController.class).contains(route), route);
        }
        assertFalse(postRoutes(UcenterBaseRouteController.class).stream().anyMatch(route -> route.contains("Feedback")));
    }

    private static Set<String> postRoutes(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(mapping -> mapping != null && mapping.value().length > 0)
                .flatMap(mapping -> Arrays.stream(mapping.value()))
                .collect(Collectors.toSet());
    }

    private static Set<String> getRoutes(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .map(method -> method.getAnnotation(GetMapping.class))
                .filter(mapping -> mapping != null)
                .flatMap(mapping -> Arrays.stream(mapping.value()))
                .collect(Collectors.toSet());
    }
}
