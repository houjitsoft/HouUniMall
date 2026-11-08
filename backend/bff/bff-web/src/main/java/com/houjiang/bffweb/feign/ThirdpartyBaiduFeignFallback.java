package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 百度翻译 Feign 降级。
 */
@Component
public class ThirdpartyBaiduFeignFallback implements ThirdpartyBaiduFeign {

    @Override
    public ResponseResult trans(Map<String, String> body) {
        return ResponseResult.error("翻译服务暂不可用");
    }

    @Override
    public ResponseResult queryLanguageAll() {
        return ResponseResult.error("语言列表暂不可用");
    }
}
