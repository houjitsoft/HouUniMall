package com.houjiang.mall.service.proxy;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.feign.GoodsPaasFeign;
import com.houjiang.mall.feign.GoodsPaasFeignFallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MallGoodsProxyService {

    private final GoodsPaasFeign goodsPaasFeign;
    private final GoodsPaasFeignFallback goodsPaasFeignFallback;

    public ResponseResult list(Map<String, Object> params) {
        try {
            return goodsPaasFeign.goodsLists(toSearchBody(params));
        } catch (Exception e) {
            log.warn("[MallGoodsProxy] 商品列表调用 goods 失败: {}", e.getMessage());
            return goodsPaasFeignFallback.goodsLists(toSearchBody(params));
        }
    }

    public ResponseResult categoryTree(Map<String, Object> params) {
        String name = null;
        if (params != null && params.get("name") != null) {
            name = String.valueOf(params.get("name"));
        }
        return goodsPaasFeign.categoryLists(name);
    }

    private Map<String, Object> toSearchBody(Map<String, Object> params) {
        Map<String, Object> body = new HashMap<>();
        if (params == null) {
            body.put("pageNo", 1);
            body.put("pageSize", 15);
            return body;
        }
        body.put("pageNo", firstInt(params, 1, "page", "pageNo"));
        body.put("pageSize", firstInt(params, 15, "size", "pageSize"));
        copyIfPresent(params, body, "name");
        copyIfPresent(params, body, "shopName", "shop_name");
        copyIfPresent(params, body, "categoryId", "category_id");
        copyIfPresent(params, body, "type");
        copyIfPresent(params, body, "status");
        copyIfPresent(params, body, "code");
        copyIfPresent(params, body, "isDistribution");
        copyIfPresent(params, body, "shopId", "shop_id");
        return body;
    }

    private static void copyIfPresent(Map<String, Object> from, Map<String, Object> to, String targetKey, String... sourceKeys) {
        for (String key : sourceKeys) {
            Object v = from.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                to.put(targetKey, v);
                return;
            }
        }
    }

    private static int firstInt(Map<String, Object> params, int defaultValue, String... keys) {
        for (String key : keys) {
            Object v = params.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return Integer.parseInt(String.valueOf(v));
            }
        }
        return defaultValue;
    }
}
