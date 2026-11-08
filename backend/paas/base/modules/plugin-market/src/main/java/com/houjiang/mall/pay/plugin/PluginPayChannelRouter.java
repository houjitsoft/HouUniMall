package com.houjiang.mall.pay.plugin;

import com.houjiang.mall.config.market.PluginMarketPayProperties;
import com.houjiang.mall.config.market.PluginPayConfigStore;
import com.houjiang.plugin.market.adapter.outbound.feign.OrderPaasFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 支付渠道路由：按配置 {@code pay.channel} 选渠道，并在缺配置时给出「缺什么、去哪配」的明确报错。
 *
 * <p><b>已接入</b>：{@code CASHIER}（复用商城收银台）、{@code MOCK}（沙箱）。
 * <b>尚未接入</b>：{@code WECHAT_V3} / {@code ALIPAY} 直连——配置里写了这两个编码会直接报错，
 * 不会静默回退到别的渠道（回退会造成「以为接的是微信，其实走的收银台」的对账事故）。
 *
 * @author Amos
 */
@Slf4j
@Component
public class PluginPayChannelRouter {

    private final PluginPayConfigStore store;
    private final Map<String, PluginPayChannel> channels = new LinkedHashMap<>();

    @Autowired
    public PluginPayChannelRouter(PluginPayConfigStore store,
                                  PluginPaySigner signer,
                                  OrderPaasFeign orderPaasFeign) {
        this.store = store;
        CashierPluginPayChannel cashier = new CashierPluginPayChannel(orderPaasFeign, store, signer);
        MockPluginPayChannel mock = new MockPluginPayChannel(store, signer);
        channels.put(cashier.code(), cashier);
        channels.put(mock.code(), mock);
        log.info("插件支付渠道已注册: {}（sandbox={}）", channels.keySet(), store.current().isSandbox());
    }

    /**
     * 当前生效配置快照：基线（yml/Nacos/env）叠加 {@code hou_market_pay_config} 覆盖后的最终值。
     * 每次调用都重新取，运营在后台改完配置点刷新后立刻生效。
     */
    private PluginMarketPayProperties cfg() {
        return store.current();
    }

    /**
     * 取当前生效渠道（沙箱优先 MOCK）。
     *
     * @throws IllegalStateException 渠道未接入或商户参数缺失，错误信息可直接回前端
     */
    public PluginPayChannel require() {
        return require(resolveCode());
    }

    /** 取指定渠道；未接入 / 参数不全时抛明确错误。 */
    public PluginPayChannel require(String code) {
        String target = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        PluginPayChannel channel = channels.get(target);
        if (channel == null) {
            throw new IllegalStateException("支付渠道未接入: " + target
                    + "。当前已接入: " + channels.keySet()
                    + "。请修改 houcloud.plugin.market.pay.channel，"
                    + "或实现 PluginPayChannel 接口后在本路由注册（微信/支付宝直连尚未内置）");
        }
        if (!channel.available()) {
            throw new IllegalStateException("支付渠道 " + target + " 配置不完整："
                    + describeChannel(target)
                    + "。请在 Nacos mall.yaml 或环境变量补全后重试");
        }
        return channel;
    }

    /** 当前生效渠道编码：沙箱模式下强制 MOCK。 */
    public String resolveCode() {
        return cfg().mockMode() ? MockPluginPayChannel.CODE : cfg().code();
    }

    /** 回调地址（当前生效渠道）。 */
    public String notifyUrl() {
        String code = resolveCode();
        PluginMarketPayProperties.ChannelProperties channel = cfg().channel(code);
        if (channel != null && channel.getNotifyUrl() != null && !channel.getNotifyUrl().isBlank()) {
            return channel.getNotifyUrl().trim();
        }
        return cfg().notifyUrlOf(code);
    }

    /**
     * 配置体检报告：供 {@code /base/plugin-center/market/pay/config} 下发，前端据此渲染「待配置」引导。
     */
    public Map<String, Object> describe() {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("enabled", cfg().isEnabled());
        view.put("sandbox", cfg().isSandbox());
        view.put("configuredChannel", cfg().code());
        view.put("effectiveChannel", resolveCode());
        view.put("currency", cfg().getCurrency());
        view.put("payWindowMinutes", cfg().effectivePayWindowMinutes());
        view.put("publicBaseUrl", cfg().getPublicBaseUrl());
        view.put("notifyUrl", notifyUrl());
        view.put("notifyPath", cfg().getNotifyPath());
        view.put("availableChannels", new ArrayList<>(channels.keySet()));

        List<String> issues = new ArrayList<>();
        if (!cfg().isEnabled()) {
            issues.add("在线购买未开启：houcloud.plugin.market.pay.enabled 当前为 false");
        }
        String base = cfg().getPublicBaseUrl();
        if (base == null || base.isBlank()) {
            issues.add("未配置 public-base-url，支付渠道无法回调，订单会停在待支付");
        }
        String effective = resolveCode();
        PluginPayChannel channel = channels.get(effective);
        if (channel == null) {
            issues.add("渠道 " + effective + " 未接入");
        } else if (!channel.available()) {
            issues.add("渠道 " + effective + " 参数不全：" + describeChannel(effective));
        }
        if (cfg().isSandbox()) {
            issues.add("当前为沙箱模式（不会真实扣款），上线前必须置 pay.sandbox=false");
        }
        view.put("issues", issues);
        view.put("ready", issues.isEmpty());
        return view;
    }

    /** 某插件的可选套餐（供购买页渲染价格）。 */
    public List<Map<String, Object>> plansOf(String pluginCode) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (cfg().getPlans() == null || pluginCode == null) {
            return result;
        }
        Map<String, PluginMarketPayProperties.PlanProperties> byPlan = cfg().getPlans().get(pluginCode.trim());
        if (byPlan == null) {
            return result;
        }
        byPlan.forEach((code, plan) -> {
            if (!plan.isEnabled()) {
                return;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", code);
            item.put("name", plan.getName());
            item.put("price", plan.getPrice());
            item.put("currency", plan.getCurrency() == null || plan.getCurrency().isBlank()
                    ? cfg().getCurrency() : plan.getCurrency());
            item.put("validDays", plan.getValidDays());
            item.put("licenseType", plan.getLicenseType());
            result.add(item);
        });
        return result;
    }

    private String describeChannel(String code) {
        // 渠道自己说得清为什么不可用（如 MOCK 的「生产不允许沙箱」）就优先用它，
        // 否则再退化成「缺哪些商户字段」，避免给出一份填不满的清单
        PluginPayChannel instance = channels.get(code);
        if (instance != null && !instance.available()) {
            String reason = instance.unavailableReason();
            if (reason != null && !reason.isBlank()) {
                return reason;
            }
        }
        PluginMarketPayProperties.ChannelProperties channel = cfg().channel(code);
        if (channel == null) {
            return "缺少 houcloud.plugin.market.pay.channels." + code + " 配置段";
        }
        List<String> missing = channel.missingFields();
        return missing.isEmpty() ? "参数齐全" : "缺少 " + String.join("、", missing);
    }
}
