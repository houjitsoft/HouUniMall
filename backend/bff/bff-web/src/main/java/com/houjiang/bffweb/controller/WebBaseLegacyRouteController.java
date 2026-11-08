package com.houjiang.bffweb.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * H5（uni-app）历史沿用的 base 直通路由，转发到 base 服务。
 *
 * <p>背景：WebBaseController 只保留了迁移后的新式接口（/web/base/sys-config/** 等），
 * 而 H5 客户端仍在调用老式路径（/web/base/baseModularizationd/**、/web/base/base/sysConfig/** …）。
 * BFF 没有对应映射时 Spring 抛 NoResourceFoundException，被全局异常处理器转成
 * code=20001「请求的资源不存在」、data=null；H5 侧拿到 null 后直接
 * res.data[0] / res.data.pageData 抛 TypeError，首页整页白屏。
 *
 * <p>这里按「路径族」白名单放行（不是开放代理）：只放行 H5 真实使用过的这些前缀，
 * 与 UcenterBaseRouteController 的既有做法保持一致；base 地址取 feign.base.url，
 * 为空时回落到注册中心里的 base 实例。
 */
@RestController
public class WebBaseLegacyRouteController {

    private final UcenterServiceForwarder forwarder;

    @Autowired
    public WebBaseLegacyRouteController(@Value("${feign.base.url:}") String configuredUrl,
                                        DiscoveryClient discovery) {
        this(configuredUrl, discovery, UcenterServiceForwarder.httpClient());
    }

    WebBaseLegacyRouteController(String configuredUrl, DiscoveryClient discovery, RestTemplate http) {
        this.forwarder = new UcenterServiceForwarder("base", "/web/base", configuredUrl, discovery, http);
    }

    @PostMapping({
            "/web/base/baseModularizationd/**",
            "/web/base/base/**",
            "/web/base/baseCountry/**",
            "/web/base/baseFeedbackInfo/**",
            "/web/base/baseFeedbackType/**",
            "/web/base/baseInformationClassify/**",
            "/web/base/baseInformationInfo/**",
            "/web/base/baseLanguage/**",
            "/web/base/baseVkSet/**",
            "/web/base/businessSwitch/**",
            "/web/base/web/index/**",
            "/web/base/wxSecurityCheck/**"})
    public ResponseEntity<byte[]> post(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.POST));
    }

    @GetMapping({
            "/web/base/baseModularizationd/**",
            "/web/base/base/**",
            "/web/base/baseCountry/**",
            "/web/base/baseFeedbackInfo/**",
            "/web/base/baseFeedbackType/**",
            "/web/base/baseInformationClassify/**",
            "/web/base/baseInformationInfo/**",
            "/web/base/baseLanguage/**",
            "/web/base/baseVkSet/**",
            "/web/base/businessSwitch/**",
            "/web/base/web/index/**",
            "/web/base/wxSecurityCheck/**"})
    public ResponseEntity<byte[]> get(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.GET));
    }
}