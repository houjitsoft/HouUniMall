package com.houjiang.auth.feign;

import com.houjiang.commons.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MerchantServiceClient 降级
 */
@Slf4j
@Component
public class MerchantServiceClientFallback implements MerchantServiceClient {

    @Override
    public ResponseResult queryGateShopIdByUserId(Map<String, String> map) {
        log.warn("merchant 服务不可用，返回空结果（用户不是商家）");
        return ResponseResult.success((Object) null);
    }

    @Override
    public ResponseResult queryShopStatus(Map<String, String> map) {
        log.warn("merchant 服务不可用，返回空结果");
        return ResponseResult.success((Object) null);
    }

    @Override
    public ResponseResult delShopInfo(Map<String, String> map) {
        log.warn("merchant 服务不可用，删除店铺信息失败");
        return ResponseResult.fail("merchant 服务暂时不可用");
    }
}
