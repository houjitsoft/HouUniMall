package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 商城后台 - 商品管理（hou_mall_goods_*）
/**
 * 前端访问路径：/web/mall/admin/goods/*
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/admin/goods")
public class MallAdminGoodsController {

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    /** 商品列表 */
    @PostMapping("/lists")
    public ResponseResult lists(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallGoodsList(param);
    }

    /** 商品详情 */
    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam("id") Integer id,
                                 @RequestParam(value = "lang", required = false) String lang) {
        return goodsServiceFeign.mallGoodsDetail(id, lang);
    }

    /** 商品编辑（含新增） */
    @PostMapping("/set")
    public ResponseResult set(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallGoodsSet(param);
    }

    /** 商品审核 */
    @PostMapping("/audit")
    public ResponseResult audit(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallGoodsAudit(param);
    }

    /** 商品重审 */
    @PostMapping("/re_audit")
    public ResponseResult reAudit(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallGoodsReAudit(param);
    }

    /** 商品删除 */
    @PostMapping("/del")
    public ResponseResult del(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallGoodsDel(param);
    }

    /** 上下架 */
    @PostMapping("/change_status")
    public ResponseResult changeStatus(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallGoodsChangeStatus(param);
    }

    /** 店内推荐 */
    @PostMapping("/change_recommend")
    public ResponseResult changeRecommend(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallGoodsChangeRecommend(param);
    }
}
