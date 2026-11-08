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

/** Exact consumer lookup routes owned by the base service. */
@RestController
public class UcenterBaseRouteController {
    private final UcenterServiceForwarder forwarder;

    @Autowired
    public UcenterBaseRouteController(@Value("${feign.base.url:}") String configuredUrl,
                                      DiscoveryClient discovery) {
        this(configuredUrl, discovery, UcenterServiceForwarder.httpClient());
    }

    UcenterBaseRouteController(String configuredUrl, DiscoveryClient discovery, RestTemplate http) {
        this.forwarder = new UcenterServiceForwarder("base", "/web/base", configuredUrl, discovery, http);
    }

    @PostMapping({"/web/base/base/district/all/district/query", "/web/base/base/express/query/noToken"})
    public ResponseEntity<byte[]> post(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.POST));
    }

    @GetMapping("/web/base/base/district/query")
    public ResponseEntity<byte[]> get(HttpServletRequest request) throws IOException {
        return UcenterServiceForwarder.sanitize(forwarder.forward(request, HttpMethod.GET));
    }
}
