package com.houjiang.payment.channel.biz.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.payment.channel.biz.impl.WechatV3PayStrategy;
import com.houjiang.payment.constant.PayMethodEnum;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.mapper.PayChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 微信支付 V3 平台证书自动刷新调度器（W5+ P2-2）
 * <p>
 * 微信支付 V3 平台证书有效期 12 个月，过期后回调验签会失败；
 * 接入 wechatpay-java 后默认有 12h 自动刷新机制，但需要应用持有 RSAAutoCertificateConfig 实例。
 * 本调度器周期性调用 {@link WechatV3PayStrategy#refreshPlatformCert(String)} 维持证书缓存活性。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>启动时延迟 60s 触发首次刷新（避免与启动初始化冲突）</li>
 *   <li>之后每 12h 刷新一次（与微信 SDK 默认周期对齐）</li>
 *   <li>从 hou_pay_channel 取出所有 channel_code=WECHAT_V3 且 status=1 的记录</li>
 *   <li>逐商户调用 refreshPlatformCert，单商户失败不影响其他商户</li>
 *   <li>未配置任何微信 V3 凭证时直接返回（日志降级到 debug）</li>
 *   <li>支持通过配置 houjiang.pay.cert-refresh.enabled=false 关闭（默认开启）</li>
 * </ul>
 * <p>
 * 与正在进行的支付交易完全隔离：本调度器仅刷新本地证书缓存，不涉及交易主链路。
 *
 * @author Amos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WechatV3CertRefreshScheduler {

    private final WechatV3PayStrategy wechatV3PayStrategy;
    private final PayChannelMapper payChannelMapper;

    /** 调度开关，false 时跳过执行（默认 true） */
    @Value("${houjiang.pay.cert-refresh.enabled:true}")
    private boolean enabled;

    /** 上次刷新时间，便于排查 */
    private volatile Date lastRefreshAt;

    /** 上次刷新成功商户数 */
    private volatile int lastRefreshedCount;

    /**
     * 应用启动后延迟 60s 触发首次刷新
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!enabled) {
            log.info("[WechatV3CertRefreshScheduler] disabled by config, skip startup refresh");
            return;
        }
        new Thread(() -> {
            try {
                Thread.sleep(60_000L);
                log.info("[WechatV3CertRefreshScheduler] application ready, trigger first refresh");
                refreshAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "wechat-v3-cert-init").start();
    }

    /**
     * 周期性刷新：每 12 小时执行一次
     * cron 表达式：0 0 0/12 * * ?  → 每天 00:00 与 12:00 各执行一次
     */
    @Scheduled(cron = "0 0 0/12 * * ?")
    public void scheduledRefresh() {
        if (!enabled) {
            log.debug("[WechatV3CertRefreshScheduler] disabled by config, skip scheduled refresh");
            return;
        }
        refreshAll();
    }

    /**
     * 执行一次全量刷新
     * <p>
     * 流程：
     * <ol>
     *   <li>查 hou_pay_channel 所有 WECHAT_V3 且 status=1 的记录</li>
     *   <li>逐商户调用 refreshPlatformCert</li>
     *   <li>统计成功/跳过/失败 数量并落日志</li>
     * </ol>
     *
     * @return 实际成功刷新数（仅 status=refreshed）
     */
    public int refreshAll() {
        List<PayChannelEntity> list = payChannelMapper.selectList(new LambdaQueryWrapper<PayChannelEntity>()
                .eq(PayChannelEntity::getChannelCode, PayMethodEnum.WECHAT_V3.getCode())
                .eq(PayChannelEntity::getStatus, 1));
        if (list == null || list.isEmpty()) {
            log.debug("[WechatV3CertRefreshScheduler] no WECHAT_V3 channel configured, skip");
            this.lastRefreshAt = new Date();
            this.lastRefreshedCount = 0;
            return 0;
        }

        int refreshed = 0;
        int skipped = 0;
        int failed = 0;
        for (PayChannelEntity entity : list) {
            String mchNo = entity.getMerchantNo();
            try {
                java.util.Map<String, Object> r = wechatV3PayStrategy.refreshPlatformCert(mchNo);
                Object status = r.get("status");
                if ("refreshed".equals(status)) {
                    refreshed++;
                } else if ("skipped".equals(status)) {
                    skipped++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
                log.warn("[WechatV3CertRefreshScheduler] refresh fail mchNo={} err={}", mchNo, e.getMessage());
            }
        }
        this.lastRefreshAt = new Date();
        this.lastRefreshedCount = refreshed;
        log.info("[WechatV3CertRefreshScheduler] done total={}, refreshed={}, skipped={}, failed={}",
                list.size(), refreshed, skipped, failed);
        return refreshed;
    }

    /**
     * 暴露给 actuator/手动触发：返回上次执行摘要
     *
     * @return [refreshAt, refreshedCount, enabled]
     */
    public java.util.Map<String, Object> status() {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("enabled", enabled);
        m.put("lastRefreshAt", lastRefreshAt);
        m.put("lastRefreshedCount", lastRefreshedCount);
        return m;
    }
}
