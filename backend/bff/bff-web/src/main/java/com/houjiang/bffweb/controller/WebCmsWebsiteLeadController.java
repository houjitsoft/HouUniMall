package com.houjiang.bffweb.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.feign.WebsiteLeadServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 官网留资：公开提交走 /web/cms/front/lead；后台列表需登录。
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/cms")
public class WebCmsWebsiteLeadController {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final WebsiteLeadServiceFeign websiteLeadServiceFeign;
    private final ObjectMapper objectMapper;

    public WebCmsWebsiteLeadController(WebsiteLeadServiceFeign websiteLeadServiceFeign, ObjectMapper objectMapper) {
        this.websiteLeadServiceFeign = websiteLeadServiceFeign;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/front/lead/submit")
    public ResponseResult submitLead(@RequestBody String raw) {
        Map<String, Object> body;
        try {
            body = objectMapper.readValue(raw == null || raw.isBlank() ? "{}" : raw, MAP_TYPE);
        } catch (Exception e) {
            return ResponseResult.fail(0, "提交内容格式不正确");
        }
        return websiteLeadServiceFeign.submitLead(body);
    }

    @PostMapping("/website/lead/page")
    public ResponseResult pageLeads(@RequestBody(required = false) Map<String, Object> params) {
        return websiteLeadServiceFeign.pageLeads(params == null ? Map.of() : params);
    }

    @PostMapping("/website/lead/status")
    public ResponseResult updateLeadStatus(@RequestBody Map<String, Object> params) {
        return websiteLeadServiceFeign.updateLeadStatus(params);
    }
}
