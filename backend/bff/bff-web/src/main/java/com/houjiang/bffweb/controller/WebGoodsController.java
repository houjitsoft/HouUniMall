package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Web端商品管理BFF控制器（已废弃，功能已迁移至 MallGoodsController）
/**
 * 
 * @description 处理Web端的商品管理相关请求
 * @author Amos
 * @date 2024-12-20
 * @deprecated 已被 MallGoodsController 替代，路由冲突已解决
 */
@RestController
@RequestMapping("/web/goods")
public class WebGoodsController {

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    // ================= 商品管理接口 =================

    /**
     * 查询商品列表
/**
     *
     * @param queryParams 查询参数（支持分页、筛选、排序）
     * @return ResponseResult 商品列表数据
     */
    @PostMapping("/list")
    public ResponseResult getGoodsList(@RequestBody Map<String, Object> queryParams) {
        return goodsServiceFeign.queryGoodsList(queryParams);
    }

    /**
     * 查询商品详情
/**
     *
     * @param id 商品ID
     * @return ResponseResult 商品详情数据
     */
    @GetMapping("/{id}")
    public ResponseResult getGoodsDetail(@PathVariable Long id) {
        return goodsServiceFeign.getGoodsDetail(id);
    }

    /**
     * 创建商品
/**
     *
     * @param goodsData 商品数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/create")
    public ResponseResult createGoods(@RequestBody Map<String, Object> goodsData) {
        return goodsServiceFeign.createGoods(goodsData);
    }

    /**
     * 更新商品
/**
     *
     * @param goodsData 商品数据（包含商品ID�?
     * @return ResponseResult 更新结果
     */
    @PostMapping("/update")
    public ResponseResult updateGoods(@RequestBody Map<String, Object> goodsData) {
        return goodsServiceFeign.updateGoods(goodsData);
    }

    /**
     * 删除商品
/**
     *
     * @param deleteData 删除参数（包含商品ID�?
     * @return ResponseResult 删除结果
     */
    @PostMapping("/delete")
    public ResponseResult deleteGoods(@RequestBody Map<String, Object> deleteData) {
        return goodsServiceFeign.deleteGoods(deleteData);
    }

    /**
     * 更新商品状态（上下架）
/**
     *
     * @param statusData 状态更新参�?
     * @return ResponseResult 更新结果
     */
    @PostMapping("/status")
    public ResponseResult updateGoodsStatus(@RequestBody Map<String, Object> statusData) {
        return goodsServiceFeign.updateGoodsStatus(statusData);
    }

    /**
     * 更新商品库存
/**
     *
     * @param stockData 库存更新参数
     * @return ResponseResult 更新结果
     */
    @PostMapping("/stock")
    public ResponseResult updateGoodsStock(@RequestBody Map<String, Object> stockData) {
        return goodsServiceFeign.updateGoodsStock(stockData);
    }

    // ================= 商品分类管理接口 =================

    /**
     * 查询商品分类列表
/**
     *
     * @param queryParams 查询参数
     * @return ResponseResult 分类列表数据
     */
    @PostMapping("/category/list")
    public ResponseResult getCategoryList(@RequestBody Map<String, Object> queryParams) {
        return goodsServiceFeign.queryCategoryList(queryParams);
    }

    /**
     * 获取分类树形结构
/**
     *
     * @return ResponseResult 分类树形数据
     */
    @PostMapping("/category/tree")
    public ResponseResult getCategoryTree() {
        return goodsServiceFeign.getCategoryTree();    }

    /**
     * 创建商品分类
/**
     *
     * @param categoryData 分类数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/category/create")
    public ResponseResult createCategory(@RequestBody Map<String, Object> categoryData) {
        return goodsServiceFeign.createCategory(categoryData);
    }

    /**
     * 更新商品分类
/**
     *
     * @param categoryData 分类数据（包含分类ID�?
     * @return ResponseResult 更新结果
     */
    @PostMapping("/category/update")
    public ResponseResult updateCategory(@RequestBody Map<String, Object> categoryData) {
        return goodsServiceFeign.updateCategory(categoryData);
    }

    /**
     * 删除商品分类
/**
     *
     * @param deleteData 删除参数（包含分类ID�?
     * @return ResponseResult 删除结果
     */
    @PostMapping("/category/delete")
    public ResponseResult deleteCategory(@RequestBody Map<String, Object> deleteData) {
        return goodsServiceFeign.deleteCategory(deleteData);
    }

    // ================= 商品品牌管理接口 =================

    /**
     * 查询商品品牌列表
/**
     *
     * @param queryParams 查询参数
     * @return ResponseResult 品牌列表数据
     */
    @PostMapping("/brand/list")
    public ResponseResult getBrandList(@RequestBody Map<String, Object> queryParams) {
        return goodsServiceFeign.queryBrandList(queryParams);
    }

    /**
     * 创建商品品牌
/**
     *
     * @param brandData 品牌数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/brand/create")
    public ResponseResult createBrand(@RequestBody Map<String, Object> brandData) {
        return goodsServiceFeign.createBrand(brandData);
    }

    /**
     * 更新商品品牌
/**
     *
     * @param brandData 品牌数据（包含品牌ID�?
     * @return ResponseResult 更新结果
     */
    @PostMapping("/brand/update")
    public ResponseResult updateBrand(@RequestBody Map<String, Object> brandData) {
        return goodsServiceFeign.updateBrand(brandData);
    }

    /**
     * 删除商品品牌
/**
     *
     * @param deleteData 删除参数（包含品牌ID�?
     * @return ResponseResult 删除结果
     */
    @PostMapping("/brand/delete")
    public ResponseResult deleteBrand(@RequestBody Map<String, Object> deleteData) {
        return goodsServiceFeign.deleteBrand(deleteData);
    }

    /**
     * 获取所有品牌列表（不分页）
/**
     *
     * @return ResponseResult 所有品牌列表
     */
    @PostMapping("/brand/all")
    public ResponseResult getAllBrands() {
        return goodsServiceFeign.queryAllBrands();
    }

    // ================= 品牌类型管理接口 =================

    /**
     * 查询品牌类型列表
/**
     *
     * @param queryParams 查询参数
     * @return ResponseResult 品牌类型列表数据
     */
    @PostMapping("/brand-type/list")
    public ResponseResult getBrandTypeList(@RequestBody Map<String, Object> queryParams) {
        return goodsServiceFeign.queryBrandTypeList(queryParams);
    }

    /**
     * 创建品牌类型
/**
     *
     * @param brandTypeData 品牌类型数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/brand-type/create")
    public ResponseResult createBrandType(@RequestBody Map<String, Object> brandTypeData) {
        return goodsServiceFeign.createBrandType(brandTypeData);
    }

    /**
     * 更新品牌类型
/**
     *
     * @param brandTypeData 品牌类型数据
     * @return ResponseResult 更新结果
     */
    @PostMapping("/brand-type/update")
    public ResponseResult updateBrandType(@RequestBody Map<String, Object> brandTypeData) {
        return goodsServiceFeign.updateBrandType(brandTypeData);
    }

    /**
     * 删除品牌类型
/**
     *
     * @param deleteData 删除参数
     * @return ResponseResult 删除结果
     */
    @PostMapping("/brand-type/delete")
    public ResponseResult deleteBrandType(@RequestBody Map<String, Object> deleteData) {
        return goodsServiceFeign.deleteBrandType(deleteData);
    }

    // ================= 属性类型管理接口 =================

    /**
     * 分页查询属性类型列表
/**
     *
     * @param queryParams 查询参数
     * @return ResponseResult 属性类型列表数据
     */
    @PostMapping("/attribute-type/page")
    public ResponseResult getAttributeTypePage(@RequestBody Map<String, Object> queryParams) {
        return goodsServiceFeign.queryAttributeTypePage(queryParams);
    }

    /**
     * 获取所有属性类型列表（不分页）
/**
     *
     * @return ResponseResult 所有属性类型列表
     */
    @PostMapping("/attribute-type/list")
    public ResponseResult getAttributeTypeList() {
        return goodsServiceFeign.queryAttributeTypeList();
    }

    /**
     * 创建属性类型
/**
     *
     * @param attributeTypeData 属性类型数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/attribute-type/create")
    public ResponseResult createAttributeType(@RequestBody Map<String, Object> attributeTypeData) {
        return goodsServiceFeign.createAttributeType(attributeTypeData);
    }

    /**
     * 更新属性类型
/**
     *
     * @param attributeTypeData 属性类型数据
     * @return ResponseResult 更新结果
     */
    @PostMapping("/attribute-type/update")
    public ResponseResult updateAttributeType(@RequestBody Map<String, Object> attributeTypeData) {
        return goodsServiceFeign.updateAttributeType(attributeTypeData);
    }

    /**
     * 删除属性类型
/**
     *
     * @param deleteData 删除参数
     * @return ResponseResult 删除结果
     */
    @PostMapping("/attribute-type/delete")
    public ResponseResult deleteAttributeType(@RequestBody Map<String, Object> deleteData) {
        return goodsServiceFeign.deleteAttributeType(deleteData);
    }

    // ================= 属性组管理接口 =================

    /**
     * 查询属性组列表
/**
     *
     * @param queryParams 查询参数
     * @return ResponseResult 属性组列表数据
     */
    @PostMapping("/attribute-group/list")
    public ResponseResult getAttributeGroupList(@RequestBody Map<String, Object> queryParams) {
        return goodsServiceFeign.queryAttributeGroupList(queryParams);
    }

    /**
     * 创建属性组
/**
     *
     * @param attributeGroupData 属性组数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/attribute-group/create")
    public ResponseResult createAttributeGroup(@RequestBody Map<String, Object> attributeGroupData) {
        return goodsServiceFeign.createAttributeGroup(attributeGroupData);
    }

    /**
     * 更新属性组
/**
     *
     * @param attributeGroupData 属性组数据
     * @return ResponseResult 更新结果
     */
    @PostMapping("/attribute-group/update")
    public ResponseResult updateAttributeGroup(@RequestBody Map<String, Object> attributeGroupData) {
        return goodsServiceFeign.updateAttributeGroup(attributeGroupData);
    }

    /**
     * 删除属性组
/**
     *
     * @param deleteData 删除参数
     * @return ResponseResult 删除结果
     */
    @PostMapping("/attribute-group/delete")
    public ResponseResult deleteAttributeGroup(@RequestBody Map<String, Object> deleteData) {
        return goodsServiceFeign.deleteAttributeGroup(deleteData);
    }

    // ================= 商品规格管理接口 =================

    /**
     * 查询商品规格列表
/**
     *
     * @param queryParams 查询参数
     * @return ResponseResult 规格列表数据
     */
    @PostMapping("/spec/list")
    public ResponseResult getSpecList(@RequestBody Map<String, Object> queryParams) {
        return goodsServiceFeign.querySpecList(queryParams);
    }

    /**
     * 创建商品规格
/**
     *
     * @param specData 规格数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/spec/create")
    public ResponseResult createSpec(@RequestBody Map<String, Object> specData) {
        return goodsServiceFeign.createSpec(specData);
    }

    /**
     * 更新商品规格
/**
     *
     * @param specData 规格数据（包含规格ID�?
     * @return ResponseResult 更新结果
     */
    @PostMapping("/spec/update")
    public ResponseResult updateSpec(@RequestBody Map<String, Object> specData) {
        return goodsServiceFeign.updateSpec(specData);
    }

    /**
     * 删除商品规格
/**
     *
     * @param deleteData 删除参数（包含规格ID�?
     * @return ResponseResult 删除结果
     */
    @PostMapping("/spec/delete")
    public ResponseResult deleteSpec(@RequestBody Map<String, Object> deleteData) {
        return goodsServiceFeign.deleteSpec(deleteData);
    }

    // ================= 商品属性管理接�?=================

    /**
     * 查询商品属性列�?
/**
     *
     * @param queryParams 查询参数
     * @return ResponseResult 属性列表数�?
     */
    @PostMapping("/attribute/list")
    public ResponseResult getAttributeList(@RequestBody Map<String, Object> queryParams) {
        return goodsServiceFeign.queryAttributeList(queryParams);
    }

    /**
     * 创建商品属�?
/**
     *
     * @param attributeData 属性数�?
     * @return ResponseResult 创建结果
     */
    @PostMapping("/attribute/create")
    public ResponseResult createAttribute(@RequestBody Map<String, Object> attributeData) {
        return goodsServiceFeign.createAttribute(attributeData);
    }

    /**
     * 更新商品属�?
/**
     *
     * @param attributeData 属性数据（包含属性ID�?
     * @return ResponseResult 更新结果
     */
    @PostMapping("/attribute/update")
    public ResponseResult updateAttribute(@RequestBody Map<String, Object> attributeData) {
        return goodsServiceFeign.updateAttribute(attributeData);
    }

    /**
     * 删除商品属�?
/**
     *
     * @param deleteData 删除参数（包含属性ID�?
     * @return ResponseResult 删除结果
     */
    @PostMapping("/attribute/delete")
    public ResponseResult deleteAttribute(@RequestBody Map<String, Object> deleteData) {
        return goodsServiceFeign.deleteAttribute(deleteData);
    }

    // ================= 商品统计接口 =================

    /**
     * 获取商品统计数据
/**
     *
     * @param param 统计参数
     * @return ResponseResult 统计数据
     */
    @PostMapping("/statistics")
    public ResponseResult getGoodsStatistics(@RequestBody(required = false) Map<String, Object> param) {
        return goodsServiceFeign.getGoodsStatistics();
    }

    /**
     * 获取商品概览数据
/**
     *
     * @return ResponseResult 概览数据
     */
    @PostMapping("/overview")
    public ResponseResult getGoodsOverview() {
        return goodsServiceFeign.getGoodsStatistics();
    }

    /**
     * 获取商品详情（POST方式）
/**
     *
     * @param param 包含商品ID的请求体
     * @return ResponseResult 商品详情数据
     */
    @PostMapping("/detail")
    public ResponseResult getGoodsDetailPost(@RequestBody Map<String, Object> param) {
        Object id = param.get("id");
        if (id == null) {
            return goodsServiceFeign.queryGoodsList(param);
        }
        return goodsServiceFeign.getGoodsDetail(Long.valueOf(id.toString()));
    }

    /**
     * 获取热销商品排行榜
/**
     *
     * @return ResponseResult 热销商品数据
     */
    @PostMapping("/top")
    public ResponseResult getTopGoods() {
        return goodsServiceFeign.getGoodsStatistics();
    }

    /**
     * 获取商品销量趋势
/**
     *
     * @return ResponseResult 销量趋势数据
     */
    @PostMapping("/sales/trend")
    public ResponseResult getSalesTrend() {
        return goodsServiceFeign.getGoodsStatistics();
    }
}