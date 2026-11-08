package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.service.GoodsAttributeTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 属性类型管理Controller
 * 
 * @author Amos
 * @date 2026-01-08
 */
@RestController
@RequestMapping("/goods/attributetype")
public class GoodsAttributeTypeController {

    @Autowired
    private GoodsAttributeTypeService attributeTypeService;

    /**
     * 分页查询属性类型列表
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/page")
    public ResponseResult getPage(@RequestBody Map<String, Object> param) {
        try {
            return attributeTypeService.queryPage(param);
        } catch (Exception e) {
            return ResponseResult.error("查询属性类型列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有属性类型列表（不分页）
     *
     * @return ResponseResult
     */
    @PostMapping("/list")
    public ResponseResult getList() {
        try {
            return attributeTypeService.queryList();
        } catch (Exception e) {
            return ResponseResult.error("查询所有属性类型失败：" + e.getMessage());
        }
    }

    /**
     * 创建属性类型
     *
     * @param param 属性类型数据
     * @return ResponseResult
     */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody Map<String, Object> param) {
        try {
            return attributeTypeService.create(param);
        } catch (Exception e) {
            return ResponseResult.error("创建属性类型失败：" + e.getMessage());
        }
    }

    /**
     * 更新属性类型
     *
     * @param param 属性类型数据
     * @return ResponseResult
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Map<String, Object> param) {
        try {
            return attributeTypeService.update(param);
        } catch (Exception e) {
            return ResponseResult.error("更新属性类型失败：" + e.getMessage());
        }
    }

    /**
     * 删除属性类型
     *
     * @param param 删除参数
     * @return ResponseResult
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> param) {
        try {
            return attributeTypeService.delete(param);
        } catch (Exception e) {
            return ResponseResult.error("删除属性类型失败：" + e.getMessage());
        }
    }
}
