package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品管理后台 Controller
 * 
 * @description 提供商品管理后台 CRUD 接口，路径 /goods/goods
 *              内部委托给 GoodsBaseService / GoodsCommonService
 * @author Amos
 */
@RestController
@RequestMapping("/goods/goods")
public class GoodsManageController extends BaseController {

    /**
     * 查询商品列表（管理后台分页查询）
     *
     * @param param 查询参数（支持分页、分类筛选、状态筛选）
     * @return ResponseResult 商品列表数据
     */
    @PostMapping("/query/list")
    public ResponseResult queryGoodsList(@RequestBody Map<String, Object> param) {
        try {
            // 使用支持 Map 入参的商品列表查询接口
            return goodsCommonService.queryGoodsListByPintuan(param);
        } catch (Exception e) {
            return ResponseResult.error("查询商品列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     *
     * @param param 包含商品 ID 的请求体
     * @return ResponseResult 商品详情数据
     */
    @PostMapping("/detail")
    public ResponseResult getGoodsDetail(@RequestBody Map<String, Object> param) {
        try {
            Integer commonId = param.get("id") != null ? Integer.valueOf(param.get("id").toString()) : null;
            if (commonId == null) {
                return ResponseResult.error("商品ID不能为空");
            }
            return goodsBaseService.getGoodsCommonDetails(commonId);
        } catch (Exception e) {
            return ResponseResult.error("获取商品详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品详情（通过 GET 方式，路径变量）
     *
     * @param id 商品 ID
     * @return ResponseResult 商品详情数据
     */
    @GetMapping("/{id}")
    public ResponseResult getGoodsDetailById(@PathVariable Long id) {
        try {
            return goodsBaseService.getGoodsCommonDetails(id.intValue());
        } catch (Exception e) {
            return ResponseResult.error("获取商品详情失败：" + e.getMessage());
        }
    }

    /**
     * 新增商品
     *
     * @param param 商品数据（Map形式，前端传入的完整商品数据）
     * @return ResponseResult 创建结果
     */
    @PostMapping("/add")
    public ResponseResult addGoods(@RequestBody Map<String, Object> param) {
        try {
            // 调用已有的管理后台商品创建接口
            return goodsBaseService.updateGoodsBaseByMap(param);
        } catch (Exception e) {
            return ResponseResult.error("新增商品失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品
     *
     * @param param 商品数据（包含商品ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/update")
    public ResponseResult updateGoods(@RequestBody Map<String, Object> param) {
        try {
            return goodsBaseService.updateGoodsBaseByMap(param);
        } catch (Exception e) {
            return ResponseResult.error("更新商品失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品
     *
     * @param param 删除参数（包含商品ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/delete")
    public ResponseResult deleteGoods(@RequestBody Map<String, Object> param) {
        try {
            return goodsCommonService.deleteByCommonId(
                buildManageCommonIdsDTO(param)
            );
        } catch (Exception e) {
            return ResponseResult.error("删除商品失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品状态（上下架）
     *
     * @param param 状态更新参数（包含商品ID和状态值）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/status")
    public ResponseResult updateGoodsStatus(@RequestBody Map<String, Object> param) {
        try {
            return goodsBaseService.updateGoodsBaseByMap(param);
        } catch (Exception e) {
            return ResponseResult.error("更新商品状态失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品库存
     *
     * @param param 库存更新参数（包含商品ID和库存数量）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/stock")
    public ResponseResult updateGoodsStock(@RequestBody Map<String, Object> param) {
        try {
            return goodsBaseService.updateGoodsBaseByMap(param);
        } catch (Exception e) {
            return ResponseResult.error("更新商品库存失败：" + e.getMessage());
        }
    }

    /**
     * 将 Map 参数转换为 ManageCommonIdsDTO（通过构建包含 commonId 的 Map 调用）
     *
     * @param param 包含 id 字段的 Map
     * @return ManageCommonIdsDTO
     */
    private com.houjiang.goods.dto.ManageCommonIdsDTO buildManageCommonIdsDTO(Map<String, Object> param) {
        com.houjiang.goods.dto.ManageCommonIdsDTO dto = new com.houjiang.goods.dto.ManageCommonIdsDTO();
        if (param.get("id") != null) {
            dto.setCommonId(Integer.valueOf(param.get("id").toString()));
        }
        return dto;
    }
}
