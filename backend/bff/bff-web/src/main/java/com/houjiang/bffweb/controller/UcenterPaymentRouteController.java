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

/** Consumer wallet routes owned by the payment service. */
@RestController
public class UcenterPaymentRouteController {
    private final UcenterServiceForwarder forwarder;

    @Autowired
    public UcenterPaymentRouteController(@Value("${feign.payment.url:}") String configuredUrl,
                                         DiscoveryClient discovery) {
        this(configuredUrl, discovery, UcenterServiceForwarder.httpClient());
    }

    UcenterPaymentRouteController(String configuredUrl, DiscoveryClient discovery, RestTemplate http) {
        this.forwarder = new UcenterServiceForwarder("payment", "/web/pay", configuredUrl, discovery, http);
    }

    @PostMapping({
            "/web/pay/payment/userResource/balanceDisplay",
            "/web/pay/pay/assetDetails/queryList",
            "/web/pay/pay/assetDetails/queryDetail",
            "/web/pay/pay/cardInfo/rechargeCard",
            "/web/pay/pay/entrance/userPay",
            // 收银台：进入支付页先校验支付密码、支付后轮询渠道状态，缺任一条支付链路断在最后一步
            "/web/pay/pay/userBase/checkPayPassword",
            "/web/pay/pay/WXPay/queryWxPayStatus",
            "/web/pay/pay/WXPay/queryWxPayBalanceRecharge",
            "/web/pay/pay/userBase/setPaymentPassword"
    })
    public ResponseEntity<byte[]> post(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.POST));
    }
}
