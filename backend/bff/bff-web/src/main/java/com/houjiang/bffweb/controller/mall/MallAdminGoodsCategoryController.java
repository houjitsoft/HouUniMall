package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 商城后台 - 商品分类
/**
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/admin/goods/category")
public class MallAdminGoodsCategoryController {

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    /** 分类树（tree 路径，前端 /web/mall/admin/goods/category/tree） */
    @GetMapping("/tree")
    public ResponseResult tree() {
        return goodsServiceFeign.getCategoryTree();
    }

    /** 分类树 */
    @GetMapping("/lists")
    public ResponseResult lists(@RequestParam(value = "name", required = false) String name) {
        return goodsServiceFeign.mallCategoryList(name);
    }

    /** 平铺列表 */
    @GetMapping("/all")
    public ResponseResult all() {
        return goodsServiceFeign.mallCategoryAll();
    }

    /** 详情 */
    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam("id") Integer id) {
        return goodsServiceFeign.mallCategoryDetail(id);
    }

    /** 新增 */
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallCategoryAdd(param);
    }

    /** 编辑 */
    @PostMapping("/edit")
    public ResponseResult edit(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallCategoryEdit(param);
    }

    /** 删除 */
    @PostMapping("/del")
    public ResponseResult del(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallCategoryDel(param);
    }

    /** 显示状态切换 */
    @PostMapping("/status")
    public ResponseResult status(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.mallCategoryStatus(param);
    }
}
