package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall商城积分商城接口控制器
/**
 * 面向PC前台用户的积分商城相关接口
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/integral")
public class MallIntegralController {
    
    @Autowired
    private MallServiceFeign mallServiceFeign;
    
    /**
     * 获取积分商品列表
     */
    @PostMapping("/list")
    public ResponseResult getIntegralGoodsList(@RequestBody Map<String, Object> param) {
        // 从推荐商品中筛选积分商品
        // 积分商品类型
        param.put("type", 5);
        return mallServiceFeign.getRecommendGoodsPage(param);
    }
    
    /**
     * 获取积分商品详情
     */
    @PostMapping("/detail")
    public ResponseResult getIntegralGoodsDetail(@RequestBody Map<String, Object> param) {
        Long id = param.get("id") != null ? Long.parseLong(param.get("id").toString()) : null;
        if (id == null) {
            return ResponseResult.error("积分商品ID不能为空");
        }
        return mallServiceFeign.getRecommendGoodsById(id);
    }
    
    /**
     * 积分兑换
     */
    @PostMapping("/exchange")
    public ResponseResult exchangeIntegralGoods(@RequestBody Map<String, Object> param) {
        // 积分兑换逻辑，需要扣减用户积分并创建兑换记录
        // TODO: 调用用户服务扣减积分，创建兑换订单
        return ResponseResult.success("兑换成功");
    }
}
