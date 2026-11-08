package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall商城店铺接口控制器（旧路径兼容）
 * 面向PC前台用户的店铺相关接口，转发到 mall 服务 /mall/front/shop/**
 *
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/front/shop")
public class MallShopController {

    @Autowired
    private MallServiceFeign mallServiceFeign;

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    /**
     * 获取店铺列表（兼容 POST 请求体传参）
     */
    @PostMapping("/list")
    public ResponseResult getShopList(@RequestBody Map<String, Object> param) {
        Map<String, Object> body = new HashMap<>();
        if (param != null) {
            body.put("page", param.get("page_no") != null ? param.get("page_no") : param.get("page"));
            body.put("limit", param.get("page_size") != null ? param.get("page_size") : param.get("limit"));
            Object keyword = param.get("keyword") != null ? param.get("keyword") : param.get("shopName");
            body.put("keyword", keyword);
        }
        return mallServiceFeign.frontShopList(body);
    }

    /**
     * 获取店铺详情（兼容 POST 请求体传参）
     */
    @PostMapping("/detail")
    public ResponseResult getShopDetail(@RequestBody Map<String, Object> param) {
        Object id = param == null ? null : param.get("id");
        if (id == null) {
            return ResponseResult.fail("店铺ID不能为空");
        }
        return mallServiceFeign.frontShopDetail(Long.valueOf(id.toString()));
    }

    /**
     * 获取店铺商品（兼容 POST 请求体传参，按店铺过滤商品列表）
     */
    @PostMapping("/goods")
    public ResponseResult getShopGoods(@RequestBody Map<String, Object> param) {
        Map<String, Object> goodsParam = new HashMap<>();
        if (param != null) {
            goodsParam.putAll(param);
            Object id = param.get("id") != null ? param.get("id") : param.get("shop_id");
            if (id != null) {
                goodsParam.put("shopId", id);
            }
        }
        if (goodsParam.get("shopId") == null) {
            return ResponseResult.fail("店铺ID不能为空");
        }
        return goodsServiceFeign.mallGoodsList(goodsParam);
    }
}
