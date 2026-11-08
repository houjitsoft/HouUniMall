package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.service.GoodsBrandTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 品牌类型管理Controller
 * 
 * @author Amos
 * @date 2026-01-08
 */
@RestController
@RequestMapping("/goods/brandtype")
public class GoodsBrandTypeController {

    @Autowired
    private GoodsBrandTypeService brandTypeService;

    /**
     * 查询品牌类型列表（分页）
     *
     * @param param 查询参数
     * @return ResponseResult
     */
    @PostMapping("/list")
    public ResponseResult getList(@RequestBody Map<String, Object> param) {
        try {
            return brandTypeService.queryList(param);
        } catch (Exception e) {
            return ResponseResult.error("查询品牌类型列表失败：" + e.getMessage());
        }
    }

    /**
     * 创建品牌类型
     *
     * @param param 品牌类型数据
     * @return ResponseResult
     */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody Map<String, Object> param) {
        try {
            return brandTypeService.create(param);
        } catch (Exception e) {
            return ResponseResult.error("创建品牌类型失败：" + e.getMessage());
        }
    }

    /**
     * 更新品牌类型
     *
     * @param param 品牌类型数据
     * @return ResponseResult
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Map<String, Object> param) {
        try {
            return brandTypeService.update(param);
        } catch (Exception e) {
            return ResponseResult.error("更新品牌类型失败：" + e.getMessage());
        }
    }

    /**
     * 删除品牌类型
     *
     * @param param 删除参数
     * @return ResponseResult
     */
    @PostMapping("/delete")
    public ResponseResult delete(@RequestBody Map<String, Object> param) {
        try {
            return brandTypeService.delete(param);
        } catch (Exception e) {
            return ResponseResult.error("删除品牌类型失败：" + e.getMessage());
        }
    }
}
