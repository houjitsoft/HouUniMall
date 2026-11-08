package com.houjiang.mall.controller.market;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.config.market.PluginMarketPayProperties;
import com.houjiang.mall.config.market.PluginPayConfigStore;
import com.houjiang.mall.entity.market.MarketPluginOrder;
import com.houjiang.mall.mapper.market.MarketPluginOrderMapper;
import com.houjiang.mall.pay.plugin.MockPluginPayChannel;
import com.houjiang.mall.pay.plugin.PayNotifyException;
import com.houjiang.mall.pay.plugin.PluginPayChannel;
import com.houjiang.mall.pay.plugin.PluginPayChannelRouter;
import com.houjiang.mall.service.market.PluginPayNotifyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 收费插件的支付回调与配置自检（{@code /base/plugin-center/market/pay/**}）。
 *
 * <p><b>回调端点必须免登录</b>：渠道服务器（微信/支付宝/商城收银台）不会带本站的用户 Token，
 * 因此 {@code /web/plugin-center/market/pay/notify/**} 要在 BFF 鉴权白名单里放行，
 * 安全性由「回调验签」而不是「登录态」保证——这也是为什么 {@code CASHIER.api-key}
 * 未配置时回调一律拒收。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/base/plugin-center/market/pay")
public class PluginPayNotifyController {

    private final PluginPayNotifyService notifyService;
    private final PluginPayChannelRouter channelRouter;
    private final PluginPayConfigStore payStore;
    private final MarketPluginOrderMapper orderMapper;

    /** 当前生效配置快照（DB 覆盖后的最终值），每次重新取以支持热更新。 */
    private PluginMarketPayProperties cfg() {
        return payStore.current();
    }

    /**
     * 渠道支付结果回调。
     *
     * <p><b>为什么用 String 而不是统一 JSON</b>：支付宝要求应答字面量 {@code success}，
     * 微信 V3 要求 JSON，回错格式渠道会一直重发。应答原文由渠道自己决定。
     */
    @PostMapping(value = "/notify/{channel}",
            consumes = {MediaType.ALL_VALUE},
            produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public String notify(@PathVariable("channel") String channel,
                         @RequestParam(required = false) Map<String, String> params,
                         @RequestBody(required = false) String rawBody,
                         HttpServletRequest request) {
        Map<String, String> headers = extractSignHeaders(request);
        Map<String, String> safeParams = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
        try {
            PluginPayNotifyService.NotifyOutcome outcome =
                    notifyService.handle(channel, rawBody, safeParams, headers);
            return successResponse(channel);
        } catch (PayNotifyException e) {
            // 非致命（重复回调等）也回成功，避免渠道无意义重发；致命错误回失败让渠道记着重发
            if (!e.isFatal()) {
                return successResponse(channel);
            }
            log.error("支付回调处理失败（等待渠道重发 / 需人工介入）: channel={} msg={}", channel, e.getMessage());
            return failResponse(channel, e.getMessage());
        } catch (RuntimeException e) {
            log.error("支付回调处理异常: channel={}", channel, e);
            return failResponse(channel, "系统异常: " + e.getMessage());
        }
    }

    /**
     * 支付配置自检 + 价格套餐（供购买页渲染）。
     *
     * <p><b>只下发「缺什么」，不下发密钥</b>：任何 appId / mchId / apiKey 都不出现在响应里，
     * 避免把商户凭据泄露给租户侧页面。
     *
     * <p>参数：{@code pluginCode}（可选，用于返回该插件的可选套餐）。
     */
    @PostMapping("/config")
    public ResponseResult config(@RequestBody(required = false) Map<String, Object> params,
                                 @RequestParam(required = false) String pluginCode) {
        Map<String, Object> view = new LinkedHashMap<>(channelRouter.describe());
        String code = pluginCode;
        if ((code == null || code.isBlank()) && params != null) {
            Object value = params.get("pluginCode");
            code = value == null ? null : String.valueOf(value);
        }
        if (code != null && !code.isBlank()) {
            view.put("plans", channelRouter.plansOf(code.trim()));
        }
        return ResponseResult.success(view);
    }

    /**
     * 沙箱模拟支付：走真实回调链路把订单置为已支付并签发授权。
     *
     * <p>⚠️ <b>仅 {@code pay.sandbox=true} 可用</b>。生产环境请保持 {@code sandbox=false}，
     * 否则任何人都能把订单刷成已支付并领走授权。
     *
     * <p>参数：{@code orderNo}（必填）、{@code result}（SUCCESS / FAILED / REFUNDED，默认 SUCCESS）、
     * {@code amount}（可选）。
     *
     * <p><b>amount 缺省时自动按订单应付金额回填</b>（2026-10-30 修复）：回调链路对实收金额
     * 是 fail-closed 校验（缺少金额一律拒收，防伪造回调绕过对账），此前注释写「不传则不校验
     * 金额」与实际行为不符——不传 amount 时签名参数里没有金额字段，回调直接被拒
     * 「回调缺少金额字段 amount，无法确认实收金额」，沙箱购买流程卡死在最后一步。
     * 现在缺省回填订单 amount（模拟支付语义：实付=应付），显式传 amount 时仍可模拟
     * 少付/多付场景（回调链会按金额不一致拒收）。
     */
    @PostMapping("/sandbox/confirm")
    public ResponseResult sandboxConfirm(@RequestBody(required = false) Map<String, Object> params) {
        if (!cfg().isSandbox()) {
            return ResponseResult.fail(403, "沙箱未开启（houcloud.plugin.market.pay.sandbox=false），"
                    + "模拟支付不可用。这是生产环境应有的状态。");
        }
        if (params == null || params.get("orderNo") == null) {
            return ResponseResult.fail(400, "缺少 orderNo");
        }
        String orderNo = String.valueOf(params.get("orderNo")).trim();
        String result = params.get("result") == null
                ? "SUCCESS" : String.valueOf(params.get("result")).trim().toUpperCase();
        String amountText = params.get("amount") == null ? null : String.valueOf(params.get("amount"));
        if (amountText == null || amountText.isBlank()) {
            // 回调链 fail-closed 必须有金额：缺省按订单应付金额回填（实付=应付的模拟语义）。
            MarketPluginOrder order = orderMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<MarketPluginOrder>()
                            .eq(MarketPluginOrder::getOrderNo, orderNo));
            if (order == null || order.getAmount() == null) {
                return ResponseResult.fail(400, "订单不存在或金额缺失: " + orderNo);
            }
            amountText = order.getAmount().toPlainString();
        }

        PluginPayChannel mock = channelRouter.require(MockPluginPayChannel.CODE);
        if (!(mock instanceof MockPluginPayChannel mockChannel)) {
            return ResponseResult.fail(500, "MOCK 渠道未装配");
        }
        Map<String, String> signed = mockChannel.buildSignedParams(orderNo, result, amountText);
        try {
            PluginPayNotifyService.NotifyOutcome outcome =
                    notifyService.handle(MockPluginPayChannel.CODE, null, signed, Map.of());
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("orderNo", orderNo);
            data.put("result", outcome.getResult());
            data.put("message", outcome.getMessage());
            return ResponseResult.success(data);
        } catch (PayNotifyException e) {
            log.warn("沙箱模拟支付失败: order={} msg={}", orderNo, e.getMessage());
            return ResponseResult.fail(500, "模拟支付失败: " + e.getMessage());
        }
    }

    private Map<String, String> extractSignHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (request == null) {
            return headers;
        }
        for (String name : java.util.List.of("Wechatpay-Serial", "Wechatpay-Signature",
                "Wechatpay-Timestamp", "Wechatpay-Nonce", "X-Pay-Sign", "X-Pay-Timestamp")) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                headers.put(name, value);
            }
        }
        return headers;
    }

    private String successResponse(String channel) {
        try {
            return channelRouter.require(channel).successResponse();
        } catch (RuntimeException e) {
            return "success";
        }
    }

    private String failResponse(String channel, String message) {
        try {
            return channelRouter.require(channel).failResponse(message);
        } catch (RuntimeException e) {
            return "fail:" + message;
        }
    }

}
