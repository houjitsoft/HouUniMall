package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.dto.PlatFormGoodsCatsDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品分类管理后台 Controller
 *
 * @description 提供商品分类管理后台 CRUD 接口，路径 /goods/category
 *              内部委托给 GoodsCatService
 * @author Amos
 */
@RestController
@RequestMapping("/goods/category")
public class GoodsCategoryManageController extends BaseController {

    /**
     * 查询商品分类列表
     *
     * @param param 查询参数
     * @return ResponseResult 分类列表数据
     */
    @PostMapping("/query")
    public ResponseResult queryCategoryList(@RequestBody Map<String, Object> param) {
        try {
            return goodsCatService.queryAll();
        } catch (Exception e) {
            return ResponseResult.error("查询分类列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取分类树形结构
     *
     * @return ResponseResult 分类树形数据
     */
    @PostMapping("/tree")
    public ResponseResult getCategoryTree() {
        try {
            return goodsCatService.queryAll();
        } catch (Exception e) {
            return ResponseResult.error("获取分类树形结构失败：" + e.getMessage());
        }
    }

    /**
     * 新增商品分类
     *
     * @param param 分类数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/add")
    public ResponseResult addCategory(@RequestBody Map<String, Object> param) {
        try {
            PlatFormGoodsCatsDTO dto = buildPlatFormGoodsCatsDTO(param);
            return goodsCatService.addGoodsCat(dto);
        } catch (Exception e) {
            return ResponseResult.error("新增分类失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品分类
     *
     * @param param 分类数据（包含分类ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/update")
    public ResponseResult updateCategory(@RequestBody Map<String, Object> param) {
        try {
            // 分类更新复用添加接口（底层判断是否有catId决定新增还是更新）
            PlatFormGoodsCatsDTO dto = buildPlatFormGoodsCatsDTO(param);
            return goodsCatService.addGoodsCat(dto);
        } catch (Exception e) {
            return ResponseResult.error("更新分类失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品分类
     *
     * @param param 删除参数（包含分类ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/delete")
    public ResponseResult deleteCategory(@RequestBody Map<String, Object> param) {
        try {
            PlatFormGoodsCatsDTO dto = buildPlatFormGoodsCatsDTO(param);
            return goodsCatService.deleteGoodsCat(dto);
        } catch (Exception e) {
            return ResponseResult.error("删除分类失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品分类分布（统计用）
     *
     * @return ResponseResult 分类分布数据
     */
    @PostMapping("/distribution")
    public ResponseResult getCategoryDistribution() {
        try {
            return goodsCatService.queryAll();
        } catch (Exception e) {
            return ResponseResult.error("获取分类分布失败：" + e.getMessage());
        }
    }

    /**
     * 将 Map 参数转换为 PlatFormGoodsCatsDTO
     *
     * @param param 包含分类字段的 Map
     * @return PlatFormGoodsCatsDTO
     */
    private PlatFormGoodsCatsDTO buildPlatFormGoodsCatsDTO(Map<String, Object> param) {
        PlatFormGoodsCatsDTO dto = new PlatFormGoodsCatsDTO();
        if (param.get("catId") != null) {
            dto.setCatId(Integer.valueOf(param.get("catId").toString()));
        }
        if (param.get("id") != null) {
            dto.setCatId(Integer.valueOf(param.get("id").toString()));
        }
        if (param.get("catName") != null) {
            dto.setCatName(param.get("catName").toString());
        }
        if (param.get("catParentId") != null) {
            dto.setCatParentId(Integer.valueOf(param.get("catParentId").toString()));
        }
        if (param.get("catSort") != null) {
            dto.setCatDisplayorder(Integer.valueOf(param.get("catSort").toString()));
        }
        if (param.get("catDisplayorder") != null) {
            dto.setCatDisplayorder(Integer.valueOf(param.get("catDisplayorder").toString()));
        }
        if (param.get("catIsShow") != null) {
            dto.setCatShowStatus(Integer.valueOf(param.get("catIsShow").toString()));
        }
        if (param.get("catShowStatus") != null) {
            dto.setCatShowStatus(Integer.valueOf(param.get("catShowStatus").toString()));
        }
        if (param.get("catPic") != null) {
            dto.setCatPic(param.get("catPic").toString());
        }
        return dto;
    }
}
