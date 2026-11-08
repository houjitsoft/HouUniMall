package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户收货地址服务 Feign 降级处理
 *
 * @author Amos
 */
@Component
public class UserAddressServiceFeignFallback implements UserAddressServiceFeign {

    @Override
    public ResponseResult getAddressList(Map<String, String> paraMap) {
        return ResponseResult.fail("地址服务暂时不可用");
    }

    @Override
    public ResponseResult getDefaultAddress(Map<String, String> paraMap) {
        return ResponseResult.fail("地址服务暂时不可用");
    }

    @Override
    public ResponseResult addAddress(Map<String, String> paraMap) {
        return ResponseResult.fail("地址服务暂时不可用");
    }

    @Override
    public ResponseResult updateAddress(Map<String, String> paraMap) {
        return ResponseResult.fail("地址服务暂时不可用");
    }

    @Override
    public ResponseResult deleteAddress(Map<String, String> paraMap) {
        return ResponseResult.fail("地址服务暂时不可用");
    }

    @Override
    public ResponseResult setDefaultAddress(Map<String, String> paraMap) {
        return ResponseResult.fail("地址服务暂时不可用");
    }
}
