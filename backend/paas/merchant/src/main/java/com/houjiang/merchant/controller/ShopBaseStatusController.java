package com.houjiang.merchant.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.merchant.service.ShopBaseStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * C 端/PC 顶栏「商家入驻状态」查询。
 * <p>
 * 旧路径 mall-business/shopBase/info/shop/status/query 经 BFF legacy 落到本接口；
 * 不可再映射到 /merchant/internal（内部 Feign 入口会被商家后台拦截器当成未登录）。
 */
@RestController
@RequestMapping("/merchant/shopBase/info")
@RequiredArgsConstructor
public class ShopBaseStatusController {

    private final ShopBaseStatusService shopBaseStatusService;

    @PostMapping("/shop/status/query")
    public ResponseResult queryShopJoinStatus(@RequestBody(required = false) Map<String, Object> param) {
        return shopBaseStatusService.queryShopJoinStatus(param);
    }
}
