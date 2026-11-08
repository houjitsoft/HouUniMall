package com.houjiang.mall.controller.front;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.mapper.front.FrontShopMapper;
import com.houjiang.mall.util.MallMediaUrlHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商城前台店铺接口（供 BFF 内部调用）
 *
 * 数据来源：hou_merchant_mall_shop（店铺）、hou_mall_goods（店铺商品）。
 * 路径前缀 /mall/front/** 已在 MallAdminTokenAuthenticationFilter 白名单，匿名可访问。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/front/shop")
@RequiredArgsConstructor
public class FrontShopController {

    private final FrontShopMapper frontShopMapper;
    private final MallMediaUrlHelper mediaUrlHelper;

    /**
     * 店铺分页列表（含收藏数、商品预览图）
     *
     * @param param 查询参数：page、limit、keyword
     * @return {lists, count, page, limit}
     */
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult list(@RequestBody(required = false) Map<String, Object> param) {
        Map<String, Object> params = param == null ? Map.of() : param;
        // 设置分页默认值，防止前端未传参导致NPE
        int page = toInt(params.get("page"), 1);
        int limit = toInt(params.get("limit"), 10);
        String keyword = params.get("keyword") != null ? String.valueOf(params.get("keyword")) : null;

        long count = frontShopMapper.countShops(keyword);
        List<Map<String, Object>> rows = count == 0
                ? List.of()
                : frontShopMapper.selectShops(keyword, limit, (page - 1) * limit);

        List<Map<String, Object>> lists = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            lists.add(buildShopItem(row));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("lists", lists);
        result.put("count", count);
        result.put("page_no", page);
        result.put("page_size", limit);
        return ResponseResult.success(result);
    }

    /**
     * 店铺详情
     *
     * @param id 店铺ID
     * @return 店铺详情对象
     */
    @GetMapping("/detail")
    public ResponseResult detail(@RequestParam("id") Long id) {
        Map<String, Object> row = frontShopMapper.selectShopById(id);
        if (row == null) {
            return ResponseResult.fail("店铺不存在");
        }
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", row.get("id"));
        detail.put("name", row.get("name"));
        detail.put("logo", mediaUrlHelper.toAbsoluteUrl(str(row.get("logo"))));
        detail.put("intro", row.get("desc"));
        detail.put("contact", row.get("contact"));
        detail.put("mobile", row.get("mobile"));
        detail.put("address", row.get("address_info"));
        detail.put("is_self", 0);
        detail.put("is_collect", 0);
        detail.put("fans", frontShopMapper.countShopCollects(id));
        detail.put("goods_count", frontShopMapper.countShopGoods(id));
        detail.put("desc_score", "5.0");
        detail.put("service_score", "5.0");
        detail.put("delivery_score", "5.0");
        return ResponseResult.success(detail);
    }

    /**
     * 店铺商品经营分类（按店铺在售商品关联的全局分类聚合）
     *
     * @param id 店铺ID
     * @return 分类数组
     */
    @GetMapping("/category")
    public ResponseResult category(@RequestParam("id") Long id) {
        return ResponseResult.success(frontShopMapper.selectShopCategories(id));
    }

    /**
     * 组装店铺列表项：附带收藏数、评分默认值与商品预览图
     */
    private Map<String, Object> buildShopItem(Map<String, Object> row) {
        Long id = Long.valueOf(String.valueOf(row.get("id")));
        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("name", row.get("name"));
        item.put("logo", mediaUrlHelper.toAbsoluteUrl(str(row.get("logo"))));
        item.put("is_self", 0);
        long fans = frontShopMapper.countShopCollects(id);
        item.put("fans", fans);
        item.put("collect_count", fans);
        item.put("goods_count", frontShopMapper.countShopGoods(id));
        item.put("desc_score", "5.0");
        item.put("service_score", "5.0");
        item.put("delivery_score", "5.0");

        List<Map<String, Object>> goods = new ArrayList<>();
        for (Map<String, Object> g : frontShopMapper.selectShopGoodsPreview(id, 4)) {
            Map<String, Object> gi = new HashMap<>();
            gi.put("id", g.get("id"));
            gi.put("name", g.get("name"));
            gi.put("image", mediaUrlHelper.toAbsoluteUrl(str(g.get("image"))));
            gi.put("min_price", g.get("min_price"));
            goods.add(gi);
        }
        item.put("goods", goods);
        return item;
    }

    private int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
