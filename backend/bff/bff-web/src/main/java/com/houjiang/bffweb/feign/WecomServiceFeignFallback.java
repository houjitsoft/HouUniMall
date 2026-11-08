package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Amos
 */
@Component
public class WecomServiceFeignFallback implements WecomServiceFeign {

    @Override
    public ResponseResult syncAttendance(Map<String, Object> body) {
        return ResponseResult.fail("企业微信服务暂时不可用，请稍后重试");
    }
}
