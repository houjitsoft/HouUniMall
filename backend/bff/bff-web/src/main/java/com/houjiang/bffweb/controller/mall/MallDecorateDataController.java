package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.ActivityServiceFeign;
import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商城营销数据BFF Controller
/**
 * 供装修系统选择营销商品和推荐店铺时使用
 *
 * 对应前端 decoration.js 中的以下接口：
 *   - GET /web/mall/pt/goodsOtherLists    → 拼团商品列表
 *   - GET /web/mall/ms/goodsOtherLists    → 秒杀商品列表
 *   - GET /web/mall/shop/recommend_lists  → 推荐店铺列表
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MallDecorateDataController {

    private final ActivityServiceFeign activityServiceFeign;
    private final MallServiceFeign mallServiceFeign;
    private final GoodsServiceFeign goodsServiceFeign;

    /**
     * 获取拼团商品列表（装修用）
/**
     * GET /web/mall/pt/goodsOtherLists
     *
     * @param page  页码（可选，默认1）
     * @param size  每页数量（可选，默认10）
     * @return 拼团商品列表数据
     */
    @GetMapping("/web/mall/pt/goodsOtherLists")
    public ResponseResult getPtGoodsOtherLists(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            log.info("获取拼团商品列表，page={}, size={}", page, size);
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("pageSize", size);
            params.put("activityType", "group_buy");
            // 复用秒杀商品接口，返回活动商品列表
            return activityServiceFeign.querySecondKillGoods(params);
        } catch (Exception e) {
            log.error("获取拼团商品列表失败", e);
            // 降级：返回普通商品列表
            Map<String, Object> fallbackParams = new HashMap<>();
            fallbackParams.put("page", page);
            fallbackParams.put("size", size);
            return goodsServiceFeign.queryGoodsList(fallbackParams);
        }
    }

    /**
     * 获取秒杀商品列表（装修用）
/**
     * GET /web/mall/ms/goodsOtherLists
     *
     * @param page  页码（可选，默认1）
     * @param size  每页数量（可选，默认10）
     * @return 秒杀商品列表数据
     */
    @GetMapping("/web/mall/ms/goodsOtherLists")
    public ResponseResult getMsGoodsOtherLists(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            log.info("获取秒杀商品列表，page={}, size={}", page, size);
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("pageSize", size);
            return activityServiceFeign.querySecondKillGoods(params);
        } catch (Exception e) {
            log.error("获取秒杀商品列表失败", e);
            // 降级：返回普通商品列表
            Map<String, Object> fallbackParams = new HashMap<>();
            fallbackParams.put("page", page);
            fallbackParams.put("size", size);
            return goodsServiceFeign.queryGoodsList(fallbackParams);
        }
    }

    /**
     * 获取推荐店铺列表（装修用）
/**
     * GET /web/mall/shop/recommend_lists
     *
     * @param page  页码（可选，默认1）
     * @param size  每页数量（可选，默认10）
     * @return 推荐店铺列表数据
     */
    @GetMapping("/web/mall/shop/recommend_lists")
    public ResponseResult getRecommendShops(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        try {
            log.info("获取推荐店铺列表，page={}, size={}", page, size);
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("size", size);
            params.put("status", 1);
            return mallServiceFeign.getShopList(params);
        } catch (Exception e) {
            log.error("获取推荐店铺列表失败", e);
            return ResponseResult.error("获取推荐店铺失败：" + e.getMessage());
        }
    }
}
