package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.feign.ThirdpartyBaiduFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品多语言 BFF：/web/goods/i18n/*
 * 维护端：商家为主；平台可查看。缺译由前台机翻兜底。
 */
@RestController
@RequestMapping("/web/goods/i18n")
public class WebGoodsI18nController {

    @Autowired
    private GoodsServiceFeign goodsServiceFeign;

    @Autowired
    private ThirdpartyBaiduFeign thirdpartyBaiduFeign;

    /** GET /web/goods/i18n/{commonId} — 各语种草稿+已发布 */
    @GetMapping("/{commonId}")
    public ResponseResult list(@PathVariable("commonId") Integer commonId) {
        return goodsServiceFeign.mallGoodsI18nList(commonId);
    }

    /** PUT /web/goods/i18n/{commonId} — 批量保存 */
    @PutMapping("/{commonId}")
    public ResponseResult save(@PathVariable("commonId") Integer commonId,
                               @RequestBody Map<String, Object> body) {
        if (body == null) {
            body = new HashMap<>();
        }
        body.put("commonId", commonId);
        return goodsServiceFeign.mallGoodsI18nSave(commonId, body);
    }

    /** POST 保存（bcenter 等无 PUT 客户端） */
    @PostMapping("/{commonId}/save")
    public ResponseResult savePost(@PathVariable("commonId") Integer commonId,
                                   @RequestBody Map<String, Object> body) {
        if (body == null) {
            body = new HashMap<>();
        }
        body.put("commonId", commonId);
        return goodsServiceFeign.mallGoodsI18nSavePost(body);
    }

    /**
     * POST /web/goods/i18n/{commonId}/auto-translate
     * body: { langCodes: ["en","ja"], publish: true }
     */
    @PostMapping("/{commonId}/auto-translate")
    @SuppressWarnings("unchecked")
    public ResponseResult autoTranslate(@PathVariable("commonId") Integer commonId,
                                        @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            body = new HashMap<>();
        }
        Object langsObj = body.get("langCodes");
        List<String> langCodes = new ArrayList<>();
        if (langsObj instanceof List<?> list) {
            for (Object o : list) {
                if (o != null && StringUtils.hasText(o.toString())) {
                    langCodes.add(o.toString().trim().toLowerCase());
                }
            }
        }
        if (langCodes.isEmpty()) {
            langCodes.add("en");
        }
        boolean publish = body.get("publish") == null || Boolean.parseBoolean(body.get("publish").toString());

        ResponseResult detailRes = goodsServiceFeign.mallGoodsDetail(commonId, null);
        if (detailRes == null || detailRes.getCode() != 1 || detailRes.getData() == null) {
            return ResponseResult.error("商品不存在或不可用");
        }
        Map<String, Object> root = asMap(detailRes.getData());
        Map<String, Object> goods = asMap(root.get("goods"));
        if (goods.isEmpty()) {
            goods = root;
        }
        String name = str(goods.get("name"));
        String tips = str(goods.get("descript"));
        if (!StringUtils.hasText(name)) {
            return ResponseResult.error("中文商品名称不能为空");
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (String lang : langCodes) {
            if (isCn(lang)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("langCode", lang);
            item.put("commonName", translate(name, lang));
            if (StringUtils.hasText(tips)) {
                item.put("promotionTips", translate(tips, lang));
            }
            item.put("source", "ai");
            item.put("status", publish ? "published" : "draft");
            items.add(item);
        }
        Map<String, Object> saveBody = new HashMap<>();
        saveBody.put("commonId", commonId);
        saveBody.put("items", items);
        return goodsServiceFeign.mallGoodsI18nSave(commonId, saveBody);
    }

    private String translate(String src, String to) {
        Map<String, String> req = new HashMap<>();
        req.put("src", src);
        req.put("from", "zh");
        req.put("to", mapBaiduLang(to));
        try {
            ResponseResult res = thirdpartyBaiduFeign.trans(req);
            if (res != null && res.getData() instanceof Map<?, ?> data) {
                Object dst = data.get("dst");
                if (dst != null && StringUtils.hasText(dst.toString())) {
                    return dst.toString();
                }
            }
        } catch (Exception ignored) {
            // 机翻失败时保留原文，后台可再校正
        }
        return src;
    }

    private String mapBaiduLang(String code) {
        if (code == null) return "en";
        return switch (code.toLowerCase()) {
            case "ja" -> "jp";
            case "ko" -> "kor";
            case "zh", "cn", "zh-cn", "zh_cn" -> "zh";
            default -> code.toLowerCase();
        };
    }

    private boolean isCn(String lang) {
        String l = lang.trim().toLowerCase();
        return "cn".equals(l) || "zh".equals(l) || "zh-cn".equals(l) || "zh_cn".equals(l);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return new HashMap<>();
    }

    private String str(Object o) {
        return o == null ? "" : o.toString().trim();
    }
}
