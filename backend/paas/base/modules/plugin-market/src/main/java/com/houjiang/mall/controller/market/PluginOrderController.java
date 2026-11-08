package com.houjiang.mall.controller.market;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SiteInstanceResolver;
import com.houjiang.mall.service.market.PluginOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 插件市场在线订单（{@code /base/plugin-center/market/order/**}）。
 *
 * <p>在线购买闭环：{@code /create} 下单 → {@code /pay} 拿支付参数 → {@code /sync} 查支付状态并自动发证
 * → 授权出现在「授权管理」页，租户直接到市场安装。
 *
 * <p><b>暴露面</b>：全部端点均为租户侧，经 BFF {@code /web/plugin-center/market/order/**} 转发；
 * 列表与详情都以 instanceId 收敛，看不到其它站点的订单。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/plugin-center/market/order")
public class PluginOrderController {

    private final PluginOrderService pluginOrderService;

    /**
     * 下单：生成待支付订单。
     *
     * <p>参数：{@code pluginCode}（必填）、
     * {@code licenseType}（PAID/TRIAL，默认取插件标价类型）、{@code version}、{@code validDays}、{@code buyerId}、
     * {@code planCode}（价格套餐，插件已配套餐时必填，见 {@code /base/plugin-center/market/pay/config} 返回的 plans）。
     *
     * <p>{@code instanceId} <b>不是入参</b>：由服务端按登录态解析并写回请求体。请求体里传了
     * 会被丢弃并记 warn（不是硬拒 400）。授权签发因此只会绑定到当前登录站点，
     * 无法伪造成别人的站点下单。
     */
    @PostMapping("/create")
    public ResponseResult create(@RequestBody Map<String, Object> params) {
        try {
            if (params == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }
            String pluginCode = requiredText(params, "插件编码不能为空", "pluginCode", "code");
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            return ResponseResult.success(pluginOrderService.toView(pluginOrderService.createOrder(
                    pluginCode,
                    text(params.get("licenseType")),
                    text(params.get("version")),
                    intValue(params.get("validDays")),
                    instanceId,
                    trustedBuyerId(),
                    text(params.get("planCode")))));
        } catch (Exception e) {
            log.error("创建插件订单失败", e);
            return ResponseResult.fail(500, "创建插件订单失败: " + e.getMessage());
        }
    }

    /**
     * 发起支付：金额为 0（试用）时不经收银台，直接自动结单发证。
     *
     * <p>参数：{@code orderNo}（必填）、{@code payMethod}（必填，如 WXPAY/ALIPAY）、
     * {@code terminal}、{@code userId}、{@code returnUrl}。
     *
     * <p><b>{@code notifyUrl} 不再生效</b>：回调地址一律取配置项
     * {@code houcloud.plugin.market.pay.public-base-url + notify-path}。请求里传了会被丢弃并记
     * warn（保留该字段只为兼容旧调用方，不接受外部指定回调地址——否则攻击者可把回调引到自己
     * 的机器上，在验签之外再开一个口子）。
     */
    @PostMapping("/pay")
    public ResponseResult pay(@RequestBody Map<String, Object> params) {
        try {
            if (params == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }
            String orderNo = requiredText(params, "订单号不能为空", "orderNo", "order");
            String payMethod = params.get("payMethod") == null ? null : text(params.get("payMethod"));
            if (payMethod == null) {
                throw new IllegalArgumentException("支付方式不能为空");
            }
            // 站点归属由服务端解析并写回：订单不属于当前站点直接拒绝（防跨站点代付/读单）
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            return ResponseResult.success(pluginOrderService.toView(pluginOrderService.pay(
                    orderNo,
                    payMethod,
                    text(params.get("terminal")),
                    text(params.get("userId")),
                    text(params.get("notifyUrl")),
                    text(params.get("returnUrl")),
                    instanceId)));
        } catch (Exception e) {
            log.error("发起插件订单支付失败", e);
            return ResponseResult.fail(500, "发起支付失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单：顺带同步收银台支付状态，成功即自动发证（幂等）。
     *
     * <p>参数：{@code orderNo}（必填）、{@code userId}。
     */
    @PostMapping("/sync")
    public ResponseResult sync(@RequestBody Map<String, Object> params) {
        try {
            if (params == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }
            String orderNo = requiredText(params, "订单号不能为空", "orderNo", "order");
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            return ResponseResult.success(pluginOrderService.toView(
                    pluginOrderService.syncAndSettle(orderNo, text(params.get("userId")), instanceId)));
        } catch (Exception e) {
            log.error("查询插件订单失败", e);
            return ResponseResult.fail(500, "查询插件订单失败: " + e.getMessage());
        }
    }

    /**
     * 订单详情（纯读取，不触发任何状态同步）。参数：{@code orderNo}
     *
     * <p><b>为什么不用 {@code syncAndSettle}</b>：详情只是「把现在的状态返回去」，
     * 复用同步接口会让一次查询顺带发起结算甚至签发授权——查询带着写副作用，
     * 且并发点开详情会撞上结算抢锁，出现「详情偶发返回旧状态」。
     */
    @PostMapping("/detail")
    public ResponseResult detail(@RequestBody Map<String, Object> params) {
        try {
            if (params == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }
            String orderNo = requiredText(params, "订单号不能为空", "orderNo", "order");
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            // 详情只回状态与金额：payData（渠道支付参数）与 licenseKey 不出这个接口
            return ResponseResult.success(pluginOrderService.toDetailView(
                    pluginOrderService.findOrder(orderNo, instanceId)));
        } catch (Exception e) {
            log.error("查询插件订单详情失败", e);
            return ResponseResult.fail(500, "查询插件订单详情失败: " + e.getMessage());
        }
    }

    /** 取消待支付订单。参数：{@code orderNo} */
    @PostMapping("/cancel")
    public ResponseResult cancel(@RequestBody Map<String, Object> params) {
        try {
            if (params == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }
            String orderNo = requiredText(params, "订单号不能为空", "orderNo", "order");
            String instanceId = SiteInstanceResolver.requireAndOverride(params);
            return ResponseResult.success(pluginOrderService.toView(
                    pluginOrderService.close(orderNo, instanceId)));
        } catch (Exception e) {
            log.error("取消插件订单失败", e);
            return ResponseResult.fail(500, "取消插件订单失败: " + e.getMessage());
        }
    }

    /**
     * 本站点订单列表（按站点收敛，看不到其它站点的订单）。
     *
     * <p>{@code instanceId} <b>不是入参</b>：由服务端按登录态解析，请求体里传了会被忽略。
     *
     * <p><b>fail-closed 但不成 500</b>：解析不出站点身份时返回<b>空列表</b>（与改动前
     * 「instanceId 为空 → {@code listMine(null)} 返回空」的行为一致），不抛异常。
     * 这里刻意不用 {@link SiteInstanceResolver#requireAndOverride(Map)}——那会把
     * 「无身份 → 空列表」退化成 500，前端列表页会直接报错。
     */
    @PostMapping("/mine")
    public ResponseResult mine(@RequestBody(required = false) Map<String, Object> params) {
        try {
            String resolved = SiteInstanceResolver.resolveOrNull();
            if (resolved == null || resolved.isBlank()) {
                log.warn("无法识别站点实例，本站点订单列表返回空");
                return ResponseResult.success(List.of());
            }
            String instanceId = resolved.trim();
            if (params != null) {
                params.put("instanceId", instanceId);
            }
            List<Map<String, Object>> view = pluginOrderService.listMine(instanceId, 200)
                    .stream().map(pluginOrderService::toView).collect(Collectors.toList());
            return ResponseResult.success(view);
        } catch (Exception e) {
            log.error("查询本站点插件订单失败", e);
            return ResponseResult.fail(500, "查询本站点插件订单失败: " + e.getMessage());
        }
    }

    private String requiredText(Map<String, Object> params, String message, String... keys) {
        if (params == null) {
            throw new IllegalArgumentException(message);
        }
        for (String key : keys) {
            String value = text(params.get(key));
            if (value != null) {
                return value;
            }
        }
        throw new IllegalArgumentException(message);
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? null : text;
    }

    private Long longValue(Object value) {
        String text = text(value);
        if (text == null) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数值格式不正确: " + text);
        }
    }

    private Integer intValue(Object value) {
        String text = text(value);
        if (text == null) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("数值格式不正确: " + text);
        }
    }
    private Long trustedBuyerId() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalArgumentException("未登录");
        var user = com.houjiang.commons.util.SecurityContextUtils.getCurrentUserOrNull();
        Object id = user != null ? user.getUserId() : auth.getPrincipal() instanceof Map<?,?> map ? map.get("id") : null;
        Long buyer = id == null ? null : longValue(id);
        if (buyer == null) throw new IllegalArgumentException("无法识别购买方");
        return buyer;
    }
}
