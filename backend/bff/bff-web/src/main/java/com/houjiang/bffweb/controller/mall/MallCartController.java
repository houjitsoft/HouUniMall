package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.OrderServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mall商城购物车接口控制器
/**
 * 面向PC前台用户的购物车相关接口
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/cart")
public class MallCartController {
    
    @Autowired
    private OrderServiceFeign orderServiceFeign;
    
    /**
     * 获取购物车列表（下游 queryShoppingCart 要求请求体带 token）
     */
    @RequestMapping(value = {"/list", "/lists"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getCartList(@RequestBody(required = false) Map<String, Object> param,
                                      HttpServletRequest request) {
        return orderServiceFeign.queryCartList(MallTokenSupport.withToken(param, request));
    }
    
    /**
     * 添加到购物车（下游 insertShoppingCart 需要 goodsId/goodsNum，
     * 前端 like-shop 风格传 goods_sku_id/num，这里做字段映射）
     */
    @RequestMapping(value = {"/add"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult addToCart(@RequestBody Map<String, Object> param,
                                    HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "goodsId", "goods_sku_id");
        MallTokenSupport.putIfAbsentFrom(body, "goodsNum", "num");
        return orderServiceFeign.addToCart(body);
    }
    
    /**
     * 更新购物车数量（下游 changeGoodsNum 按 goodsId+goodsNum 更新，
     * 前端传 id/num，这里做字段映射）
     */
    @PostMapping({"/update", "/change"})
    public ResponseResult updateCart(@RequestBody Map<String, Object> param,
                                     HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "goodsId", "id");
        MallTokenSupport.putIfAbsentFrom(body, "goodsNum", "num");
        return orderServiceFeign.updateCart(body);
    }
    
    /**
     * 删除购物车商品（下游 batchDeleteGoods 期望 goodsId 为商品ID列表，
     * 前端传 ids，这里做字段映射）
     */
    @PostMapping({"/remove", "/delete"})
    public ResponseResult removeFromCart(@RequestBody Map<String, Object> param,
                                         HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "goodsId", "ids");
        return orderServiceFeign.removeFromCart(body);
    }

    /**
     * 勾选购物车商品（下游无对应接口，返回成功兼容前端交互）
     */
    @PostMapping("/select")
    public ResponseResult selectCart(@RequestBody(required = false) Map<String, Object> param) {
        return ResponseResult.success(new HashMap<>());
    }

    /**
     * 切换购物车商品规格（下游无对应接口，返回成功兼容前端交互）
     */
    @PostMapping("/changeSku")
    public ResponseResult changeCartSku(@RequestBody(required = false) Map<String, Object> param) {
        return ResponseResult.success(new HashMap<>());
    }

    /**
     * 获取购物车商品数量（角标显示）
/**
     * 未登录或下游异常时安全返回 {num: 0}，不阻断前端页面加载
     */
    @RequestMapping(value = "/count", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getCartCount(@RequestBody(required = false) Map<String, Object> param,
                                       HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("num", 0);
        try {
            ResponseResult listResult = orderServiceFeign.queryCartList(MallTokenSupport.withToken(param, request));
            if (listResult != null && listResult.getCode() == 1 && listResult.getData() != null) {
                int count = countCartItems(listResult.getData());
                result.put("num", count);
            }
        } catch (Exception e) {
            log.warn("获取购物车数量异常，返回 0: {}", e.getMessage());
        }
        return ResponseResult.success(result);
    }

    /**
     * 从购物车查询结果中推算商品总数，兼容 List / 带 items / records / list 字段的 Map
     */
    private int countCartItems(Object data) {
        if (data instanceof List<?> list) {
            return list.size();
        }
        if (data instanceof Map<?, ?> map) {
            for (String key : new String[]{"items", "records", "list", "data"}) {
                Object inner = map.get(key);
                if (inner instanceof List<?> innerList) {
                    return innerList.size();
                }
            }
            Object total = map.get("total");
            if (total instanceof Number n) {
                return n.intValue();
            }
        }
        return 0;
    }
}
