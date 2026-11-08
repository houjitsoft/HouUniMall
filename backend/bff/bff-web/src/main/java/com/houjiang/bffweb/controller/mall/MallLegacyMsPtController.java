package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.MerchantServiceFeign;
import com.houjiang.bffweb.feign.OrderServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * H5/装修组件兼容 like-shop 旧路径：/mall.ms/*、/mall.pt/*
/**
 * 统一映射为 /web/mall/ms/*、/web/mall/pt/* 后由本 Controller 转发 merchant 服务。
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class MallLegacyMsPtController {

    private final MerchantServiceFeign merchantServiceFeign;

    private final OrderServiceFeign orderServiceFeign;

    private final MallServiceFeign mallServiceFeign;

    // ================= 老版 H5（uniapp）订单契约兼容 =================
    // 老 H5 详情页加购走 /web/order/userCart/insertShoppingCart，
    // 评价列表走 /web/order/order/goodsEvaluation/getComments，
    // 这两个路径在新 BFF 里已改名为 /web/mall/cart/add、/web/mall/goods/comment/list，
    // 老页面不知道 → 加购恒失败、评价恒为空。这里做别名转发。

    /**
     * 老版加入购物车（参数与 /web/mall/cart/add 一致：goodsId + goodsNum）
     */
    @PostMapping("/web/order/userCart/insertShoppingCart")
    public ResponseResult legacyInsertCart(@RequestBody(required = false) Map<String, Object> param,
                                           HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "goodsId", "goods_sku_id");
        MallTokenSupport.putIfAbsentFrom(body, "goodsNum", "num");
        return orderServiceFeign.addToCart(body);
    }

    /**
     * 老版商品评价列表：老页面只认 { records: { list, totalCount } } 结构
     */
    @PostMapping("/web/order/order/goodsEvaluation/getComments")
    public ResponseResult legacyGoodsComments(@RequestBody(required = false) Map<String, Object> param) {
        Map<String, Object> query = new HashMap<>();
        if (param != null) {
            query.putAll(param);
        }
        MallTokenSupport.putIfAbsentFrom(query, "page_no", "page");
        MallTokenSupport.putIfAbsentFrom(query, "page_size", "limit");
        MallTokenSupport.putIfAbsentFrom(query, "goodsId", "goods_id");
        if (query.get("goodsId") == null && query.get("goods_id") != null) {
            query.put("goodsId", query.get("goods_id"));
        }
        if (query.get("page") == null && query.get("page_no") != null) {
            query.put("page", query.get("page_no"));
        }
        if (query.get("limit") == null && query.get("page_size") != null) {
            query.put("limit", query.get("page_size"));
        }
        List<Object> list = new ArrayList<>();
        long total = 0L;
        try {
            ResponseResult result = mallServiceFeign.frontCommentList(query);
            if (result != null && result.getCode() == 1 && result.getData() instanceof Map<?, ?> raw) {
                Object lists = raw.get("lists");
                if (lists instanceof List<?> items) {
                    list = new ArrayList<>(items);
                }
                Object count = raw.get("count");
                if (count instanceof Number number) {
                    total = number.longValue();
                }
            }
        } catch (Exception e) {
            log.warn("查询商品评价失败，返回空列表: {}", e.getMessage());
        }
        Map<String, Object> records = new LinkedHashMap<>();
        records.put("list", list);
        records.put("totalCount", total);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("records", records);
        return ResponseResult.success(data);
    }

    // ================= admin 装修组件路径别名 =================
    // admin 前端 api/decoration.js 调的是 /web/mall/decorate/**，而历史实现只有
    // /web/mall/ms|pt/**，导致 20001「请求的资源不存在」。这里补别名路由转发同一实现。
    @GetMapping("/web/mall/decorate/ms/lists")
    public Object decorateMsLists(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.ms", "lists", params);
    }

    @GetMapping("/web/mall/decorate/ms/category_lists")
    public Object decorateMsCategoryLists(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.ms", "category_lists", params);
    }

    @GetMapping("/web/mall/decorate/store/lists")
    public Object decorateStoreLists(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.ms", "lists", params);
    }

    @GetMapping("/web/mall/decorate/article/lists")
    public Object decorateArticleLists(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.ms", "lists", params);
    }

    @GetMapping("/web/mall/decorate/hot_goods/lists")
    public Object decorateHotGoodsLists(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.ms", "lists", params);
    }

    @GetMapping("/web/mall/ms/lists")
    public Object msLists(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.ms", "lists", params);
    }

    @GetMapping("/web/mall/ms/category_lists")
    public Object msCategoryLists(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.ms", "category_lists", params);
    }

    @GetMapping("/web/mall/ms/detail")
    public Object msDetail(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.ms", "detail", params);
    }

    @GetMapping("/web/mall/pt/lists")
    public Object ptLists(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.pt", "lists", params);
    }

    @GetMapping("/web/mall/pt/detail")
    public Object ptDetail(@RequestParam(required = false) Map<String, Object> params) {
        return forwardOrEmpty("mall.pt", "detail", params);
    }

    /**
     * 老版区域可售查询（详情页「送至 XX / 有货」那一行）
     *
     * <p>老商城按「商品 × 区域」矩阵返回可售标记，data[i][j] == 1 表示可售；
     * 该接口在新 BFF 里没有对应实现，详情页拿不到结果就直接兜底成「缺货」，
     * 连带把「加入购物车」换成「到货通知」。评估线不做按区域限售，统一返回可售。
     */
    @PostMapping("/web/merchant/shop/transportArea/query")
    public ResponseResult legacyTransportArea(@RequestBody(required = false) Map<String, Object> param) {
        int size = legacyGoodsGroupSize(param);
        List<List<Integer>> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(List.of(1));
        }
        return ResponseResult.success(data);
    }

    /**
     * 老版区域可售查询（下单页计算运费/是否可发，data 为扁平数组，元素 == 1 表示可发）
     */
    @PostMapping("/web/merchant/shop/transportArea/queryTwo")
    public ResponseResult legacyTransportAreaTwo(@RequestBody(required = false) Map<String, Object> param) {
        int size = legacyGoodsGroupSize(param);
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            data.add(1);
        }
        return ResponseResult.success(data);
    }

    /**
     * 老版货到付款开关：结算页进页即调，新 BFF 只有 /web/mall 版本，
     * 老页面按 /web/merchant（businessRequest 基址）请求 → 20001。此处补别名，统一返回未开通。
     */
    @PostMapping("/web/merchant/shop/cashOnDelivery/queryIsDelivery")
    public ResponseResult legacyCashOnDelivery(@RequestBody(required = false) Map<String, Object> param) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shopId", null);
        result.put("isDelivery", 0);
        return ResponseResult.success(result);
    }

    private static int legacyGoodsGroupSize(Map<String, Object> param) {
        Object value = param == null ? null : param.get("shopGoodsIds");
        if (value instanceof List<?> outer && !outer.isEmpty()) {
            Object first = outer.get(0);
            if (first instanceof List<?> inner && !inner.isEmpty()) {
                return inner.size();
            }
            return outer.size();
        }
        return 1;
    }

    private Object forwardOrEmpty(String module, String action, Map<String, Object> params) {
        try {
            return merchantServiceFeign.forwardGet(module, action, params != null ? params : Map.of());
        } catch (Exception e) {
            log.warn("转发 {}.{} 失败，返回空列表: {}", module, action, e.getMessage());
            return ok(emptyPage());
        }
    }

    private static Map<String, Object> ok(Object data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("code", 1);
        resp.put("msg", "");
        resp.put("data", data);
        resp.put("show", 0);
        return resp;
    }

    private static Map<String, Object> emptyPage() {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("count", 0);
        page.put("page_no", 1);
        page.put("page_size", 15);
        page.put("lists", new ArrayList<>());
        page.put("extend", new LinkedHashMap<>());
        return page;
    }
}
