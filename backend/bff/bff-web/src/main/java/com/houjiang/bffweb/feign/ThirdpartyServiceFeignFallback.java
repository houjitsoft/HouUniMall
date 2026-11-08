package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 第三方服务Feign客户端降级处理
/**
 * 
 * @author Amos
 * @since 2024-01-01
 */
@Component
public class ThirdpartyServiceFeignFallback implements ThirdpartyServiceFeign {

    @Override
    public ResponseResult sendCode(Map<String, String> paraMap) {
        return ResponseResult.error("发送验证码失败");
    }

    @Override
    public ResponseResult deliveryDetails(Map<String, String> paraMap) {
        return ResponseResult.error("查询短信回执失败：短信服务暂不可用");
    }
}