package com.houjiang.goods.mall.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.mall.dto.MallGoodsAuditDTO;
import com.houjiang.goods.mall.dto.MallGoodsSearchDTO;
import com.houjiang.goods.mall.dto.MallGoodsUpdateDTO;
import com.houjiang.goods.mall.service.MallGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 商品后台 Controller
 * 内部接口路径前缀：/mall/goods
 * @author Amos
 */
@RestController
@RequestMapping("/mall/goods")
public class MallGoodsController {

    @Autowired
    private MallGoodsService service;

    @GetMapping("/saleable-sku")
    public com.houjiang.commons.dto.mall.MallSkuSnapshot saleableSku(
            @RequestParam("skuId") Long skuId) {
        return service.requireSaleableSku(skuId);
    }

    /** 商品列表 */
    @PostMapping("/lists")
    public ResponseResult lists(@RequestBody MallGoodsSearchDTO dto) {
        return ResponseResult.success(service.list(dto));
    }

    /** 商品详情（可选 lang 覆盖多语言展示字段） */
    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam("id") Integer id,
                                 @RequestParam(value = "lang", required = false) String lang) {
        return ResponseResult.success(service.detail(id, lang));
    }

    /** 商品编辑（含新增） */
    @PostMapping("/set")
    public ResponseResult set(@RequestBody MallGoodsUpdateDTO dto) {
        return ResponseResult.success(service.set(dto));
    }

    /** 商品审核 */
    @PostMapping("/audit")
    public ResponseResult audit(@RequestBody MallGoodsAuditDTO dto) {
        service.audit(dto);
        return ResponseResult.success();
    }

    /** 商品重审 */
    @PostMapping("/re_audit")
    public ResponseResult reAudit(@RequestBody MallGoodsAuditDTO dto) {
        service.reAudit(dto);
        return ResponseResult.success();
    }

    /** 商品删除 */
    @PostMapping("/del")
    public ResponseResult del(@RequestBody Map<String, Object> param) {
        service.del(toInt(param.get("id")));
        return ResponseResult.success();
    }

    /** 上下架 */
    @PostMapping("/change_status")
    public ResponseResult changeStatus(@RequestBody Map<String, Object> param) {
        service.changeStatus(toInt(param.get("id")), toInt(param.get("status")));
        return ResponseResult.success();
    }

    /** 设为店内推荐 */
    @PostMapping("/change_recommend")
    public ResponseResult changeRecommend(@RequestBody Map<String, Object> param) {
        service.changeRecommend(toInt(param.get("id")), toInt(param.get("shopRecommend")));
        return ResponseResult.success();
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        return v instanceof Number ? ((Number) v).intValue() : Integer.valueOf(v.toString());
    }
}
