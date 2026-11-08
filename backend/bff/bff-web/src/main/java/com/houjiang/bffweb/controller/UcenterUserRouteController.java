package com.houjiang.bffweb.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/** Consumer user-service routes not covered by the typed BFF user controller. */
@RestController
public class UcenterUserRouteController {
    private final UcenterServiceForwarder forwarder;

    @Autowired
    public UcenterUserRouteController(@Value("${feign.userms.url:}") String configuredUrl,
                                      DiscoveryClient discovery) {
        this(configuredUrl, discovery, UcenterServiceForwarder.httpClient());
    }

    UcenterUserRouteController(String configuredUrl, DiscoveryClient discovery, RestTemplate http) {
        this.forwarder = new UcenterServiceForwarder("userms", "/web/user", configuredUrl, discovery, http);
    }

    @PostMapping({
            "/web/user/user/ms/updateUserMobile",
            "/web/user/user/ms/uploadPhoto",
            // 老 H5 登录态校验 / 会员信息 / 会员折扣：详情页与结算页必调。
            // 缺这几条时 checkToken() 直接判定「未登录」并跳 /general/landingPage，
            // 表现是点「立即购买」无论如何都回登录页。
            "/web/user/user/ms/getuser",
            "/web/user/user/ms/getuserMemberInformation",
            "/web/user/user/ms/queryPersonalDiscount",
            "/web/user/user/address/getDef",
            "/web/user/user/favoritesGoods/check",
            "/web/user/user/favoritesGoods/add",
            "/web/user/user/favoritesGoods/query",
            "/web/user/user/favoritesShop/check",
            "/web/user/user/favoritesShop/add",
            "/web/user/user/plusInfo/selectPlusMember",
            "/web/user/user/plusInfoOrder/get",
            "/web/user/user/vkSet/vkQuerySetting",
            "/web/user/user/platformMessage/queryPlatformMessage",
            "/web/user/user/platformMessage/readPlatformMessage",
            "/web/user/user/platformMessage/deletePlatformMessage",
            "/web/user/user/businessInfo/info/query",
            "/web/user/user/businessInfo/info/update",
            "/web/user/user/favoritesGoods/get",
            "/web/user/user/favoritesGoods/delete",
            "/web/user/user/favoritesShop/get",
            "/web/user/user/favoritesShop/delete",
            "/web/user/user/pointsLog/integral/detail"
    })
    public ResponseEntity<byte[]> post(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.POST));
    }
}
