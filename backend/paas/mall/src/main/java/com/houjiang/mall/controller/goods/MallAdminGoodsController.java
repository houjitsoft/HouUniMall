package com.houjiang.mall.controller.goods;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.auth.MallAdminTokenService;
import com.houjiang.mall.service.proxy.MallGoodsProxyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 平台后台 — 商品（BFF /web/mall/goods/* → mall 鉴权 → goods /mall/goods/*）
 * @author Amos
 */
@RestController
@RequestMapping("/mall/goods")
@RequiredArgsConstructor
public class MallAdminGoodsController {

    private static final String GOODS_READ =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','mall:goods','shell:mall:goods')";

    private final MallAdminTokenService mallAdminTokenService;
    private final MallGoodsProxyService mallGoodsProxyService;

    /**
     * 与 {@code MallAdminTokenAuthenticationFilter} 保持一致：商城管理员 token 与
     * Cloud 内嵌商城的云后台 token 均视为已登录。
     */
    private ResponseResult requireLogin(String token) {
        if (!mallAdminTokenService.isAuthenticated(token)) {
            return ResponseResult.fail(401, "登录已过期");
        }
        return null;
    }

    @PostMapping("/list")
    @PreAuthorize(GOODS_READ)
    public ResponseResult list(@RequestHeader(value = "Authorization", required = false) String token,
                               @RequestBody(required = false) Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallGoodsProxyService.list(params);
    }

    @PostMapping("/category/tree")
    @PreAuthorize(GOODS_READ)
    public ResponseResult categoryTree(@RequestHeader(value = "Authorization", required = false) String token,
                                       @RequestBody(required = false) Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallGoodsProxyService.categoryTree(params);
    }
}
