package com.houjiang.goods.mall.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.mall.dto.MallGoodsCategoryDTO;
import com.houjiang.goods.mall.service.MallGoodsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 商品分类 Controller
 * @author Amos
 */
@RestController
@RequestMapping("/mall/goods/category")
public class MallGoodsCategoryController {

    @Autowired
    private MallGoodsCategoryService service;

    /** 分类列表（树） */
    @GetMapping("/lists")
    public ResponseResult lists(@RequestParam(value = "name", required = false) String name) {
        return ResponseResult.success(service.list(name));
    }

    /** 平铺列表（下拉） */
    @GetMapping("/all")
    public ResponseResult all() {
        return ResponseResult.success(service.otherList());
    }

    /** 分类详情 */
    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam("id") Integer id) {
        return ResponseResult.success(service.detail(id));
    }

    /** 新增 */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody MallGoodsCategoryDTO dto) {
        return ResponseResult.success(service.add(dto));
    }

    /** 编辑 */
    @PostMapping("/edit")
    public ResponseResult edit(@RequestBody MallGoodsCategoryDTO dto) {
        service.edit(dto);
        return ResponseResult.success();
    }

    /** 删除 */
    @PostMapping("/del")
    public ResponseResult del(@RequestBody Map<String, Object> param) {
        Object id = param.get("id");
        service.del(id instanceof Number ? ((Number) id).intValue() : Integer.valueOf(id.toString()));
        return ResponseResult.success();
    }

    /** 切换显示 */
    @PostMapping("/status")
    public ResponseResult status(@RequestBody Map<String, Object> param) {
        Object id = param.get("id");
        service.changeStatus(id instanceof Number ? ((Number) id).intValue() : Integer.valueOf(id.toString()));
        return ResponseResult.success();
    }
}
