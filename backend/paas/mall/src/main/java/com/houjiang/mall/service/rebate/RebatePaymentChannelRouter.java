package com.houjiang.mall.service.rebate;

import com.houjiang.mall.entity.rebate.RebatePayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 返利发放通道路由。
 *
 * <p>配置：{@code houcloud.rebate.pay.gateway}，默认 {@code none}。
 * 选不到或不可用时一律回落到 {@code none}（fail-closed）——宁可发放失败让人介入，
 * 也不能出现「钱没打、账已成功」的假账。
 *
 * @author Amos
 */
@Slf4j
@Component
public class RebatePaymentChannelRouter {

    /** 未接入通道：明确失败，不产生任何成功记录。 */
    public static final String NONE = "none";

    /** 模拟通道：只在沙箱里可用，流水号带 MOCK- 前缀，对账口径可直接排除。 */
    public static final String MOCK = "mock";

    @Value("${houcloud.rebate.pay.gateway:none}")
    private String gateway;

    @Value("${houcloud.rebate.pay.sandbox:false}")
    private boolean sandbox;

    private final Map<String, RebatePaymentChannel> channels = new LinkedHashMap<>();

    public RebatePaymentChannelRouter() {
        channels.put(NONE, new NoneChannel());
        channels.put(MOCK, new MockChannel());
    }

    /** 注册额外通道（真实通道实现后调用，key 用通道自己的 code）。 */
    public synchronized void register(RebatePaymentChannel channel) {
        channels.put(channel.code(), channel);
    }

    /**
     * 取当前生效通道。
     *
     * @return 永不返回 null：找不到就返回 {@code none}
     */
    public RebatePaymentChannel route() {
        String key = gateway == null || gateway.isBlank() ? NONE : gateway.trim().toLowerCase();
        RebatePaymentChannel channel = channels.get(key);
        if (channel == null) {
            log.warn("[返利发放] 配置的通道 {} 不存在，回落 none（不会发放）", key);
            return channels.get(NONE);
        }
        if (!channel.available()) {
            log.warn("[返利发放] 通道 {} 当前不可用（sandbox={}），回落 none（不会发放）", key, sandbox);
            return channels.get(NONE);
        }
        return channel;
    }

    /** 当前配置描述，给管理页/日志用。 */
    public String describe() {
        return "gateway=" + gateway + ", sandbox=" + sandbox + ", effective=" + route().code();
    }

    /** 未接入通道：永远失败。 */
    private static class NoneChannel implements RebatePaymentChannel {

        @Override
        public String code() {
            return NONE;
        }

        @Override
        public boolean available() {
            return true;
        }

        @Override
        public Result pay(RebatePayment payment) {
            return Result.fail("未配置返利发放通道（houcloud.rebate.pay.gateway=none），"
                    + "本次未出款，请接入通道后重试或线下打款");
        }
    }

    /** 模拟通道：仅沙箱可用，流水号显式带 MOCK- 前缀，绝不伪装成真实流水。 */
    private class MockChannel implements RebatePaymentChannel {

        @Override
        public String code() {
            return MOCK;
        }

        @Override
        public boolean available() {
            return sandbox;
        }

        @Override
        public Result pay(RebatePayment payment) {
            return Result.success("MOCK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase());
        }
    }
}
