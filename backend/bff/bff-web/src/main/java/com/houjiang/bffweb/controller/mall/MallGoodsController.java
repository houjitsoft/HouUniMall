package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mall商城商品接口控制器
/**
 * 管理后台列表/分类树走 mall 服务（MALL_ADMIN_TOKEN）；其余接口仍直连 goods（逐步迁移）。
 *
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/goods")
public class MallGoodsController {
    
    @Autowired
    private MallServiceFeign mallServiceFeign;

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    @Autowired(required = false)
    private com.houjiang.bffweb.feign.ReductionQueryFeign reductionQueryFeign;

    @Autowired(required = false)
    private com.houjiang.bffweb.feign.DiscountQueryFeign discountQueryFeign;

    @PostMapping("/goods/userFootprint/query")
    public ResponseResult queryUserFootprint(@RequestBody Map<String, Object> params) {
        return goodsServiceFeign.queryUserFootprint(params);
    }

    @PostMapping("/goods/userFootprint/delete")
    public ResponseResult deleteUserFootprint(@RequestBody Map<String, Object> params) {
        return goodsServiceFeign.deleteUserFootprint(params);
    }

    @PostMapping("/goods/userFootprint/im/queryFootPrint")
    public ResponseResult queryImFootprint(@RequestBody Map<String, Object> params) {
        return goodsServiceFeign.queryImFootprint(params);
    }
    
    // =============== 商品基础查询 ===============

    /**
     * 获取商品列表
/**
     *
     * @param param 查询参数（分页、筛选）
     * @return ResponseResult 商品列表数据
     */
    @PostMapping("/list")
    public ResponseResult getGoodsList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.mallAdminGoodsList(param);
    }

    /**
     * 前台商品搜索（H5 商品列表页 / 搜索结果页）。
     *
     * <p>路径与入参照搬老 uniapp 直连 goods 的契约：前端拼出来是
     * /web/mall/goods/es/searchGoodsData，BFF 迁移时这条没补，页面拿到 20001 后渲染成空列表。
     * 鉴权靠 FeignRequestInterceptor 把 Authorization（或老客户端的 Token 头）透传给 goods。
     *
     * @param param 搜索参数（分页、分类、价格、排序、关键字）
     * @return 与 goods /es/searchGoodsData 完全一致的 SearchVO 结构
     */
    @PostMapping("/es/searchGoodsData")
    public ResponseResult searchGoodsData(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.searchGoodsData(param);
    }

    /**
     * 前台分类/品牌/属性筛选项：商品列表页的分类条与侧滑筛选面板都读它。
     *
     * @param param 含 catId 的筛选查询
     * @return 分类层级 + 品牌 + 属性
     */
    @PostMapping("/goods/cat/query/filter")
    public ResponseResult queryCatFilter(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryCatFilter(param);
    }

    /** 商品配送区域查询。 */
    @PostMapping("/transport/area/query")
    public ResponseResult queryTransportArea(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryTransportArea(param);
    }

    /**
     * 前台商品列表（不走管理后台鉴权，PC/H5 共用）
     */
    @RequestMapping(value = "/front/lists", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult frontGoodsLists(@RequestParam(required = false) Map<String, Object> query,
                                          @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> param = new HashMap<>();
        if (query != null) {
            param.putAll(query);
        }
        if (body != null) {
            param.putAll(body);
        }
        copyAlias(param, "page_no", "pageNo");
        copyAlias(param, "page", "pageNo");
        copyAlias(param, "page_size", "pageSize");
        copyAlias(param, "limit", "pageSize");
        copyAlias(param, "category_id", "categoryId");
        copyAlias(param, "cid", "categoryId");
        copyAlias(param, "catId", "categoryId");
        copyAlias(param, "categoryId", "cid");
        copyAlias(param, "categoryId", "catId");
        copyAlias(param, "shop_id", "shopId");
        // PC/H5 搜索关键词：commonName/goodsName/kws/keyword -> name
        copyAlias(param, "commonName", "name");
        copyAlias(param, "goodsName", "name");
        copyAlias(param, "kws", "name");
        copyAlias(param, "keyword", "name");
        if (param.containsKey("page_no") && !param.containsKey("page")) {
            param.put("page", param.get("page_no"));
        }
        if (param.containsKey("page_size") && !param.containsKey("limit")) {
            param.put("limit", param.get("page_size"));
        }
        if (param.containsKey("category_id") && !param.containsKey("cid")) {
            param.put("cid", param.get("category_id"));
        }
        // 前台只展示已上架且审核通过的商品
        if (!param.containsKey("type") && !param.containsKey("status")) {
            param.put("type", "sale");
        }
        // 透传语种供列表名称本地化
        if (!param.containsKey("lang")) {
            copyAlias(param, "languageCode", "lang");
            copyAlias(param, "language", "lang");
        }
        return goodsServiceFeign.mallGoodsList(param);
    }

    /** 前台筛选项及商品列表，供 PC 商城商品列表页使用。 */
    @RequestMapping(value = "/front/filter", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult frontGoodsFilter(@RequestBody(required = false) Map<String, Object> body) {
        return goodsServiceFeign.queryGoodsFilter(body == null ? new HashMap<>() : body);
    }

    private void copyAlias(Map<String, Object> param, String from, String to) {
        if (param.containsKey(from) && !param.containsKey(to)) {
            param.put(to, param.get(from));
        }
    }

    /**
     * 前台分类树
     */
    @RequestMapping(value = "/category/all", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult frontCategoryAll() {
        return goodsServiceFeign.mallCategoryAll();
    }

    /**
     * 前台商品详情
     */
    @RequestMapping(value = "/front/detail", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult frontGoodsDetail(@RequestParam(value = "id", required = false) Integer id,
                                           @RequestParam(value = "lang", required = false) String lang,
                                           @RequestBody(required = false) Map<String, Object> body) {
        Integer gid = id;
        String langCode = lang;
        if (body != null) {
            if (gid == null && body.get("id") != null) {
                gid = Integer.valueOf(body.get("id").toString());
            }
            if (langCode == null && body.get("lang") != null) {
                langCode = body.get("lang").toString();
            }
            if (langCode == null && body.get("languageCode") != null) {
                langCode = body.get("languageCode").toString();
            }
        }
        if (gid == null) {
            return ResponseResult.error("商品ID不能为空");
        }
        ResponseResult result = goodsServiceFeign.mallGoodsDetail(gid, langCode);
        if (result != null && result.getData() != null) {
            Object flat = flattenMallGoodsDetail(result.getData());
            result.setData(attachPromoToFlatDetail(flat, gid));
        }
        return result;
    }

    /** 前台扁平详情挂限时折扣（优先）或满减。 */
    @SuppressWarnings("unchecked")
    private Object attachPromoToFlatDetail(Object data, Integer goodsId) {
        Object withDiscount = attachDiscountToFlatDetail(data, goodsId);
        if (withDiscount instanceof Map<?, ?> m && Integer.valueOf(3).equals(m.get("commonIsXian"))) {
            return withDiscount;
        }
        return attachReductionToFlatDetail(withDiscount, goodsId);
    }

    @SuppressWarnings("unchecked")
    private Object attachDiscountToFlatDetail(Object data, Integer goodsId) {
        if (!(data instanceof Map<?, ?>) || discountQueryFeign == null || goodsId == null) {
            return data;
        }
        Map<String, Object> flat = (Map<String, Object>) data;
        try {
            Map<String, Object> dg = discountQueryFeign.selectByGoodsIdAndTime(goodsId);
            if (dg == null || dg.get("discountPrice") == null) {
                return flat;
            }
            Map<String, Object> activityDetail = new HashMap<>();
            activityDetail.put("activityId", dg.get("discountId"));
            activityDetail.put("activityStatus", 3);
            activityDetail.put("activityPrice", dg.get("discountPrice"));
            activityDetail.put("activityStartTime", dg.get("goodsStartTime"));
            activityDetail.put("activityEndTime", dg.get("goodsEndTime"));
            activityDetail.put("discountGoods", dg);
            flat.put("commonIsXian", 3);
            flat.put("activityGoodsDetailDTO", activityDetail);
            flat.put("activityPrice", dg.get("discountPrice"));
            flat.put("promotionContent", "限时折扣");
            flat.put("goodsPromotionTips", "限时折扣");
        } catch (Exception ignored) {
            // 折扣不可用时保持原详情
        }
        return flat;
    }

    /** 前台扁平详情挂上生效满减（展示文案 + commonIsXian）。 */
    @SuppressWarnings("unchecked")
    private Object attachReductionToFlatDetail(Object data, Integer goodsId) {
        if (!(data instanceof Map<?, ?>) || reductionQueryFeign == null || goodsId == null) {
            return data;
        }
        Map<String, Object> flat = (Map<String, Object>) data;
        try {
            Map<String, Object> mj = reductionQueryFeign.selectByGoodsId(goodsId);
            if (mj == null || mj.get("promotionId") == null) {
                return flat;
            }
            Integer promotionId = Integer.valueOf(String.valueOf(mj.get("promotionId")));
            List<Map<String, Object>> rules = reductionQueryFeign.selectList(promotionId);
            List<Map<String, Object>> contents = reductionQueryFeign.selectContent(goodsId);
            Map<String, Object> activityDetail = new HashMap<>();
            activityDetail.put("promotionId", promotionId);
            activityDetail.put("activityId", promotionId);
            activityDetail.put("activityStatus", 5);
            activityDetail.put("ruleList", rules == null ? List.of() : rules);
            activityDetail.put("activityEndTime", mj.get("endTime"));
            flat.put("commonIsXian", 5);
            flat.put("activityGoodsDetailDTO", activityDetail);
            if (contents != null && !contents.isEmpty() && contents.get(0).get("promotionContent") != null) {
                String promo = String.valueOf(contents.get(0).get("promotionContent"));
                flat.put("promotionContent", promo);
                flat.put("goodsPromotionTips", promo);
            }
        } catch (Exception ignored) {
            // 满减不可用时保持普通商品详情
        }
        return flat;
    }

    @SuppressWarnings("unchecked")
    private Object flattenMallGoodsDetail(Object data) {
        if (!(data instanceof Map<?, ?> rawMap)) {
            return data;
        }
        Map<String, Object> root = (Map<String, Object>) rawMap;
        Object goodsObj = root.get("goods");
        if (!(goodsObj instanceof Map<?, ?> goodsMap)) {
            return data;
        }
        Map<String, Object> flat = new HashMap<>((Map<String, Object>) goodsMap);
        Object imageList = root.get("imageList") != null ? root.get("imageList") : root.get("image_list");
        if (imageList instanceof List<?> imgs && !imgs.isEmpty()) {
            flat.put("images", imgs);
            flat.put("goods_image", imgs);
            if (flat.get("image") == null || String.valueOf(flat.get("image")).isBlank()) {
                flat.put("image", imgs.get(0));
            }
        }
        Object skuList = root.get("skuList") != null ? root.get("skuList") : root.get("sku_list");
        if (skuList != null) {
            flat.put("sku", skuList);
            flat.put("skuList", skuList);
        }
        Object skuNames = root.get("skuNameList") != null ? root.get("skuNameList") : root.get("sku_name_list");
        if (skuNames instanceof List<?> names) {
            List<Map<String, Object>> skuName = new ArrayList<>();
            for (Object n : names) {
                if (n instanceof String s) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", s);
                    item.put("sku_value", List.of());
                    skuName.add(item);
                } else if (n instanceof Map<?, ?> m) {
                    skuName.add((Map<String, Object>) m);
                }
            }
            flat.put("sku_name", skuName);
        }
        return flat;
    }
    
    // =============== 老版 H5（uniapp）商品契约兼容 ===============
    // 老 H5 详情页调的是 /web/mall/goods/goods/common/query/detail，且只认
    // { goodsBases, gdsCommon, shopDetail, goodsImages } 这套结构，
    // 而新版 /front/detail 返回的是扁平结构；路径与结构双重不匹配时，
    // 页面恒显示「店铺或商品已下架」，加购与立即购买也就无从触发。
    // 这里按老契约做适配，页面与下单流程无需改动。

    /**
     * 老版商品详情（uniapp 详情页 / 下单页依赖的 legacy 契约）
     *
     * @param body  请求体（goodsId / goods_id / id）
     * @param query 查询参数，兜底
     * @return 老契约结构的商品详情
     */
    @PostMapping("/goods/common/query/detail")
    public ResponseResult legacyGoodsDetail(@RequestBody(required = false) Map<String, Object> body,
                                            @RequestParam(required = false) Map<String, Object> query,
                                            HttpServletRequest request) {
        Map<String, Object> param = new HashMap<>();
        if (query != null) {
            param.putAll(query);
        }
        if (body != null) {
            param.putAll(body);
        }
        Integer goodsId = legacyInt(param, "goodsId", "goods_id", "id");
        if (goodsId == null || goodsId <= 0) {
            return ResponseResult.error("商品ID不能为空");
        }
        return legacyDetailPayload(goodsId, request);
    }

    /**
     * 老版规格接口（切换规格时调用，返回与详情同构的数据）
     */
    @PostMapping("/goods/common/spec/queryNew")
    public ResponseResult legacyGoodsSpec(@RequestBody(required = false) Map<String, Object> body,
                                          @RequestParam(required = false) Map<String, Object> query,
                                          HttpServletRequest request) {
        Map<String, Object> param = new HashMap<>();
        if (query != null) {
            param.putAll(query);
        }
        if (body != null) {
            param.putAll(body);
        }
        Integer goodsId = legacyInt(param, "goodsId", "goods_id", "id");
        if (goodsId == null || goodsId <= 0) {
            return ResponseResult.error("商品ID不能为空");
        }
        return legacyDetailPayload(goodsId, request);
    }

    /**
     * 老版图文详情（详情页「详情」tab 的主体内容）
     */
    @GetMapping("/goods/common/desc/query")
    public ResponseResult legacyGoodsDesc(@RequestParam(required = false) Map<String, Object> query,
                                          @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> param = new HashMap<>();
        if (query != null) {
            param.putAll(query);
        }
        if (body != null) {
            param.putAll(body);
        }
        Integer goodsId = legacyInt(param, "commonsId", "commonId", "goodsId", "goods_id", "id");
        if (goodsId == null || goodsId <= 0) {
            return ResponseResult.error("商品ID不能为空");
        }
        ResponseResult detail = frontGoodsDetail(goodsId, null, null);
        if (detail == null || detail.getCode() != 1 || !(detail.getData() instanceof Map<?, ?> raw)) {
            return ResponseResult.error("商品不存在或已下架");
        }
        Map<String, Object> goods = castMap(raw);
        Map<String, Object> data = new HashMap<>();
        data.put("attributeParam", new ArrayList<>());
        data.put("brandName", "");
        data.put("commonBody", goods.get("content"));
        data.put("commonBodyWechat", "");
        return ResponseResult.success(data);
    }

    /**
     * 老版商品保障 / 品牌分类：页面仅做展示，缺失不阻断
     */
    @PostMapping("/goods/BrandCat/query/list")
    public ResponseResult legacyBrandCat() {
        return ResponseResult.success(new ArrayList<>());
    }

    /**
     * 老版浏览足迹上报：详情页进页即调，失败会连带阻断后续渲染
     */
    @PostMapping("/goods/base/h5Userfoot/query")
    public ResponseResult legacyUserFoot(@RequestBody(required = false) Map<String, Object> body) {
        return ResponseResult.success(new HashMap<>());
    }

    /**
     * 老版分享海报：当前环境不生成海报，返回业务失败让前端跳过该模块
     */
    @PostMapping("/goods/base/h5/goods/getGoodsPoster")
    public ResponseResult legacyGoodsPoster() {
        return ResponseResult.error("暂不支持生成分享海报");
    }

    /**
     * 把新版扁平商品详情组装成老契约结构
     */
    @SuppressWarnings("unchecked")
    private ResponseResult legacyDetailPayload(Integer goodsId, HttpServletRequest request) {
        ResponseResult detail = frontGoodsDetail(goodsId, null, null);
        if (detail == null || detail.getCode() != 1 || !(detail.getData() instanceof Map<?, ?> raw)) {
            return ResponseResult.error("商品不存在或已下架");
        }
        Map<String, Object> goods = castMap(raw);

        Long shopId = legacyLong(goods, "shopId", "shop_id");
        Map<String, Object> shop = new HashMap<>();
        if (shopId != null) {
            try {
                ResponseResult shopResult = mallServiceFeign.frontShopDetail(shopId);
                if (shopResult != null && shopResult.getCode() == 1 && shopResult.getData() instanceof Map<?, ?> shopRaw) {
                    shop = castMap(shopRaw);
                }
            } catch (Exception ignored) {
                // 店铺信息缺失不影响商品可购买
            }
        }

        String image = absolutize(legacyStr(goods, "image", "goods_image"), request);
        List<Map<String, Object>> goodsImages = new ArrayList<>();
        Object imageList = goods.get("images") != null ? goods.get("images") : goods.get("goods_image");
        if (imageList instanceof List<?> rawImages) {
            for (Object item : rawImages) {
                String url = absolutize(item instanceof Map<?, ?> im ? legacyStr(castMap(im), "url", "image", "src")
                        : (item == null ? null : item.toString()), request);
                if (url != null && !url.isBlank()) {
                    Map<String, Object> one = new HashMap<>();
                    one.put("imagesImage", url);
                    goodsImages.add(one);
                }
            }
        }
        if (goodsImages.isEmpty() && image != null && !image.isBlank()) {
            Map<String, Object> one = new HashMap<>();
            one.put("imagesImage", image);
            goodsImages.add(one);
        }

        Map<String, Object> goodsBases = new HashMap<>();
        goodsBases.put("goodsId", goodsId);
        goodsBases.put("shopId", shopId);
        goodsBases.put("shopName", legacyStr(shop, "name", "shopName"));
        goodsBases.put("brandId", 0);
        goodsBases.put("catId", 0);
        // 探测限时折扣（优先）或满减，挂到详情供前端展示与加购
        int commonIsXian = 0;
        Map<String, Object> activityDetail = null;
        String promotionContent = null;
        if (discountQueryFeign != null && goodsId != null) {
            try {
                Map<String, Object> dg = discountQueryFeign.selectByGoodsIdAndTime(goodsId);
                if (dg != null && dg.get("discountPrice") != null) {
                    activityDetail = new HashMap<>();
                    activityDetail.put("activityId", dg.get("discountId"));
                    activityDetail.put("activityStatus", 3);
                    activityDetail.put("activityPrice", dg.get("discountPrice"));
                    activityDetail.put("activityStartTime", dg.get("goodsStartTime"));
                    activityDetail.put("activityEndTime", dg.get("goodsEndTime"));
                    activityDetail.put("discountGoods", dg);
                    commonIsXian = 3;
                    promotionContent = "限时折扣";
                }
            } catch (Exception ignored) {
                // 折扣不可用时继续探测满减
            }
        }
        if (commonIsXian == 0 && reductionQueryFeign != null && goodsId != null) {
            try {
                Map<String, Object> mj = reductionQueryFeign.selectByGoodsId(goodsId);
                if (mj != null && mj.get("promotionId") != null) {
                    Integer promotionId = Integer.valueOf(String.valueOf(mj.get("promotionId")));
                    List<Map<String, Object>> rules = reductionQueryFeign.selectList(promotionId);
                    List<Map<String, Object>> contents = reductionQueryFeign.selectContent(goodsId);
                    activityDetail = new HashMap<>();
                    activityDetail.put("promotionId", promotionId);
                    activityDetail.put("activityId", promotionId);
                    activityDetail.put("activityStatus", 5);
                    activityDetail.put("ruleList", rules == null ? List.of() : rules);
                    activityDetail.put("activityEndTime", mj.get("endTime"));
                    commonIsXian = 5;
                    if (contents != null && !contents.isEmpty()) {
                        Object first = contents.get(0).get("promotionContent");
                        if (first != null) {
                            promotionContent = String.valueOf(first);
                        }
                    }
                }
            } catch (Exception ignored) {
                // 满减插件未装或查询失败时保持普通商品详情
            }
        }
        goodsBases.put("commonIsXian", commonIsXian);
        goodsBases.put("activityGoodsDetailDTO", activityDetail);
        goodsBases.put("goodsName", legacyStr(goods, "name"));
        goodsBases.put("goodsImage", image);
        goodsBases.put("goodsPrice", legacyDecimal(goods, "minPrice", "price"));
        goodsBases.put("goodsMarketPrice", legacyDecimal(goods, "minLinePrice", "linePrice"));
        goodsBases.put("goodsSalenum", legacyInt(goods, "saleNum", "virtualSaleNum"));
        goodsBases.put("goodsStock", legacyInt(goods, "totalStock", "stock"));
        goodsBases.put("commonVideo", legacyStr(goods, "videoUri", "video"));
        goodsBases.put("promotionContent", promotionContent);
        goodsBases.put("goodsPromotionTips", promotionContent);
        // 单规格商品：不出现规格选择行
        goodsBases.put("specValueList", new ArrayList<>());

        Map<String, Object> gdsCommon = new HashMap<>();
        gdsCommon.put("commonId", goodsId);
        // 0 表示虚拟商品（页面会校验有效期），实物固定 1
        gdsCommon.put("virtualGoods", 1);
        gdsCommon.put("periodOfValidity", null);
        gdsCommon.put("specParamList", new ArrayList<>());
        gdsCommon.put("commonSpecValueList", new ArrayList<>());
        gdsCommon.put("dealerGoodsFlag", 0);

        Map<String, Object> shopDetail = new HashMap<>();
        shopDetail.put("shopId", shopId);
        shopDetail.put("shopName", legacyStr(shop, "name", "shopName"));
        shopDetail.put("shopLogo", absolutize(legacyStr(shop, "logo"), request));
        shopDetail.put("wapShopLogo", absolutize(legacyStr(shop, "logo"), request));
        shopDetail.put("shopSelfSupport", 0);
        shopDetail.put("invoiceActive", 0);
        // 0 = 未缴保证金；页面据此隐藏相关提示
        shopDetail.put("depositStatus", 0);
        shopDetail.put("tiosHide", "hide");

        Map<String, Object> data = new HashMap<>();
        data.put("goodsId", goodsId);
        data.put("goodsImage", image);
        data.put("commonImage", image);
        data.put("goodsImages", goodsImages);
        data.put("goodsBases", goodsBases);
        data.put("gdsCommon", gdsCommon);
        data.put("shopDetail", shopDetail);
        return ResponseResult.success(data);
    }

    /**
     * 把后端返回的相对图片路径补成绝对地址。
     *
     * <p>H5 站点挂在 /h5/ 下，相对路径 /pc/demo/x.svg 会被浏览器解析成
     * /h5/pc/demo/x.svg（nginx 兜底返回 index.html），图片就变成一片空白。
     */
    private static String absolutize(String url, HttpServletRequest request) {
        if (url == null || url.isBlank()) {
            return url;
        }
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("//")) {
            return url;
        }
        if (request == null) {
            return url;
        }
        String scheme = headerOr(request, "X-Forwarded-Proto", request.getScheme());
        // 反向代理会把 Host 改写为对外域名；优先用它，避免把内部端口（如 :80）带出去
        String host = headerOr(request, "X-Forwarded-Host", null);
        if (host == null || host.isBlank()) {
            host = headerOr(request, "Host", null);
        }
        if (host == null || host.isBlank()) {
            host = request.getServerName();
            int port = request.getServerPort();
            boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
            if (!defaultPort) {
                host = host + ":" + port;
            }
        }
        if ("https".equals(scheme) && host.endsWith(":80")) {
            host = host.substring(0, host.length() - 3);
        } else if ("http".equals(scheme) && host.endsWith(":443")) {
            host = host.substring(0, host.length() - 4);
        }
        return scheme + "://" + host + (url.startsWith("/") ? url : "/" + url);
    }

    private static String headerOr(HttpServletRequest request, String name, String fallback) {
        String value = request.getHeader(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        int comma = value.indexOf(',');
        return (comma > 0 ? value.substring(0, comma) : value).trim();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> raw) {
        return (Map<String, Object>) raw;
    }

    private static Integer legacyInt(Map<String, Object> param, String... keys) {
        for (String key : keys) {
            Object value = param.get(key);
            if (value instanceof Number number) {
                return number.intValue();
            }
            if (value != null && !value.toString().isBlank()) {
                try {
                    return Integer.valueOf(value.toString().trim());
                } catch (NumberFormatException ignored) {
                    // 继续尝试下一个别名
                }
            }
        }
        return null;
    }

    private static Long legacyLong(Map<String, Object> param, String... keys) {
        for (String key : keys) {
            Object value = param.get(key);
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value != null && !value.toString().isBlank()) {
                try {
                    return Long.valueOf(value.toString().trim());
                } catch (NumberFormatException ignored) {
                    // 继续尝试下一个别名
                }
            }
        }
        return null;
    }

    private static String legacyStr(Map<String, Object> param, String... keys) {
        for (String key : keys) {
            Object value = param.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    private static Object legacyDecimal(Map<String, Object> param, String... keys) {
        for (String key : keys) {
            Object value = param.get(key);
            if (value != null) {
                return value;
            }
        }
        return 0;
    }

    /**
     * 获取商品详情（POST 方式）
/**
     *
     * @param param 包含商品ID的请求体
     * @return ResponseResult 商品详情数据
     */
    @PostMapping("/detail")
    public ResponseResult getGoodsDetail(@RequestBody Map<String, Object> param) {
        Object id = param.get("id");
        if (id != null) {
            return goodsServiceFeign.getGoodsDetail(Long.valueOf(id.toString()));
        }
        return goodsServiceFeign.queryGoodsList(param);
    }

    /**
     * 前台商品评论分页列表（兼容 GET/POST，goods_id + page_no/page_size）
     */
    @RequestMapping(value = "/comment/list", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult frontCommentList(@RequestParam(required = false) Map<String, Object> query,
                                           @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> param = new HashMap<>();
        if (query != null) {
            param.putAll(query);
        }
        if (body != null) {
            param.putAll(body);
        }
        copyAlias(param, "page_no", "page");
        copyAlias(param, "page_size", "limit");
        copyAlias(param, "goodsId", "goods_id");
        return mallServiceFeign.frontCommentList(param);
    }

    // =============== 商品 CRUD 管理 ===============

    /**
     * 创建商品
/**
     *
     * @param param 商品数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/create")
    public ResponseResult createGoods(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createGoods(param);
    }

    /**
     * 更新商品
/**
     *
     * @param param 商品数据（包含商品ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/update")
    public ResponseResult updateGoods(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateGoods(param);
    }

    /**
     * 删除商品
/**
     *
     * @param param 删除参数（包含商品ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/delete")
    public ResponseResult deleteGoods(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteGoods(param);
    }

    /**
     * 更新商品状态（上下架）
/**
     *
     * @param param 状态更新参数（包含商品ID和状态值）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/status")
    public ResponseResult updateGoodsStatus(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateGoodsStatus(param);
    }

    /**
     * 更新商品库存
/**
     *
     * @param param 库存更新参数（包含商品ID和库存数量）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/stock")
    public ResponseResult updateGoodsStock(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateGoodsStock(param);
    }

    // =============== 商品统计 ===============

    /**
     * 获取商品统计数据
/**
     *
     * @return ResponseResult 统计数据
     */
    @PostMapping("/statistics")
    public ResponseResult getGoodsStatistics() {
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
    
    // =============== 分类管理 ===============
    
    /**
     * 获取分类列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 分类列表数据
     */
    @PostMapping("/category/list")
    public ResponseResult getCategoryList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryCategoryList(param);
    }
    
    /**
     * 获取分类树
/**
     *
     * @return ResponseResult 分类树形数据
     */
    @PostMapping("/category/tree")
    public ResponseResult getCategoryTree(@RequestBody(required = false) Map<String, Object> param) {
        return mallServiceFeign.mallAdminGoodsCategoryTree(param == null ? Map.of() : param);
    }
    
    /**
     * 创建分类
/**
     *
     * @param param 分类数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/category/create")
    public ResponseResult createCategory(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createCategory(param);
    }
    
    /**
     * 更新分类
/**
     *
     * @param param 分类数据（包含分类ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/category/update")
    public ResponseResult updateCategory(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateCategory(param);
    }
    
    /**
     * 删除分类
/**
     *
     * @param param 删除参数（包含分类ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/category/delete")
    public ResponseResult deleteCategory(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteCategory(param);
    }
    
    /**
     * 分类分布（统计用）
/**
     *
     * @return ResponseResult 分类分布数据
     */
    @PostMapping("/category/distribution")
    public ResponseResult getCategoryDistribution() {
        return goodsServiceFeign.getCategoryDistribution();
    }

    /**
     * 获取分类商品
/**
     *
     * @param param 查询参数
     * @return ResponseResult 分类商品数据
     */
    @PostMapping("/category")
    public ResponseResult getGoodsByCategory(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryGoodsList(param);
    }
    
    // =============== 品牌管理 ===============
    
    /**
     * 获取品牌列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 品牌列表数据
     */
    @PostMapping("/brand/list")
    public ResponseResult getBrandList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryBrandList(param);
    }
    
    /**
     * 获取所有品牌（不分页）
/**
     *
     * @return ResponseResult 所有品牌列表
     */
    @PostMapping("/brand/all")
    public ResponseResult getAllBrands() {
        return goodsServiceFeign.queryAllBrands();
    }
    
    /**
     * 创建品牌
/**
     *
     * @param param 品牌数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/brand/create")
    public ResponseResult createBrand(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createBrand(param);
    }
    
    /**
     * 更新品牌
/**
     *
     * @param param 品牌数据（包含品牌ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/brand/update")
    public ResponseResult updateBrand(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateBrand(param);
    }
    
    /**
     * 删除品牌
/**
     *
     * @param param 删除参数（包含品牌ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/brand/delete")
    public ResponseResult deleteBrand(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteBrand(param);
    }
    
    // =============== 品牌类型管理 ===============
    
    /**
     * 获取品牌类型列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 品牌类型列表数据
     */
    @PostMapping("/brand-type/list")
    public ResponseResult getBrandTypeList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryBrandTypeList(param);
    }
    
    /**
     * 创建品牌类型
/**
     *
     * @param param 品牌类型数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/brand-type/create")
    public ResponseResult createBrandType(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createBrandType(param);
    }
    
    /**
     * 更新品牌类型
/**
     *
     * @param param 品牌类型数据（包含品牌类型ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/brand-type/update")
    public ResponseResult updateBrandType(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateBrandType(param);
    }
    
    /**
     * 删除品牌类型
/**
     *
     * @param param 删除参数（包含品牌类型ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/brand-type/delete")
    public ResponseResult deleteBrandType(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteBrandType(param);
    }
    
    // =============== 规格管理 ===============
    
    /**
     * 获取规格列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 规格列表数据
     */
    @PostMapping("/spec/list")
    public ResponseResult getSpecList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.querySpecList(param);
    }
    
    /**
     * 创建规格
/**
     *
     * @param param 规格数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/spec/create")
    public ResponseResult createSpec(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createSpec(param);
    }
    
    /**
     * 更新规格
/**
     *
     * @param param 规格数据（包含规格ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/spec/update")
    public ResponseResult updateSpec(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateSpec(param);
    }
    
    /**
     * 删除规格
/**
     *
     * @param param 删除参数（包含规格ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/spec/delete")
    public ResponseResult deleteSpec(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteSpec(param);
    }
    
    // =============== 属性管理 ===============
    
    /**
     * 获取属性列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 属性列表数据
     */
    @PostMapping("/attribute/list")
    public ResponseResult getAttributeList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryAttributeList(param);
    }
    
    /**
     * 创建属性
/**
     *
     * @param param 属性数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/attribute/create")
    public ResponseResult createAttribute(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createAttribute(param);
    }
    
    /**
     * 更新属性
/**
     *
     * @param param 属性数据（包含属性ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/attribute/update")
    public ResponseResult updateAttribute(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateAttribute(param);
    }
    
    /**
     * 删除属性
/**
     *
     * @param param 删除参数（包含属性ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/attribute/delete")
    public ResponseResult deleteAttribute(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteAttribute(param);
    }

    // =============== 属性类型管理 ===============
    
    /**
     * 分页查询属性类型
/**
     *
     * @param param 查询参数
     * @return ResponseResult 属性类型列表数据
     */
    @PostMapping("/attribute-type/page")
    public ResponseResult getAttributeTypePage(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryAttributeTypePage(param);
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
     * @param param 属性类型数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/attribute-type/create")
    public ResponseResult createAttributeType(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createAttributeType(param);
    }
    
    /**
     * 更新属性类型
/**
     *
     * @param param 属性类型数据（包含属性类型ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/attribute-type/update")
    public ResponseResult updateAttributeType(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateAttributeType(param);
    }
    
    /**
     * 删除属性类型
/**
     *
     * @param param 删除参数（包含属性类型ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/attribute-type/delete")
    public ResponseResult deleteAttributeType(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteAttributeType(param);
    }
    
    // =============== 属性组管理 ===============
    
    /**
     * 获取属性组列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 属性组列表数据
     */
    @PostMapping("/attribute-group/list")
    public ResponseResult getAttributeGroupList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryAttributeGroupList(param);
    }
    
    /**
     * 创建属性组
/**
     *
     * @param param 属性组数据
     * @return ResponseResult 创建结果
     */
    @PostMapping("/attribute-group/create")
    public ResponseResult createAttributeGroup(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createAttributeGroup(param);
    }
    
    /**
     * 更新属性组
/**
     *
     * @param param 属性组数据（包含属性组ID）
     * @return ResponseResult 更新结果
     */
    @PostMapping("/attribute-group/update")
    public ResponseResult updateAttributeGroup(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateAttributeGroup(param);
    }
    
    /**
     * 删除属性组
/**
     *
     * @param param 删除参数（包含属性组ID）
     * @return ResponseResult 删除结果
     */
    @PostMapping("/attribute-group/delete")
    public ResponseResult deleteAttributeGroup(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.deleteAttributeGroup(param);
    }

    // =============== 国产替换 ===============

    /**
     * 获取国产替换列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 国产替换列表数据
     */
    @PostMapping("/domestic-replace/list")
    public ResponseResult getDomesticReplaceList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryGoodsList(param);
    }

    /**
     * 获取替代品列表
/**
     *
     * @param param 查询参数
     * @return ResponseResult 替代品列表数据
     */
    @PostMapping("/domestic-replace/replacement-list")
    public ResponseResult getReplacementList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryGoodsList(param);
    }

    // =============== 平台运营补齐：SKU/SPU/批量/导出/上传 ===============

    /**
     * SKU 列表查询（平台运营）
/**
     *
     * @param param 多条件查询参数：commonId/typeId/keyword/page/size 等
     * @return ResponseResult SKU 列表
     */
    @PostMapping("/sku/list")
    public ResponseResult getSkuList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.querySkuList(param);
    }

    /**
     * SKU 信息更新（平台运营）复用通用更新接口
/**
     *
     * @param param SKU 字段集合
     * @return ResponseResult 更新结果
     */
    @PostMapping("/sku/update")
    public ResponseResult updateSku(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateGoodsBaseByMap(param);
    }

    /**
     * SPU 详情查询（平台运营）
/**
     *
     * @param param 请求体，需包含 commonId
     * @return ResponseResult SPU 详情
     */
    @PostMapping("/spu/detail")
    public ResponseResult getSpuDetail(@RequestBody Map<String, Object> param) {
        Object commonId = param == null ? null : param.get("commonId");
        if (commonId == null) {
            commonId = param == null ? null : param.get("id");
        }
        if (commonId == null) {
            return ResponseResult.fail(400, "参数缺失：commonId");
        }
        Integer cid;
        try {
            cid = Integer.valueOf(commonId.toString());
        } catch (NumberFormatException e) {
            return ResponseResult.fail(400, "commonId 格式错误，需为整型");
        }
        return goodsServiceFeign.getCommonDetail(cid);
    }

    /**
     * 批量调整商品价格（平台运营）
/**
     *
     * @param param 批量调价参数
     * @return ResponseResult 批量调价结果
     */
    @PostMapping("/batch/price")
    public ResponseResult batchUpdatePrice(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.batchUpdateGoodsPrice(param);
    }

    /**
     * 批量修改商品状态（上架/下架）（平台运营）
/**
     *
     * @param param 状态批量更新参数
     * @return ResponseResult 更新结果
     */
    @PostMapping("/batch/status")
    public ResponseResult batchUpdateStatus(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateGoodsBaseState(param);
    }

    /**
     * 按商品类型ID查询商品发布信息（平台运营）
/**
     *
     * @param param typeId 及附加查询参数
     * @return ResponseResult 发布信息
     */
    @PostMapping("/publish/typeinfo")
    public ResponseResult getPublishTypeInfo(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryByTypeId(param);
    }

    /**
     * 商品详情列表导出（平台运营）
/**
     *
     * @param param 导出筛选参数
     * @return ResponseResult 导出结果/文件信息
     */
    @PostMapping("/export")
    public ResponseResult exportGoods(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.selectCommodityDetailsExp(param);
    }

    /**
     * 商品详情查询（平台运营，含导出预览）
/**
     *
     * @param param 详情查询参数
     * @return ResponseResult 详情数据
     */
    @PostMapping("/details/select")
    public ResponseResult selectGoodsDetails(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.selectCommodityDetails(param);
    }

    /**
     * OSS 上传（平台运营）
/**
     *
     * @param param OSS 上传参数
     * @return ResponseResult 上传结果
     */
    @PostMapping("/oss/upload")
    public ResponseResult ossUpload(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.ossUpload(param);
    }

    /**
     * OSS 图片记录入库（平台运营）
/**
     *
     * @param param 图片入库参数
     * @return ResponseResult 入库结果
     */
    @PostMapping("/oss/photo/save")
    public ResponseResult savePhoto(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.insertOssPhotoPort(param);
    }
}
