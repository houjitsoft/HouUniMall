package com.houjiang.bffweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.feign.MerchantServiceFeign;
import com.houjiang.bffweb.feign.ThirdpartyBaiduFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BcenterMallGoodsControllerTest {

    @Test
    void addWritesCanonicalGoodsWithCurrentMerchantShopAndConvertedFormFields() {
        MerchantServiceFeign merchantFeign = merchantWith(7, "mall.Goods/add:edit");
        GoodsServiceFeign goodsFeign = mock(GoodsServiceFeign.class);
        when(goodsFeign.mallGoodsSet(anyMap())).thenReturn(ResponseResult.success(14));
        BcenterMallGoodsController controller = controller(merchantFeign, goodsFeign);

        ResponseResult<?> response = controller.add(Map.of(
                "id", 999,
                "name", "茶杯",
                "category_ids", List.of(3, 8),
                "shop_category_id", 21,
                "goods_image", List.of("cover.jpg", "detail.jpg"),
                "virtual_sale_num", 12,
                "virtual_visit_num", 34,
                "sku_name_list", List.of(Map.of("name", "颜色")),
                "sku", List.of(Map.of(
                        "sku_value_ids", List.of(5),
                        "sku_value_arr", List.of("红色"),
                        "price", "9.90",
                        "stock", 6))));

        assertEquals(1, response.getCode());
        assertEquals(14, response.getData());
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(goodsFeign).mallGoodsSet(payload.capture());
        Map<String, Object> sent = payload.getValue();
        assertEquals(7, sent.get("shopId"));
        assertEquals(List.of(3, 8), sent.get("categoryIds"));
        assertEquals(21, sent.get("shopCategoryId"));
        assertEquals(List.of("cover.jpg", "detail.jpg"), sent.get("imageList"));
        assertEquals("cover.jpg", sent.get("image"));
        assertEquals(12, sent.get("virtualSaleNum"));
        assertEquals(34, sent.get("virtualVisitNum"));
        assertEquals(List.of("颜色"), sent.get("skuNameList"));
        assertEquals("[5]", sku(sent).get("skuValueIds"));
        assertEquals("[\"红色\"]", sku(sent).get("skuValueArr"));
        assertTrue(!sent.containsKey("id") && !sent.containsKey("goods_image")
                && !sent.containsKey("sku_name_list") && !sent.containsKey("sku"));
    }

    @Test
    void detailReturnsBcenterFormShapeFromCanonicalGoods() {
        MerchantServiceFeign merchantFeign = merchantWith(7, "mall.Goods/lists");
        GoodsServiceFeign goodsFeign = mock(GoodsServiceFeign.class);
        Map<String, Object> canonical = Map.of(
                "goods", Map.of(
                        "id", 14,
                        "shopId", 7,
                        "name", "茶杯",
                        "shopCategoryId", 21,
                        "auditStatus", 1),
                "categoryIds", List.of(3, 8),
                "imageList", List.of("cover.jpg"),
                "skuNameList", List.of("颜色"),
                "skuList", List.of(Map.of(
                        "id", 31,
                        "skuValueIds", "[5]",
                        "skuValueArr", "[\"红色\"]",
                        "linePrice", "12.00")));
        when(goodsFeign.mallGoodsDetail(14, null)).thenReturn(ResponseResult.success(canonical));
        BcenterMallGoodsController controller = controller(merchantFeign, goodsFeign);

        ResponseResult<?> response = controller.detail(14);

        assertEquals(1, response.getCode());
        Map<String, Object> data = map(response.getData());
        assertEquals(7, data.get("shop_id"));
        assertEquals(21, data.get("shop_category_id"));
        assertEquals(List.of(3, 8), data.get("category_ids"));
        assertEquals(List.of("cover.jpg"), data.get("goods_image"));
        assertEquals("审核通过", data.get("audit_status_text"));
        Map<String, Object> sku = map(((List<?>) data.get("sku")).get(0));
        assertEquals(List.of(5), sku.get("sku_value_ids"));
        assertEquals(List.of("红色"), sku.get("sku_value_arr"));
        assertEquals("12.00", sku.get("line_price"));
        Map<String, Object> skuName = map(((List<?>) data.get("sku_name_list")).get(0));
        assertEquals("颜色", skuName.get("name"));
        assertEquals("红色", map(((List<?>) skuName.get("value")).get(0)).get("value"));
    }

    @Test
    void editRejectsCanonicalGoodsOwnedByAnotherShop() {
        MerchantServiceFeign merchantFeign = merchantWith(7, "mall.Goods/add:edit");
        GoodsServiceFeign goodsFeign = mock(GoodsServiceFeign.class);
        when(goodsFeign.mallGoodsDetail(eq(14), isNull())).thenReturn(ResponseResult.success(
                Map.of("goods", Map.of("id", 14, "shopId", 9))));
        BcenterMallGoodsController controller = controller(merchantFeign, goodsFeign);

        ResponseResult<?> response = controller.edit(Map.of("id", 14, "name", "不可越权修改"));

        assertEquals(0, response.getCode());
        assertEquals("不能操作其他店铺的商品", response.getMessage());
        verify(goodsFeign, never()).mallGoodsSet(anyMap());
    }

    @Test
    void categoryTreeUsesBcenterSonsField() {
        MerchantServiceFeign merchantFeign = merchantWith(7, "mall.Goods/lists");
        GoodsServiceFeign goodsFeign = mock(GoodsServiceFeign.class);
        when(goodsFeign.mallCategoryList(null)).thenReturn(ResponseResult.success(List.of(
                Map.of("id", 1, "name", "家居", "children", List.of(
                        Map.of("id", 2, "name", "杯具", "children", List.of()))))));
        BcenterMallGoodsController controller = controller(merchantFeign, goodsFeign);

        ResponseResult<?> response = controller.categoryLists();

        Map<String, Object> parent = map(((List<?>) response.getData()).get(0));
        assertTrue(!parent.containsKey("children"));
        assertEquals("杯具", map(((List<?>) parent.get("sons")).get(0)).get("name"));
    }

    @Test
    void i18nListReadsTranslationsOnlyAfterCanonicalGoodsOwnershipCheck() {
        MerchantServiceFeign merchantFeign = merchantWith(7, "mall.Goods/add:edit");
        GoodsServiceFeign goodsFeign = mock(GoodsServiceFeign.class);
        when(goodsFeign.mallGoodsDetail(14, null)).thenReturn(ResponseResult.success(
                Map.of("goods", Map.of("id", 14, "shopId", 7))));
        when(goodsFeign.mallGoodsI18nList(14)).thenReturn(ResponseResult.success(List.of(
                Map.of("commonId", 14, "langCode", "en", "commonName", "Tea cup"))));
        BcenterMallGoodsController controller = controller(merchantFeign, goodsFeign);

        ResponseResult<?> response = controller.i18nList(14);

        assertEquals(1, response.getCode());
        assertEquals("Tea cup", map(((List<?>) response.getData()).get(0)).get("commonName"));
        verify(goodsFeign).mallGoodsDetail(14, null);
        verify(goodsFeign).mallGoodsI18nList(14);
    }

    @Test
    void i18nSaveRejectsTranslationsForCanonicalGoodsOwnedByAnotherShop() {
        MerchantServiceFeign merchantFeign = merchantWith(7, "mall.Goods/add:edit");
        GoodsServiceFeign goodsFeign = mock(GoodsServiceFeign.class);
        when(goodsFeign.mallGoodsDetail(14, null)).thenReturn(ResponseResult.success(
                Map.of("goods", Map.of("id", 14, "shopId", 9))));
        BcenterMallGoodsController controller = controller(merchantFeign, goodsFeign);

        ResponseResult<?> response = controller.i18nSave(14, Map.of(
                "items", List.of(Map.of("langCode", "en", "commonName", "Forbidden"))));

        assertEquals(0, response.getCode());
        assertEquals("不能操作其他店铺的商品", response.getMessage());
        verify(goodsFeign, never()).mallGoodsI18nSavePost(anyMap());
    }

    @Test
    void i18nSaveForcesTheOwnedCanonicalGoodsIdFromTheRoute() {
        MerchantServiceFeign merchantFeign = merchantWith(7, "mall.Goods/add:edit");
        GoodsServiceFeign goodsFeign = mock(GoodsServiceFeign.class);
        when(goodsFeign.mallGoodsDetail(14, null)).thenReturn(ResponseResult.success(
                Map.of("goods", Map.of("id", 14, "shopId", 7))));
        when(goodsFeign.mallGoodsI18nSavePost(anyMap())).thenReturn(ResponseResult.success(List.of()));
        BcenterMallGoodsController controller = controller(merchantFeign, goodsFeign);

        ResponseResult<?> response = controller.i18nSave(14, Map.of(
                "commonId", 999,
                "items", List.of(Map.of("langCode", "en", "commonName", "Tea cup"))));

        assertEquals(1, response.getCode());
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(goodsFeign).mallGoodsI18nSavePost(payload.capture());
        assertEquals(14, payload.getValue().get("commonId"));
    }

    @Test
    void autoTranslateSavesAgainstTheSameOwnedCanonicalGoodsId() {
        MerchantServiceFeign merchantFeign = merchantWith(7, "mall.Goods/add:edit");
        GoodsServiceFeign goodsFeign = mock(GoodsServiceFeign.class);
        ThirdpartyBaiduFeign baiduFeign = mock(ThirdpartyBaiduFeign.class);
        when(goodsFeign.mallGoodsDetail(14, null)).thenReturn(ResponseResult.success(
                Map.of("goods", Map.of(
                        "id", 14,
                        "shopId", 7,
                        "name", "茶杯",
                        "descript", "耐热"))));
        when(baiduFeign.trans(anyMap())).thenReturn(ResponseResult.success(Map.of("dst", "Translated")));
        when(goodsFeign.mallGoodsI18nSave(eq(14), anyMap())).thenReturn(ResponseResult.success(List.of()));
        BcenterMallGoodsController controller = controller(merchantFeign, goodsFeign, baiduFeign);

        ResponseResult<?> response = controller.i18nAutoTranslate(14, Map.of(
                "langCodes", List.of("en"), "publish", true));

        assertEquals(1, response.getCode());
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(goodsFeign).mallGoodsI18nSave(eq(14), payload.capture());
        Map<String, Object> item = map(((List<?>) payload.getValue().get("items")).get(0));
        assertEquals("en", item.get("langCode"));
        assertEquals("Translated", item.get("commonName"));
        assertEquals("published", item.get("status"));
        assertEquals(14, payload.getValue().get("commonId"));
    }

    private MerchantServiceFeign merchantWith(Integer shopId, String... access) {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        when(feign.forwardGet("index", "mySelf", Map.of())).thenReturn(Map.of(
                "code", 1,
                "data", Map.of(
                        "account", Map.of("shop_id", shopId),
                        "access", List.of(access))));
        return feign;
    }

    private BcenterMallGoodsController controller(
            MerchantServiceFeign merchantFeign, GoodsServiceFeign goodsFeign) {
        return controller(merchantFeign, goodsFeign, mock(ThirdpartyBaiduFeign.class));
    }

    private BcenterMallGoodsController controller(
            MerchantServiceFeign merchantFeign,
            GoodsServiceFeign goodsFeign,
            ThirdpartyBaiduFeign baiduFeign) {
        BcenterMallGoodsController controller = new BcenterMallGoodsController();
        ReflectionTestUtils.setField(controller, "merchantServiceFeign", merchantFeign);
        ReflectionTestUtils.setField(controller, "goodsServiceFeign", goodsFeign);
        ReflectionTestUtils.setField(controller, "thirdpartyBaiduFeign", baiduFeign);
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        return controller;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    private Map<String, Object> sku(Map<String, Object> payload) {
        return map(((List<?>) payload.get("skuList")).get(0));
    }
}
