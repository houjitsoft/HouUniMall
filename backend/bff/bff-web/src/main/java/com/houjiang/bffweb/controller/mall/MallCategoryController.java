package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall商城分类接口控制器
/**
 * 面向PC前台用户的分类相关接口
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/category")
public class MallCategoryController {
    
    @Autowired
    private GoodsServiceFeign goodsServiceFeign;
    
    /**
     * 获取分类列表
     */
    @PostMapping("/list")
    public ResponseResult getCategoryList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryCategoryList(param);
    }
    
    /**
     * 获取分类树
     */
    @PostMapping("/tree")
    public ResponseResult getCategoryTree(@RequestBody Map<String, Object> param) {
        param.put("tree", true);
        return goodsServiceFeign.queryCategoryList(param);
    }
}
