package com.houjiang.gift.route;

import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.gift.service.GiftService;
import com.houjiang.plugin.sdk.annotation.HouRoute;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * C 端满赠接口：结算页取可赠礼、下单后登记赠出。
 *
 * <p>身份取会话，不接受请求体里的 userId。
 */
@Component
public class GiftFrontRoute {

    private final GiftService service;

    public GiftFrontRoute(GiftService service) {
        this.service = service;
    }

    /** 结算页：传订单商品金额与商品/类目集合，返回可赠规则与赠品。 */
    @HouRoute(path = "/front/evaluate", method = "POST", permission = "", allowCallers = {"host"},
            description = "满赠可赠查询")
    public Map<String, Object> evaluate(@RequestBody Map<String, Object> body) {
        return service.evaluate(
                body.get("amount") == null || String.valueOf(body.get("amount")).isBlank()
                        ? null : new BigDecimal(String.valueOf(body.get("amount")).trim()),
                stringList(body.get("goodsIds")),
                stringList(body.get("categoryIds")));
    }

    /** 下单成功后登记赠出（幂等键 orderNo + itemId）。 */
    @HouRoute(path = "/front/claim", method = "POST", permission = "", allowCallers = {"host"},
            description = "登记赠出")
    public Map<String, Object> claim(@RequestBody Map<String, Object> body) {
        String userId = SecurityContextUtils.getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("请先登录");
        }
        long ruleId = longOf(body.get("ruleId"));
        long itemId = longOf(body.get("itemId"));
        int qty = intOf(body.get("qty"));
        return service.claim(strOf(body.get("orderNo")), userId.trim(), ruleId, itemId, qty);
    }

    private static List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    result.add(String.valueOf(item).trim());
                }
            }
        }
        return result;
    }

    private static long longOf(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("缺少必要参数");
        }
        return Long.parseLong(String.valueOf(value).trim());
    }

    private static int intOf(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return 1;
        }
        return Integer.parseInt(String.valueOf(value).trim().replace(".0", ""));
    }

    private static String strOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
