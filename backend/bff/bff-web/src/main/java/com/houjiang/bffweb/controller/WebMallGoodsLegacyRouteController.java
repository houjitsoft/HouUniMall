package com.houjiang.bffweb.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * H5（uni-app）历史沿用的 mall/goods 双前缀路由，转发到 goods 服务。
 *
 * <p>H5 qM helper 以 {@code /web/mall/goods} 为基址再拼 {@code /goods/...}，
 * 实际请求形如 {@code /web/mall/goods/goods/common/h5/shopindex/query}。
 * MallGoodsController 只覆盖后台/新式路径，未覆盖这批双前缀消费者路径时
 * Spring 返回 code=20001，H5 首页/分类/猜你喜欢整页空白。
 *
 * <p>转发前缀 {@code /web/mall/goods}：
 * {@code /web/mall/goods/goods/common/... → /goods/common/...}。
 */
@RestController
public class WebMallGoodsLegacyRouteController {

    private final UcenterServiceForwarder forwarder;

    @Autowired
    public WebMallGoodsLegacyRouteController(@Value("${feign.goods.url:}") String configuredUrl,
                                             DiscoveryClient discovery) {
        this(configuredUrl, discovery, UcenterServiceForwarder.httpClient());
    }

    WebMallGoodsLegacyRouteController(String configuredUrl, DiscoveryClient discovery, RestTemplate http) {
        this.forwarder = new UcenterServiceForwarder("goods", "/web/mall/goods", configuredUrl, discovery, http);
    }

    @PostMapping({
            "/web/mall/goods/goods/common/**",
            "/web/mall/goods/goods/cat/**",
            "/web/mall/goods/goods/userFootprint/**",
            "/web/mall/goods/goods/base/**",
            "/web/mall/goods/goods/brand/**",
            "/web/mall/goods/goods/evaluation/**",
            "/web/mall/goods/goods/goodsBase/**"
    })
    public ResponseEntity<byte[]> post(HttpServletRequest request) throws IOException {
        return sanitize(forwarder.forward(request, HttpMethod.POST));
    }

    @GetMapping({
            "/web/mall/goods/goods/common/**",
            "/web/mall/goods/goods/cat/**",
            "/web/mall/goods/goods/userFootprint/**",
            "/web/mall/goods/goods/base/**",
            "/web/mall/goods/goods/brand/**",
            "/web/mall/goods/goods/evaluation/**",
            "/web/mall/goods/goods/goodsBase/**"
    })
    public ResponseEntity<byte[]> get(HttpServletRequest request) throws IOException {
        return sanitize(forwarder.forward(request, HttpMethod.GET));
    }

    private static ResponseEntity<byte[]> sanitize(ResponseEntity<byte[]> forwarded) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(forwarded.getHeaders());
        headers.remove(HttpHeaders.TRANSFER_ENCODING);
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        headers.remove(HttpHeaders.CONNECTION);
        return new ResponseEntity<>(forwarded.getBody(), headers, forwarded.getStatusCode());
    }
}
