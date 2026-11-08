package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebsiteLeadServiceFeignFallback implements WebsiteLeadServiceFeign {

    @Override
    public ResponseResult submitLead(Map<String, Object> body) {
        return ResponseResult.error("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult pageLeads(Map<String, Object> params) {
        return ResponseResult.error("CMS服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateLeadStatus(Map<String, Object> params) {
        return ResponseResult.error("CMS服务暂时不可用，请稍后重试");
    }
}
