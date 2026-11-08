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

/** Exact consumer SMS routes owned by the thirdparty service. */
@RestController
public class UcenterSmsRouteController {
    private final UcenterServiceForwarder forwarder;

    @Autowired
    public UcenterSmsRouteController(@Value("${feign.thirdparty.url:}") String configuredUrl,
                                     DiscoveryClient discovery) {
        this(configuredUrl, discovery, UcenterServiceForwarder.httpClient());
    }

    UcenterSmsRouteController(String configuredUrl, DiscoveryClient discovery, RestTemplate http) {
        this.forwarder = new UcenterServiceForwarder("thirdparty", "/web/thirdparty", configuredUrl, discovery, http);
    }

    @PostMapping({
            "/web/thirdparty/sms/verifiPhoneCode2",
            "/web/thirdparty/sms/sendPhoneCodeByToken",
            "/web/thirdparty/sms/checkoldphone",
            "/web/thirdparty/sms/checkNewphone"
    })
    public ResponseEntity<byte[]> post(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.POST));
    }
}
