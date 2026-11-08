package com.houjiang.gift.route;

import com.houjiang.gift.domain.GiftItem;
import com.houjiang.gift.domain.GiftRule;
import com.houjiang.gift.service.GiftService;
import com.houjiang.plugin.sdk.annotation.HouRoute;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台满赠配置接口。
 */
@Component
public class GiftAdminRoute {

    private final GiftService service;

    public GiftAdminRoute(GiftService service) {
        this.service = service;
    }

    /** 规则列表 + 概览。 */
    @HouRoute(path = "/admin/rules", method = "GET", permission = "order-gift:root",
            allowCallers = {"host"}, description = "满赠规则列表")
    public Map<String, Object> list() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", service.list());
        result.put("stats", service.stats());
        return result;
    }

    /** 保存规则（含赠品池整组覆盖）。 */
    @HouRoute(path = "/admin/rules", method = "POST", permission = "order-gift:root",
            allowCallers = {"host"}, description = "保存满赠规则")
    @SuppressWarnings("unchecked")
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {
        List<GiftItem> items = new ArrayList<>();
        Object rawItems = body.get("items");
        if (rawItems instanceof List<?> rows) {
            for (Object row : rows) {
                if (!(row instanceof Map<?, ?> map)) {
                    continue;
                }
                items.add(new GiftItem(
                        0L, 0L,
                        strOf(map.get("goodsId")),
                        strOf(map.get("skuId")),
                        strOf(map.get("giftTitle")),
                        intOf(map.get("giftQty"), 1),
                        intOf(map.get("stockTotal"), 0),
                        intOf(map.get("stockUsed"), 0),
                        intOf(map.get("sort"), 0)));
            }
        }
        long id = service.save(new GiftRule(
                longOf(body.get("id"), 0L),
                strOf(body.get("ruleName")),
                strOf(body.get("scopeType")),
                stringList(body.get("scopeIds")),
                decimalOf(body.get("thresholdAmount")),
                strOf(body.get("giftMode")),
                intOf(body.get("maxGiftKinds"), 1),
                intOf(body.get("status"), 1),
                intOf(body.get("priority"), 0),
                dateOf(body.get("startTime")),
                dateOf(body.get("endTime")),
                strOf(body.get("remark")),
                items));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("list", service.list());
        result.put("stats", service.stats());
        return result;
    }

    /** 删除规则。 */
    @HouRoute(path = "/admin/rules/delete", method = "POST", permission = "order-gift:root",
            allowCallers = {"host"}, description = "删除满赠规则")
    public Map<String, Object> delete(@RequestParam("id") long id) {
        service.delete(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", service.list());
        result.put("stats", service.stats());
        return result;
    }

    /** 赠出记录分页。 */
    @HouRoute(path = "/admin/records", method = "GET", permission = "order-gift:root",
            allowCallers = {"host"}, description = "满赠赠出记录")
    public Map<String, Object> records(@RequestParam(value = "page", defaultValue = "1") int page,
                                       @RequestParam(value = "size", defaultValue = "20") int size,
                                       @RequestParam(value = "orderNo", required = false) String orderNo,
                                       @RequestParam(value = "userId", required = false) String userId) {
        return service.pageRecords(orderNo, userId, page, size);
    }

    /** 后台预检：输入金额看能送什么，不用真下单。 */
    @HouRoute(path = "/admin/evaluate", method = "POST", permission = "order-gift:root",
            allowCallers = {"host"}, description = "满赠预检")
    public Map<String, Object> evaluate(@RequestBody Map<String, Object> body) {
        return service.evaluate(decimalOf(body.get("amount")),
                stringList(body.get("goodsIds")), stringList(body.get("categoryIds")));
    }

    private static List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    result.add(String.valueOf(item).trim());
                }
            }
        } else if (value != null && !String.valueOf(value).isBlank()) {
            for (String piece : String.valueOf(value).split(",")) {
                if (!piece.isBlank()) {
                    result.add(piece.trim());
                }
            }
        }
        return result;
    }

    private static long longOf(Object value, long fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return Long.parseLong(String.valueOf(value).trim());
    }

    private static int intOf(Object value, int fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim().replace(".0", ""));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("数值格式不正确: " + value);
        }
    }

    private static BigDecimal decimalOf(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return new BigDecimal(String.valueOf(value).trim());
    }

    private static String strOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static LocalDateTime dateOf(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        String text = String.valueOf(value).trim().replace('T', ' ');
        if (text.length() == 10) {
            text = text + " 00:00:00";
        }
        return LocalDateTime.parse(text.replace(' ', 'T'));
    }
}
