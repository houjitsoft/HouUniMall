package com.houjiang.bffweb.controller.mall;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * 店铺页旧 mall-business 接口兼容：旧路径已不存在于 merchant，
 * 改为调用 mall/front/shop 与 mall/goods/lists，并映射为 PC 店铺页期望结构。
 */
final class MallLegacyShopCompat {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MallLegacyShopCompat() {
    }

    static boolean supports(String path) {
        return "shopBase/info/platform/findQueryByShopId".equals(path)
                || "shopBase/info/query/index".equals(path)
                || "shopGoods/listFilter".equals(path);
    }

    static ResponseEntity<String> handle(
            String path,
            byte[] body,
            String mallBaseUrl,
            String goodsBaseUrl,
            RestTemplate http,
            MediaType jsonUtf8) {
        try {
            JsonNode req = body == null || body.length == 0
                    ? MAPPER.createObjectNode()
                    : MAPPER.readTree(body);
            Long shopId = readLong(req, "shopId", "shop_id", "id");
            if (shopId == null) {
                return ResponseEntity.ok().contentType(jsonUtf8)
                        .body("{\"code\":0,\"message\":\"店铺ID不能为空\",\"data\":null}");
            }

            if ("shopBase/info/platform/findQueryByShopId".equals(path)) {
                JsonNode detail = fetchShopDetail(mallBaseUrl, shopId, http);
                if (detail == null) {
                    return ResponseEntity.ok().contentType(jsonUtf8)
                            .body("{\"code\":0,\"message\":\"店铺不存在\",\"data\":null}");
                }
                ObjectNode data = mapHeader(detail, shopId);
                return ok(data, jsonUtf8);
            }

            if ("shopBase/info/query/index".equals(path)) {
                JsonNode detail = fetchShopDetail(mallBaseUrl, shopId, http);
                if (detail == null) {
                    return ResponseEntity.ok().contentType(jsonUtf8)
                            .body("{\"code\":0,\"message\":\"店铺不存在\",\"data\":null}");
                }
                JsonNode categories = fetchShopCategory(mallBaseUrl, shopId, http);
                ObjectNode data = MAPPER.createObjectNode();
                ObjectNode shopBase = mapHeader(detail, shopId);
                shopBase.put("wapShopLogo", text(detail, "logo"));
                shopBase.put("shopCollect", shopBase.path("shopcollect").asInt(0));
                shopBase.put("shopImage", text(detail, "logo"));
                shopBase.putArray("shopSlideurlList");
                data.set("shopBase", shopBase);

                ArrayNode catList = data.putArray("shopSeltCatList");
                if (categories != null && categories.isArray()) {
                    int i = 0;
                    for (JsonNode c : categories) {
                        ObjectNode item = catList.addObject();
                        item.put("shopGoodsCatId", c.path("id").asLong(0));
                        item.put("shopGoodsCatName", text(c, "name"));
                        item.put("shopGoodsCatDisplayorder", i++);
                        item.putArray("children");
                    }
                }
                return ok(data, jsonUtf8);
            }

            // shopGoods/listFilter
            int page = (int) Math.max(1, readLongOrDefault(req, 1, "page", "currentPage", "page_no"));
            int limit = (int) Math.max(1, readLongOrDefault(req, 8, "limit", "page_size", "pageSize"));
            ObjectNode goodsReq = MAPPER.createObjectNode();
            goodsReq.put("shopId", shopId);
            goodsReq.put("page", page);
            goodsReq.put("limit", limit);
            String name = text(req, "commonName", "name", "keyword");
            if (!name.isBlank()) {
                goodsReq.put("name", name);
            }
            String cid = text(req, "shopCatId", "cid", "category_id", "catParentId");
            if (!cid.isBlank()) {
                goodsReq.put("cid", cid);
            }

            JsonNode goodsResp = postJson(goodsBaseUrl + "/mall/goods/lists", goodsReq, http);
            JsonNode payload = goodsResp == null ? null : goodsResp.path("data");
            ArrayNode lists = payload != null && payload.path("lists").isArray()
                    ? (ArrayNode) payload.path("lists")
                    : MAPPER.createArrayNode();

            boolean recommendOnly = readLongOrDefault(req, 0, "commonIsRecommend") == 1;
            ArrayNode mapped = MAPPER.createArrayNode();
            for (JsonNode g : lists) {
                if (recommendOnly && g.path("shopRecommend").asInt(0) != 1) {
                    continue;
                }
                mapped.add(mapGoods(g));
            }

            ObjectNode data = MAPPER.createObjectNode();
            data.set("list", mapped);
            data.set("goodsList", mapped);
            data.put("totalCount", payload == null ? mapped.size() : payload.path("count").asInt(mapped.size()));
            return ok(data, jsonUtf8);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).contentType(jsonUtf8)
                    .body("{\"code\":0,\"message\":\"商城服务暂不可用\",\"data\":null}");
        }
    }

    private static ResponseEntity<String> ok(ObjectNode data, MediaType jsonUtf8) throws Exception {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("code", 1);
        root.put("message", "操作成功");
        root.set("data", data);
        return ResponseEntity.ok().contentType(jsonUtf8).body(MAPPER.writeValueAsString(root));
    }

    private static ObjectNode mapHeader(JsonNode detail, Long shopId) {
        ObjectNode data = MAPPER.createObjectNode();
        data.put("shopId", detail.path("id").asLong(shopId));
        data.put("shopName", text(detail, "name"));
        data.put("shopLogo", text(detail, "logo"));
        data.put("shopcollect", detail.path("fans").asInt(0));
        data.put("shopSelfSupport", detail.path("is_self").asInt(0) == 1);
        data.put("type", detail.path("is_collect").asInt(0) == 1 ? 1 : 2);
        data.put("shopBanner", text(detail, "logo"));
        data.put("qrUrl", "");
        return data;
    }

    private static ObjectNode mapGoods(JsonNode g) {
        ObjectNode item = MAPPER.createObjectNode();
        long id = g.path("id").asLong(0);
        String name = text(g, "name");
        String image = text(g, "image");
        double price = g.path("minPrice").asDouble(g.path("goodsPrice").asDouble(0));
        int sales = g.path("saleNum").asInt(g.path("allSaleNum").asInt(0));
        item.put("goodsId", id);
        item.put("commonId", id);
        item.put("goodsName", name);
        item.put("commonName", name);
        item.put("goodsImage", image);
        item.put("commonImage", image);
        item.put("goodsPrice", price);
        item.put("commonPrice", price);
        item.put("commonSalenum", sales);
        item.put("goodsSalenum", sales);
        item.put("shopId", g.path("shopId").asLong(0));
        return item;
    }

    private static JsonNode fetchShopDetail(String mallBaseUrl, Long shopId, RestTemplate http) {
        String url = mallBaseUrl + "/mall/front/shop/detail?id=" + shopId;
        JsonNode resp = getJson(url, http);
        if (resp == null || resp.path("code").asInt(0) != 1) {
            return null;
        }
        JsonNode data = resp.path("data");
        return data.isMissingNode() || data.isNull() ? null : data;
    }

    private static JsonNode fetchShopCategory(String mallBaseUrl, Long shopId, RestTemplate http) {
        String url = mallBaseUrl + "/mall/front/shop/category?id=" + shopId;
        JsonNode resp = getJson(url, http);
        if (resp == null || resp.path("code").asInt(0) != 1) {
            return MAPPER.createArrayNode();
        }
        return resp.path("data");
    }

    private static JsonNode getJson(String url, RestTemplate http) {
        ResponseEntity<byte[]> response = http.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, byte[].class);
        return parse(response.getBody());
    }

    private static JsonNode postJson(String url, ObjectNode body, RestTemplate http) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<byte[]> response = http.exchange(
                url, HttpMethod.POST, new HttpEntity<>(MAPPER.writeValueAsBytes(body), headers), byte[].class);
        return parse(response.getBody());
    }

    private static JsonNode parse(byte[] body) {
        try {
            if (body == null || body.length == 0) {
                return null;
            }
            return MAPPER.readTree(new String(body, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    private static Long readLong(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode v = node.get(key);
            if (v != null && !v.isNull() && !v.asText("").isBlank()) {
                try {
                    return Long.valueOf(v.asText());
                } catch (NumberFormatException ignored) {
                    if (v.isNumber()) {
                        return v.asLong();
                    }
                }
            }
        }
        return null;
    }

    private static long readLongOrDefault(JsonNode node, long defaultValue, String... keys) {
        Long v = readLong(node, keys);
        return v == null ? defaultValue : v;
    }

    private static String text(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode v = node.get(key);
            if (v != null && !v.isNull()) {
                String s = v.asText("");
                if (!s.isBlank()) {
                    return s;
                }
            }
        }
        return "";
    }
}
