package com.houjiang.bffweb.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.feign.MerchantServiceFeign;
import com.houjiang.bffweb.feign.ThirdpartyBaiduFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * bcenter 商品适配层。
 *
 * <p>保留 LikeShop 的 /web/merchant/mall.Goods/* 契约，但主商品统一读写 goods
 * 服务的 hou_mall_goods。这样商品主键与 hou_goods_common_i18n.common_id 始终同源。
 */
@RestController
@RequestMapping("/web/merchant")
public class BcenterMallGoodsController {

    private static final List<String> BCENTER_I18N_LANGUAGES =
            List.of("en", "ja", "ko", "fr", "de", "es");

    @Autowired
    private MerchantServiceFeign merchantServiceFeign;

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    @Autowired
    private ThirdpartyBaiduFeign thirdpartyBaiduFeign;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/mall.Goods/lists")
    public ResponseResult<?> lists(@RequestParam(required = false) Map<String, Object> request) {
        MerchantContext merchant = currentMerchant("mall.Goods/lists");
        if (!merchant.isAllowed()) return merchant.failure();
        Map<String, Object> params = normalizeRequest(request);
        params.put("shopId", merchant.getShopId());
        ResponseResult<?> response = goodsServiceFeign.mallGoodsList(params);
        if (!isSuccess(response)) return response;
        return ResponseResult.success(toBcenterPage(response.getData()));
    }

    @GetMapping("/mall.Goods/detail")
    public ResponseResult<?> detail(@RequestParam("id") Integer id) {
        MerchantContext merchant = currentMerchant("mall.Goods/lists", "mall.Goods/add:edit");
        if (!merchant.isAllowed()) return merchant.failure();
        ResponseResult<?> response = assertOwnedByShop(id, merchant.getShopId());
        if (!isSuccess(response)) return response;
        return ResponseResult.success(toBcenterDetail(response.getData()));
    }

    @PostMapping("/mall.Goods/add")
    public ResponseResult<?> add(@RequestBody(required = false) Map<String, Object> request) {
        MerchantContext merchant = currentMerchant("mall.Goods/add:edit");
        if (!merchant.isAllowed()) return merchant.failure();
        Map<String, Object> params = normalizeRequest(request);
        params.remove("id");
        params.put("shopId", merchant.getShopId());
        return goodsServiceFeign.mallGoodsSet(params);
    }

    @PostMapping("/mall.Goods/edit")
    public ResponseResult<?> edit(@RequestBody(required = false) Map<String, Object> request) {
        MerchantContext merchant = currentMerchant("mall.Goods/add:edit");
        if (!merchant.isAllowed()) return merchant.failure();
        Map<String, Object> params = normalizeRequest(request);
        Integer id = integerValue(params.get("id"));
        ResponseResult<?> owned = assertOwnedByShop(id, merchant.getShopId());
        if (!isSuccess(owned)) return owned;
        params.put("shopId", merchant.getShopId());
        return goodsServiceFeign.mallGoodsSet(params);
    }

    @PostMapping("/mall.Goods/status")
    public ResponseResult<?> status(@RequestBody(required = false) Map<String, Object> request) {
        MerchantContext merchant = currentMerchant("mall.Goods/add:edit");
        if (!merchant.isAllowed()) return merchant.failure();
        Map<String, Object> params = normalizeRequest(request);
        ResponseResult<?> owned = assertOwnedByShop(integerValue(params.get("id")), merchant.getShopId());
        if (!isSuccess(owned)) return owned;
        return goodsServiceFeign.mallGoodsChangeStatus(params);
    }

    @PostMapping("/mall.Goods/delete")
    public ResponseResult<?> delete(@RequestBody(required = false) Map<String, Object> request) {
        MerchantContext merchant = currentMerchant("mall.Goods/add:edit");
        if (!merchant.isAllowed()) return merchant.failure();
        Map<String, Object> params = normalizeRequest(request);
        ResponseResult<?> owned = assertOwnedByShop(integerValue(params.get("id")), merchant.getShopId());
        if (!isSuccess(owned)) return owned;
        return goodsServiceFeign.mallGoodsDel(params);
    }

    /** bcenter 平台分类也从 canonical Mall 商品分类读取。 */
    @GetMapping("/mall.goods/category_lists")
    public ResponseResult<?> categoryLists() {
        MerchantContext merchant = currentMerchant(
                "mall.Goods/lists", "mall.Goods/add:edit",
                "gather.gather/detial", "application.distribution/goods");
        if (!merchant.isAllowed()) return merchant.failure();
        ResponseResult<?> response = goodsServiceFeign.mallCategoryList(null);
        if (!isSuccess(response)) return response;
        return ResponseResult.success(toBcenterCategories(response.getData()));
    }

    /** bcenter 商品多语言列表：commonId 必须是当前店铺的 canonical 商品 ID。 */
    @GetMapping("/mall.Goods/i18n/{commonId}")
    public ResponseResult<?> i18nList(@PathVariable("commonId") Integer commonId) {
        MerchantContext merchant = currentMerchant("mall.Goods/lists", "mall.Goods/add:edit");
        if (!merchant.isAllowed()) return merchant.failure();
        ResponseResult<?> owned = assertOwnedByShop(commonId, merchant.getShopId());
        if (!isSuccess(owned)) return owned;
        return goodsServiceFeign.mallGoodsI18nList(commonId);
    }

    /** bcenter 商品多语言保存：只允许写入当前店铺的 canonical 商品。 */
    @PostMapping("/mall.Goods/i18n/{commonId}/save")
    public ResponseResult<?> i18nSave(@PathVariable("commonId") Integer commonId,
                                      @RequestBody(required = false) Map<String, Object> request) {
        MerchantContext merchant = currentMerchant("mall.Goods/add:edit");
        if (!merchant.isAllowed()) return merchant.failure();
        ResponseResult<?> owned = assertOwnedByShop(commonId, merchant.getShopId());
        if (!isSuccess(owned)) return owned;
        Map<String, Object> body = request == null ? new HashMap<>() : new HashMap<>(request);
        body.put("commonId", commonId);
        return goodsServiceFeign.mallGoodsI18nSavePost(body);
    }

    /** bcenter 一键翻译：读取与保存始终使用同一个、且属于当前店铺的 canonical 商品 ID。 */
    @PostMapping("/mall.Goods/i18n/{commonId}/auto-translate")
    public ResponseResult<?> i18nAutoTranslate(
            @PathVariable("commonId") Integer commonId,
            @RequestBody(required = false) Map<String, Object> request) {
        MerchantContext merchant = currentMerchant("mall.Goods/add:edit");
        if (!merchant.isAllowed()) return merchant.failure();
        ResponseResult<?> detail = assertOwnedByShop(commonId, merchant.getShopId());
        if (!isSuccess(detail)) return detail;

        Map<String, Object> body = request == null ? Map.of() : request;
        List<String> langCodes = i18nLanguageCodes(body.get("langCodes"));
        boolean publish = body.get("publish") == null
                || Boolean.parseBoolean(body.get("publish").toString());
        Map<String, Object> root = asMap(detail.getData());
        Map<String, Object> goods = asMap(root.get("goods"));
        if (goods.isEmpty()) goods = root;
        String name = stringValue(goods.get("name"));
        String tips = stringValue(goods.get("descript"));
        if (!StringUtils.hasText(name)) {
            return ResponseResult.error("中文商品名称不能为空");
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (String lang : langCodes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("langCode", lang);
            item.put("commonName", translate(name, lang));
            if (StringUtils.hasText(tips)) item.put("promotionTips", translate(tips, lang));
            item.put("source", "ai");
            item.put("status", publish ? "published" : "draft");
            items.add(item);
        }
        Map<String, Object> saveBody = new LinkedHashMap<>();
        saveBody.put("commonId", commonId);
        saveBody.put("items", items);
        return goodsServiceFeign.mallGoodsI18nSave(commonId, saveBody);
    }

    private ResponseResult<?> assertOwnedByShop(Integer id, Integer shopId) {
        if (id == null) return ResponseResult.error("商品ID不能为空");
        ResponseResult<?> detail = goodsServiceFeign.mallGoodsDetail(id, null);
        if (!isSuccess(detail)) return detail;
        Map<String, Object> root = asMap(detail.getData());
        Map<String, Object> goods = asMap(root.get("goods"));
        if (goods.isEmpty()) goods = root;
        if (!Objects.equals(integerValue(first(goods, "shopId", "shop_id")), shopId)) {
            return ResponseResult.error("不能操作其他店铺的商品");
        }
        return detail;
    }

    private MerchantContext currentMerchant(String... anyOfPermissions) {
        Object raw = merchantServiceFeign.forwardGet("index", "mySelf", new HashMap<>());
        Map<String, Object> response = asMap(raw);
        if (!Objects.equals(integerValue(response.get("code")), 1)) {
            return new MerchantContext(null, false, false);
        }
        Map<String, Object> data = asMap(response.get("data"));
        Map<String, Object> account = asMap(data.get("account"));
        Integer shopId = integerValue(first(account, "shop_id", "shopId"));
        if (shopId == null) return new MerchantContext(null, false, false);
        Object rawAccess = data.get("access");
        if (!(rawAccess instanceof List<?> access)) {
            return new MerchantContext(shopId, true, false);
        }
        if (access.contains("*")) return new MerchantContext(shopId, true, true);
        for (String permission : anyOfPermissions) {
            if (access.contains(permission)) return new MerchantContext(shopId, true, true);
        }
        return new MerchantContext(shopId, true, false);
    }

    private ResponseResult<?> loginFailure() {
        return ResponseResult.error("登录状态已失效，请重新登录");
    }

    private ResponseResult<?> permissionFailure() {
        return ResponseResult.error("无商品管理权限");
    }

    private boolean isSuccess(ResponseResult<?> response) {
        return response != null && response.getCode() == 1 && response.getData() != null;
    }

    private Map<String, Object> normalizeRequest(Map<String, Object> source) {
        Map<String, Object> result = source == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source);
        moveAlias(result, "page_no", "pageNo");
        moveAlias(result, "page_size", "pageSize");
        moveAlias(result, "category_id", "categoryId");
        moveAlias(result, "shop_category_id", "shopCategoryId");
        moveAlias(result, "category_ids", "categoryIds");
        moveAlias(result, "video_is", "videoIs");
        moveAlias(result, "video_from", "videoFrom");
        moveAlias(result, "video_cover", "videoCover");
        moveAlias(result, "video_uri", "videoUri");
        moveAlias(result, "sku_type", "skuType");
        moveAlias(result, "express_type", "expressType");
        moveAlias(result, "express_tpl_id", "expressTplId");
        moveAlias(result, "express_money", "expressMoney");
        moveAlias(result, "stock_warning", "stockWarning");
        moveAlias(result, "virtual_sale_num", "virtualSaleNum");
        moveAlias(result, "virtual_visit_num", "virtualVisitNum");
        moveAlias(result, "shop_recommend", "shopRecommend");
        moveAlias(result, "is_discount", "isDiscount");
        moveAlias(result, "discount_type", "discountType");
        moveAlias(result, "is_distribution", "isDistribution");
        moveAlias(result, "distribution_type", "distributionType");

        Object images = first(result, "goods_image", "godos_image", "imageList");
        if (images instanceof List<?> list) {
            result.put("imageList", list);
            if (!list.isEmpty()) result.put("image", list.get(0));
        }
        result.remove("goods_image");
        result.remove("godos_image");

        Object skuNames = first(result, "sku_name_list", "skuNameList");
        if (skuNames instanceof List<?> list) {
            List<String> names = new ArrayList<>();
            for (Object entry : list) {
                if (entry instanceof Map<?, ?> map) {
                    Object name = map.get("name");
                    if (name != null) names.add(name.toString());
                } else if (entry != null) {
                    names.add(entry.toString());
                }
            }
            result.put("skuNameList", names);
        }
        result.remove("sku_name_list");

        Object sku = first(result, "sku", "skuList");
        if (sku instanceof List<?> list) {
            List<Map<String, Object>> normalized = new ArrayList<>();
            for (Object entry : list) {
                Map<String, Object> item = new LinkedHashMap<>(asMap(entry));
                moveAlias(item, "sku_value_ids", "skuValueIds");
                moveAlias(item, "sku_value_arr", "skuValueArr");
                moveAlias(item, "line_price", "linePrice");
                moveAlias(item, "market_price", "marketPrice");
                item.computeIfPresent("skuValueIds", (key, value) -> jsonString(value));
                item.computeIfPresent("skuValueArr", (key, value) -> jsonString(value));
                normalized.add(item);
            }
            result.put("skuList", normalized);
        }
        result.remove("sku");
        result.remove("goods_send_mode");
        result.putIfAbsent("isExpress", 1);
        return result;
    }

    private Map<String, Object> toBcenterPage(Object data) {
        Map<String, Object> page = new LinkedHashMap<>(asMap(data));
        aliasValue(page, "pageNo", "page_no");
        aliasValue(page, "pageSize", "page_size");
        Object rows = page.get("lists");
        if (rows instanceof List<?> list) {
            List<Map<String, Object>> mapped = new ArrayList<>();
            for (Object row : list) mapped.add(withSnakeAliases(asMap(row)));
            page.put("lists", mapped);
        }
        return page;
    }

    private Map<String, Object> toBcenterDetail(Object data) {
        Map<String, Object> root = asMap(data);
        Map<String, Object> goods = new LinkedHashMap<>(asMap(root.get("goods")));
        if (goods.isEmpty()) goods.putAll(root);
        Map<String, Object> result = withSnakeAliases(goods);
        Object categoryIds = first(root, "categoryIds", "category_ids");
        result.put("category_ids", categoryIds == null ? List.of() : categoryIds);
        Object images = first(root, "imageList", "image_list");
        result.put("goods_image", images == null ? List.of() : images);

        List<Map<String, Object>> skus = new ArrayList<>();
        Object skuRows = first(root, "skuList", "sku_list");
        if (skuRows instanceof List<?> list) {
            for (Object row : list) {
                Map<String, Object> item = withSnakeAliases(asMap(row));
                item.put("sku_value_ids", jsonValue(first(item, "skuValueIds", "sku_value_ids")));
                item.put("sku_value_arr", jsonValue(first(item, "skuValueArr", "sku_value_arr")));
                skus.add(item);
            }
        }
        result.put("sku", skus);
        result.put("sku_name_list", buildSkuNames(root, skus));
        result.put("goods_send_mode", List.of(0));
        result.put("audit_status_text", auditStatusText(integerValue(first(goods, "auditStatus", "audit_status"))));
        return result;
    }

    private List<Map<String, Object>> toBcenterCategories(Object data) {
        if (!(data instanceof List<?> categories)) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object category : categories) {
            Map<String, Object> item = withSnakeAliases(asMap(category));
            Object children = first(item, "children", "sons");
            item.put("sons", toBcenterCategories(children));
            item.remove("children");
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildSkuNames(Map<String, Object> root, List<Map<String, Object>> skus) {
        Object rawNames = first(root, "skuNameList", "sku_name_list");
        if (!(rawNames instanceof List<?> names)) return List.of();
        List<Map<String, Object>> result = new ArrayList<>();
        for (int index = 0; index < names.size(); index++) {
            Object nameEntry = names.get(index);
            String name = nameEntry instanceof Map<?, ?> map
                    ? Objects.toString(map.get("name"), "") : Objects.toString(nameEntry, "");
            List<Map<String, Object>> values = new ArrayList<>();
            List<String> seen = new ArrayList<>();
            for (Map<String, Object> sku : skus) {
                Object rawValues = sku.get("sku_value_arr");
                if (rawValues instanceof List<?> list && index < list.size()) {
                    String value = Objects.toString(list.get(index), "");
                    if (!value.isBlank() && !seen.contains(value)) {
                        seen.add(value);
                        values.add(new LinkedHashMap<>(Map.of("value", value, "image", "")));
                    }
                }
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", name);
            item.put("has_image", 0);
            item.put("value", values);
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> withSnakeAliases(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>(source);
        aliasValue(result, "createTime", "create_time");
        aliasValue(result, "updateTime", "update_time");
        aliasValue(result, "deleteTime", "delete_time");
        aliasValue(result, "shopId", "shop_id");
        aliasValue(result, "categoryIds", "category_ids");
        aliasValue(result, "shopCategoryId", "shop_category_id");
        aliasValue(result, "videoIs", "video_is");
        aliasValue(result, "videoFrom", "video_from");
        aliasValue(result, "videoCover", "video_cover");
        aliasValue(result, "videoUri", "video_uri");
        aliasValue(result, "skuType", "sku_type");
        aliasValue(result, "expressType", "express_type");
        aliasValue(result, "expressTplId", "express_tpl_id");
        aliasValue(result, "expressMoney", "express_money");
        aliasValue(result, "totalStock", "total_stock");
        aliasValue(result, "stockWarning", "stock_warning");
        aliasValue(result, "minPrice", "min_price");
        aliasValue(result, "maxPrice", "max_price");
        aliasValue(result, "saleNum", "sale_num");
        aliasValue(result, "virtualSaleNum", "virtual_sale_num");
        aliasValue(result, "virtualVisitNum", "virtual_visit_num");
        aliasValue(result, "auditStatus", "audit_status");
        aliasValue(result, "auditContent", "audit_content");
        aliasValue(result, "shopRecommend", "shop_recommend");
        aliasValue(result, "isRecycle", "is_recycle");
        aliasValue(result, "isDiscount", "is_discount");
        aliasValue(result, "discountType", "discount_type");
        aliasValue(result, "isDistribution", "is_distribution");
        aliasValue(result, "distributionType", "distribution_type");
        aliasValue(result, "priceText", "price_text");
        aliasValue(result, "linePrice", "line_price");
        aliasValue(result, "marketPrice", "market_price");
        return result;
    }

    private void moveAlias(Map<String, Object> map, String from, String to) {
        if (map.containsKey(from) && !map.containsKey(to)) map.put(to, map.get(from));
        if (!from.equals(to)) map.remove(from);
    }

    private void aliasValue(Map<String, Object> map, String from, String to) {
        if (map.containsKey(from)) map.put(to, map.get(from));
    }

    private Object first(Map<String, Object> map, String... keys) {
        for (String key : keys) if (map.containsKey(key) && map.get(key) != null) return map.get(key);
        return null;
    }

    private String jsonString(Object value) {
        if (value == null || value instanceof String) return Objects.toString(value, null);
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("商品规格格式错误", e);
        }
    }

    private Object jsonValue(Object value) {
        if (!(value instanceof String text) || text.isBlank()) return value == null ? List.of() : value;
        try {
            return objectMapper.readValue(text, List.class);
        } catch (JsonProcessingException ignored) {
            return List.of();
        }
    }

    private Integer integerValue(Object value) {
        if (value == null || value.toString().isBlank()) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String auditStatusText(Integer status) {
        if (Objects.equals(status, 1)) return "审核通过";
        if (Objects.equals(status, 2)) return "审核拒绝";
        return "待审核";
    }

    private List<String> i18nLanguageCodes(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                String lang = stringValue(item).toLowerCase();
                if (BCENTER_I18N_LANGUAGES.contains(lang) && !result.contains(lang)) result.add(lang);
            }
        }
        if (result.isEmpty()) result.add("en");
        return result;
    }

    private String translate(String source, String lang) {
        Map<String, String> request = new HashMap<>();
        request.put("src", source);
        request.put("from", "zh");
        request.put("to", baiduLanguageCode(lang));
        try {
            ResponseResult<?> response = thirdpartyBaiduFeign.trans(request);
            Map<String, Object> data = response == null ? Map.of() : asMap(response.getData());
            String translated = stringValue(data.get("dst"));
            return StringUtils.hasText(translated) ? translated : source;
        } catch (Exception ignored) {
            return source;
        }
    }

    private String baiduLanguageCode(String lang) {
        return switch (lang) {
            case "ja" -> "jp";
            case "ko" -> "kor";
            default -> lang;
        };
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : new LinkedHashMap<>();
    }

    private final class MerchantContext {
        private final Integer shopId;
        private final boolean authenticated;
        private final boolean allowed;

        private MerchantContext(Integer shopId, boolean authenticated, boolean allowed) {
            this.shopId = shopId;
            this.authenticated = authenticated;
            this.allowed = allowed;
        }

        private Integer getShopId() {
            return shopId;
        }

        private boolean isAllowed() {
            return allowed;
        }

        private ResponseResult<?> failure() {
            return authenticated ? permissionFailure() : loginFailure();
        }

    }
}
