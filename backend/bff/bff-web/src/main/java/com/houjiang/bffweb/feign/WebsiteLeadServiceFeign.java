package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "cms", url = "${feign.cms.url:}", path = "/plugin/cms", fallback = WebsiteLeadServiceFeignFallback.class, contextId = "cmsWebsiteLead")
public interface WebsiteLeadServiceFeign {

    @PostMapping("/front/lead/submit")
    ResponseResult submitLead(@RequestBody Map<String, Object> body);

    @PostMapping("/website/lead/page")
    ResponseResult pageLeads(@RequestBody Map<String, Object> params);

    @PostMapping("/website/lead/status")
    ResponseResult updateLeadStatus(@RequestBody Map<String, Object> params);
}
