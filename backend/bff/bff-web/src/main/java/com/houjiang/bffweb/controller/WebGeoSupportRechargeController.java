package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import com.houjiang.bffweb.feign.PaymentServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GEO 智能客服 Token 充值（创建订单 + H5 支付 + 同步到账）
/**
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/geo/support/recharge")
@RequiredArgsConstructor
public class WebGeoSupportRechargeController {

    private final GeoServiceFeign geoServiceFeign;
    private final PaymentServiceFeign paymentServiceFeign;

    @GetMapping("/packages")
    public ResponseResult packages() {
        return geoServiceFeign.supportRechargePackages();
    }

    /**
     * 创建充值订单并获取 H5 收银台链接（需登录）
     */
    @PostMapping("/pay")
    public ResponseResult pay(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> req = body != null ? body : Map.of();
        ResponseResult createRes = geoServiceFeign.supportRechargeCreate(req);
        if (!isOk(createRes)) {
            return createRes;
        }
        Map<String, Object> order = asMap(createRes.getData());
        if (order.isEmpty()) {
            return ResponseResult.fail(500, "创建充值订单失败");
        }

        String orderNo = stringVal(order.get("orderNo"));
        String amountFen = String.valueOf(order.getOrDefault("amountFen", order.get("amount_fen")));
        String title = stringVal(order.get("title"));
        if (!StringUtils.hasText(orderNo) || !StringUtils.hasText(amountFen)) {
            return ResponseResult.fail(500, "充值订单数据不完整");
        }

        Map<String, Object> h5Req = new LinkedHashMap<>();
        h5Req.put("mchOrderNo", orderNo);
        h5Req.put("amount", amountFen);
        h5Req.put("body", StringUtils.hasText(title) ? title : "AI Token 充值");
        h5Req.put("extParam", orderNo);
        h5Req.put("validTime", 3600);
        h5Req.put("expiredTime", 3600);
        if (req.get("jumpUrl") != null && StringUtils.hasText(String.valueOf(req.get("jumpUrl")))) {
            h5Req.put("jumpUrl", String.valueOf(req.get("jumpUrl")).trim());
        }

        ResponseResult h5Res = paymentServiceFeign.h5CashGetH5Url(h5Req);
        if (!isOk(h5Res)) {
            return h5Res;
        }
        Map<String, Object> h5Data = asMap(h5Res.getData());
        String h5Url = firstNonBlank(
            stringVal(h5Data.get("H5Url")),
            stringVal(h5Data.get("h5Url")),
            stringVal(h5Data.get("h5_url"))
        );
        if (!StringUtils.hasText(h5Url)) {
            return ResponseResult.fail(500, "支付链接生成失败");
        }

        Map<String, Object> attach = new LinkedHashMap<>();
        attach.put("orderNo", orderNo);
        attach.put("h5Url", h5Url);
        geoServiceFeign.supportRechargeAttachH5(attach);

        Map<String, Object> data = new LinkedHashMap<>(order);
        data.put("h5Url", h5Url);
        data.put("mchOrderNo", orderNo);
        return ResponseResult.success(data);
    }

    /**
     * 同步支付结果并发放 Token（前端轮询或支付回跳后调用）
     */
    @PostMapping("/sync")
    public ResponseResult sync(@RequestBody Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return ResponseResult.fail(400, "参数不能为空");
        }

        String orderNo = stringVal(body.get("orderNo"));
        if (!StringUtils.hasText(orderNo) && body.get("mchOrderNo") != null) {
            orderNo = stringVal(body.get("mchOrderNo"));
        }
        if (!StringUtils.hasText(orderNo)) {
            return ResponseResult.fail(400, "orderNo 不能为空");
        }

        boolean paid = false;
        Integer payState = null;

        if (hasJumpSignParams(body)) {
            Map<String, String> jumpParams = toStringMap(body);
            ResponseResult parseRes = paymentServiceFeign.h5CashParseJumpResult(jumpParams);
            if (isOk(parseRes)) {
                Map<String, Object> payOrder = asMap(parseRes.getData());
                payState = intVal(payOrder.get("state"));
                paid = Integer.valueOf(2).equals(payState);
            }
        } else {
            String mchNo = stringVal(body.get("mchNo"));
            ResponseResult queryRes = paymentServiceFeign.h5CashQueryByMchOrderNo(
                StringUtils.hasText(mchNo) ? mchNo : "",
                orderNo
            );
            if (isOk(queryRes) && queryRes.getData() != null) {
                Map<String, Object> payOrder = asMap(queryRes.getData());
                payState = intVal(payOrder.get("state"));
                paid = Integer.valueOf(2).equals(payState);
            }
        }

        Map<String, Object> confirmBody = new LinkedHashMap<>();
        confirmBody.put("orderNo", orderNo);
        confirmBody.put("paid", paid);
        if (payState != null) {
            confirmBody.put("state", payState);
        }
        return geoServiceFeign.supportRechargeConfirm(confirmBody);
    }

    @GetMapping("/status")
    public ResponseResult status(@RequestParam("orderNo") String orderNo) {
        return geoServiceFeign.supportRechargeStatus(orderNo);
    }

    private static boolean isOk(ResponseResult res) {
        if (res == null) {
            return false;
        }
        int code = res.getCode();
        return code == 0 || code == 1 || code == 200;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object raw) {
        if (raw instanceof Map<?, ?> map) {
            Map<String, Object> out = new LinkedHashMap<>();
            map.forEach((k, v) -> out.put(String.valueOf(k), v));
            return out;
        }
        return Map.of();
    }

    private static String stringVal(Object raw) {
        return raw == null ? "" : String.valueOf(raw).trim();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return "";
    }

    private static Integer intVal(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static boolean hasJumpSignParams(Map<String, Object> body) {
        return StringUtils.hasText(stringVal(body.get("sign")))
            && StringUtils.hasText(stringVal(body.get("mchOrderNo")));
    }

    private static Map<String, String> toStringMap(Map<String, Object> body) {
        Map<String, String> out = new LinkedHashMap<>();
        body.forEach((k, v) -> {
            if (v != null) {
                out.put(k, String.valueOf(v));
            }
        });
        return out;
    }
}
