package com.houjiang.mall.controller.shop;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.entity.shop.Goods;
import com.houjiang.mall.service.shop.GoodsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品管理 Controller
 * 路径：/mall/shop/goods/*
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/shop/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    /**
     * 获取商品列表（分页）
     * POST /mall/shop/goods/list
     * 
     * @param params 查询参数
     * @return 商品列表分页数据
     */
    @PostMapping("/list")
    public ResponseResult<IPage<Goods>> getGoodsList(@RequestBody Map<String, Object> params) {
        try {
            log.info("查询商品列表，参数：{}", params);
            IPage<Goods> page = goodsService.pageList(params);
            return ResponseResult.success(page);
        } catch (Exception e) {
            log.error("查询商品列表失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取商品详情
     * POST /mall/shop/goods/detail
     * 
     * @param params 查询参数（包含 goodsId）
     * @return 商品详情
     */
    @PostMapping("/detail")
    public ResponseResult<Goods> getGoodsDetail(@RequestBody Map<String, Object> params) {
        try {
            log.info("查询商品详情，参数：{}", params);
            
            if (!params.containsKey("goodsId")) {
                return ResponseResult.error("缺少商品 ID 参数");
            }
            
            Integer goodsId = Integer.parseInt(params.get("goodsId").toString());
            Goods goods = goodsService.getGoodsDetail(goodsId);
            
            if (goods == null) {
                return ResponseResult.error("商品不存在");
            }
            
            return ResponseResult.success(goods);
        } catch (Exception e) {
            log.error("查询商品详情失败", e);
            return ResponseResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加商品
     * POST /mall/shop/goods/add
     * 
     * @param params 商品信息
     * @return 是否成功
     */
    @PostMapping("/add")
    public ResponseResult<Boolean> addGoods(@RequestBody Map<String, Object> params) {
        try {
            log.info("添加商品，参数：{}", params);
            
            Goods goods = convertToGoods(params);
            boolean result = goodsService.addGoods(goods);
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("添加商品失败", e);
            return ResponseResult.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 编辑商品
     * POST /mall/shop/goods/edit
     * 
     * @param params 商品信息（包含 goodsId）
     * @return 是否成功
     */
    @PostMapping("/edit")
    public ResponseResult<Boolean> editGoods(@RequestBody Map<String, Object> params) {
        try {
            log.info("编辑商品，参数：{}", params);
            
            if (!params.containsKey("goodsId")) {
                return ResponseResult.error("缺少商品 ID 参数");
            }
            
            Goods goods = convertToGoods(params);
            boolean result = goodsService.editGoods(goods);
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("编辑商品失败", e);
            return ResponseResult.error("编辑失败：" + e.getMessage());
        }
    }

    /**
     * 商品上下架
     * POST /mall/shop/goods/status
     * 
     * @param params 查询参数（包含 goodsId 和 status）
     * @return 是否成功
     */
    @PostMapping("/status")
    public ResponseResult<Boolean> updateGoodsStatus(@RequestBody Map<String, Object> params) {
        try {
            log.info("更新商品状态，参数：{}", params);
            
            if (!params.containsKey("goodsId") || !params.containsKey("status")) {
                return ResponseResult.error("缺少必要参数");
            }
            
            Integer goodsId = Integer.parseInt(params.get("goodsId").toString());
            Integer status = Integer.parseInt(params.get("status").toString());
            
            // 验证状态值：1-上架 2-下架
            if (!status.equals(1) && !status.equals(2)) {
                return ResponseResult.error("状态值错误，必须为 1（上架）或 2（下架）");
            }
            
            boolean result = goodsService.updateGoodsStatus(goodsId, status);
            
            if (!result) {
                return ResponseResult.error("更新失败，商品可能不存在");
            }
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("更新商品状态失败", e);
            return ResponseResult.error("操作失败：" + e.getMessage());
        }
    }

    /**
     * 删除商品（移到回收站）
     * POST /mall/shop/goods/delete
     * 
     * @param params 查询参数（包含 goodsId）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public ResponseResult<Boolean> deleteGoods(@RequestBody Map<String, Object> params) {
        try {
            log.info("删除商品，参数：{}", params);
            
            if (!params.containsKey("goodsId")) {
                return ResponseResult.error("缺少商品 ID 参数");
            }
            
            Integer goodsId = Integer.parseInt(params.get("goodsId").toString());
            boolean result = goodsService.deleteGoods(goodsId);
            
            if (!result) {
                return ResponseResult.error("删除失败，商品可能不存在");
            }
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("删除商品失败", e);
            return ResponseResult.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除商品
     * POST /mall/shop/goods/batchDelete
     * 
     * @param params 查询参数（包含 goodsIds 数组）
     * @return 是否成功
     */
    @PostMapping("/batchDelete")
    public ResponseResult<Boolean> batchDeleteGoods(@RequestBody Map<String, Object> params) {
        try {
            log.info("批量删除商品，参数：{}", params);
            
            if (!params.containsKey("goodsIds")) {
                return ResponseResult.error("缺少商品 ID 数组参数");
            }
            
            Object goodsIdsObj = params.get("goodsIds");
            Integer[] goodsIds;
            
            if (goodsIdsObj instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) goodsIdsObj;
                goodsIds = list.stream()
                    .map(id -> Integer.parseInt(id.toString()))
                    .toArray(Integer[]::new);
            } else {
                return ResponseResult.error("商品 ID 数组格式错误");
            }
            
            boolean result = goodsService.batchDeleteGoods(goodsIds);
            return ResponseResult.success(result);
        } catch (Exception e) {
            log.error("批量删除商品失败", e);
            return ResponseResult.error("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 将 Map 参数转换为 Goods 对象
     * TODO: 实际项目中建议使用 BeanUtils.copyProperties() 进行转换
     * 
     * @param params 参数 Map
     * @return Goods 实体
     */
    private Goods convertToGoods(Map<String, Object> params) {
        Goods goods = new Goods();
        
        // 基础字段
        if (params.containsKey("goodsId")) {
            goods.setGoodsId(Integer.parseInt(params.get("goodsId").toString()));
        }
        if (params.containsKey("commonId")) {
            goods.setCommonId(Integer.parseInt(params.get("commonId").toString()));
        }
        if (params.containsKey("shopId")) {
            goods.setShopId(Integer.parseInt(params.get("shopId").toString()));
        }
        if (params.containsKey("shopName")) {
            goods.setShopName(params.get("shopName").toString());
        }
        if (params.containsKey("goodsName")) {
            goods.setGoodsName(params.get("goodsName").toString());
        }
        if (params.containsKey("catId")) {
            goods.setCatId(Integer.parseInt(params.get("catId").toString()));
        }
        if (params.containsKey("brandId")) {
            goods.setBrandId(Integer.parseInt(params.get("brandId").toString()));
        }
        if (params.containsKey("goodsSpec")) {
            goods.setGoodsSpec(params.get("goodsSpec").toString());
        }
        if (params.containsKey("goodsPrice")) {
            goods.setGoodsPrice(new BigDecimal(params.get("goodsPrice").toString()));
        }
        if (params.containsKey("goodsMarketPrice")) {
            goods.setGoodsMarketPrice(new BigDecimal(params.get("goodsMarketPrice").toString()));
        }
        if (params.containsKey("goodsAlarm")) {
            goods.setGoodsAlarm(Integer.parseInt(params.get("goodsAlarm").toString()));
        }
        if (params.containsKey("goodsCode")) {
            goods.setGoodsCode(params.get("goodsCode").toString());
        }
        if (params.containsKey("goodsIsRecommend")) {
            goods.setGoodsIsRecommend(Integer.parseInt(params.get("goodsIsRecommend").toString()));
        }
        if (params.containsKey("goodsImage")) {
            goods.setGoodsImage(params.get("goodsImage").toString());
        }
        if (params.containsKey("goodsIsShelves")) {
            goods.setGoodsIsShelves(Integer.parseInt(params.get("goodsIsShelves").toString()));
        }
        if (params.containsKey("goodsCubage")) {
            goods.setGoodsCubage(new BigDecimal(params.get("goodsCubage").toString()));
        }
        if (params.containsKey("dw")) {
            goods.setDw(params.get("dw").toString());
        }
        if (params.containsKey("moq")) {
            goods.setMoq(Integer.parseInt(params.get("moq").toString()));
        }
        if (params.containsKey("isEnquiry")) {
            goods.setIsEnquiry(Integer.parseInt(params.get("isEnquiry").toString()));
        }
        if (params.containsKey("msrp")) {
            goods.setMsrp(params.get("msrp").toString());
        }
        if (params.containsKey("sampleGoodsNum")) {
            goods.setSampleGoodsNum(Integer.parseInt(params.get("sampleGoodsNum").toString()));
        }
        
        return goods;
    }
}
