package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.service.GoodsAttributeGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 属性组管理Controller
 * 
 * @author Amos
 * @date 2026-01-08
 */
@RestController
@RequestMapping("/goods/attributegroup")
public class GoodsAttributeGroupController {

    @Autowired
    private GoodsAttributeGroupService attributeGroupService;

    /**
     * 查询属性组列表
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/list")
    public ResponseResult getList(@RequestBody Map<String, Object> param) {
        try {
            return attributeGroupService.queryList(param);
        } catch (Exception e) {
            return ResponseResult.error("查询属性组列表失败：" + e.getMessage());
        }
    }

    /**
     * 创建属性组
     *
     * @param param 属性组数据
     * @return ResponseResult
     */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody Map<String, Object> param) {
        try {
            return attributeGroupService.create(param);
        } catch (Exception e) {
            return ResponseResult.error("创建属性组失败：" + e.getMessage());
        }
    }

    /**
     * 更新属性组
     *
     * @param param 属性组数据
     * @return ResponseResult
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Map<String, Object> param) {
        try {
            return attributeGroupService.update(param);
        } catch (Exception e) {
            return ResponseResult.error("更新属性组失败：" + e.getMessage());
        }
    }

    /**
     * 删除属性组
     *
     * @param param 删除参数
     * @return ResponseResult
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> param) {
        try {
            return attributeGroupService.delete(param);
        } catch (Exception e) {
            return ResponseResult.error("删除属性组失败：" + e.getMessage());
        }
    }
}
