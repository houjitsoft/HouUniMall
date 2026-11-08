package com.houjiang.payment.channel.biz;

import com.houjiang.payment.exception.PaymentException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自研渠道策略工厂（W4 P0 任务 T8）。
 * <p>
 * 与历史 {@link com.houjiang.payment.handle.PayStrategyHandleFactory} 物理隔离：
 * <ul>
 *   <li>历史工厂使用静态 Map + new 实例化，对应通企付适配历史代码，冻结不动</li>
 *   <li>本工厂走 Spring 容器自动收集 {@link ChannelPayStrategy} 实现，由 {@link #getChannel()} 注册</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayChannelStrategyFactory {

    private final List<ChannelPayStrategy> strategies;
    private final Map<String, ChannelPayStrategy> registry = new HashMap<>();

    @PostConstruct
    public void init() {
        for (ChannelPayStrategy s : strategies) {
            String code = s.getChannel();
            if (code == null || code.isEmpty()) {
                log.warn("[PayChannelStrategyFactory] skip strategy without channel code: {}", s.getClass().getSimpleName());
                continue;
            }
            registry.put(code, s);
            log.info("[PayChannelStrategyFactory] register channel={} -> {}", code, s.getClass().getSimpleName());
        }
        log.info("[PayChannelStrategyFactory] total {} channels registered", registry.size());
    }

    /** 按渠道 code 取策略实例（不存在抛业务异常） */
    public ChannelPayStrategy require(String channel) {
        ChannelPayStrategy s = registry.get(channel);
        if (s == null) {
            throw PaymentException.bizError("不支持的支付渠道：" + channel);
        }
        return s;
    }

    /** 仅取，不存在返回 null */
    public ChannelPayStrategy get(String channel) {
        return registry.get(channel);
    }

    /** 返回已注册的所有渠道 code */
    public java.util.Set<String> listChannels() {
        return registry.keySet();
    }
}
