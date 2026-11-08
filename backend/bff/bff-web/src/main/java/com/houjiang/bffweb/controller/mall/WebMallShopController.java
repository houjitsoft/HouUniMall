package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall商城前台店铺接口控制器
 *
 * <p>店铺信息走 mall 服务 /mall/front/shop/**（匿名白名单），
 * 店铺商品复用 goods 服务商品列表（按 shopId 过滤）。
 *
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/shop")
public class WebMallShopController {

    @Autowired
    private MallServiceFeign mallServiceFeign;

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    /**
     * 解析分页参数，防止前端未传参导致NPE
     */
    private Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 店铺分页列表（兼容 page_no/page_size 与 keyword 参数）
     */
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult list(@RequestParam(required = false) Map<String, Object> query) {
        Map<String, Object> params = query == null ? Map.of() : query;
        Map<String, Object> body = new HashMap<>();
        body.put("page", toInt(params.get("page_no") != null ? params.get("page_no") : params.get("page"), 1));
        body.put("limit", toInt(params.get("page_size") != null ? params.get("page_size") : params.get("limit"), 10));
        if (params.get("keyword") != null) {
            body.put("keyword", params.get("keyword"));
        }
        return mallServiceFeign.frontShopList(body);
    }

    /**
     * 店铺详情
     */
    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam(value = "id", required = false) Long id) {
        if (id == null) {
            return ResponseResult.fail("店铺ID不能为空");
        }
        return mallServiceFeign.frontShopDetail(id);
    }

    /**
     * 店铺商品经营分类
     */
    @GetMapping("/category")
    public ResponseResult category(@RequestParam(value = "id", required = false) Long id) {
        if (id == null) {
            return ResponseResult.fail("店铺ID不能为空");
        }
        return mallServiceFeign.frontShopCategory(id);
    }

    /**
     * 店铺商品列表（复用前台商品列表接口，按店铺过滤）
     */
    @RequestMapping(value = "/goods", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult goods(
            @RequestParam(required = false) Map<String, Object> query,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> params = new HashMap<>();
        if (query != null) {
            params.putAll(query);
        }
        if (body != null) {
            params.putAll(body);
        }
        // 归一化参数到 goods 服务 MallGoodsSearchDTO 字段
        if (params.containsKey("page_no") && !params.containsKey("page")) {
            params.put("page", params.get("page_no"));
        }
        if (params.containsKey("page_size") && !params.containsKey("limit")) {
            params.put("limit", params.get("page_size"));
        }
        if (params.containsKey("category_id") && !params.containsKey("cid")) {
            params.put("cid", params.get("category_id"));
        }
        if (params.containsKey("shop_id") && !params.containsKey("shopId")) {
            params.put("shopId", params.get("shop_id"));
        }
        if (params.containsKey("commonName") && !params.containsKey("name")) {
            params.put("name", params.get("commonName"));
        }
        if (params.containsKey("shopCatId") && !params.containsKey("cid")) {
            params.put("cid", params.get("shopCatId"));
        }
        return goodsServiceFeign.mallGoodsList(params);
    }
}
