package com.houjiang.mall.service.market;

import com.houjiang.mall.entity.market.MarketPluginLicense;
import java.time.LocalDateTime;

/** Keeps official maintenance entitlement separate from the perpetual runtime grant. */
public final class OfficialMaintenancePolicy {
    public static final int FIRST_PURCHASE_MONTHS = 12;
    private OfficialMaintenancePolicy() {}

    /**
     * 一笔购买折算出的维护期增量。
     *
     * <p>默认是官方政策 12 个月；当这笔 grant 带 {@code term_days}（来自价格套餐的
     * {@code valid_days}）时按天累计——否则「买 30 天版」与「买永久版」拿到的
     * 维护期完全一样，套餐价差无法解释（审计 2026-10-27 P2-2）。
     */
    private static LocalDateTime extend(LocalDateTime base, Integer termDays) {
        LocalDateTime from = base == null ? LocalDateTime.now() : base;
        return termDays != null && termDays > 0
                ? from.plusDays(termDays)
                : from.plusMonths(FIRST_PURCHASE_MONTHS);
    }

    public static void firstPurchase(MarketPluginLicense license, LocalDateTime purchasedAt) {
        requirePaid(license);
        LocalDateTime at = purchasedAt == null ? LocalDateTime.now() : purchasedAt;
        license.setMaintenanceUntil(extend(at, license.getTermDays()));
        license.setExpireTime(null);
    }
    public static void renew(MarketPluginLicense license, LocalDateTime renewedAt) {
        requirePaid(license);
        LocalDateTime at = renewedAt == null ? LocalDateTime.now() : renewedAt;
        LocalDateTime base = license.getMaintenanceUntil();
        if (base == null || base.isBefore(at)) base = at;
        license.setMaintenanceUntil(extend(base, license.getTermDays()));
        license.setExpireTime(null);
    }
    public static boolean active(MarketPluginLicense license, LocalDateTime at) {
        return license != null && license.getStatus() != null && license.getStatus() == 1
                && license.getMaintenanceUntil() != null
                && license.getMaintenanceUntil().isAfter(at == null ? LocalDateTime.now() : at);
    }
    /** Grants must be ordered by purchase time/id. Manual grants retain their original cutoff. */
    public static void recalculate(java.util.List<MarketPluginLicense> grants) {
        LocalDateTime until=null;
        for(var grant:grants) {
            if(!"PAID".equalsIgnoreCase(grant.getLicenseType()) || !Integer.valueOf(1).equals(grant.getStatus())
                    || Integer.valueOf(1).equals(grant.getPurchaseSandbox())) continue;
            if(grant.getSourceOrderNo()==null || grant.getSourceOrderNo().isBlank()) {
                if(grant.getMaintenanceUntil()!=null && (until==null || until.isBefore(grant.getMaintenanceUntil()))) until=grant.getMaintenanceUntil();
                continue;
            }
            LocalDateTime at=grant.getCreateTime();
            if(at==null)throw new IllegalStateException("购买记录缺少结算时间，无法计算维护期");
            if(until==null || until.isBefore(at))until=at;
            until=extend(until, grant.getTermDays());
            grant.setMaintenanceUntil(until);grant.setExpireTime(null);
        }
    }

    private static void requirePaid(MarketPluginLicense license) {
        if (license == null || !PluginLicenseService.TYPE_PAID.equalsIgnoreCase(license.getLicenseType()))
            throw new IllegalStateException("只有 PAID 授权拥有官方维护期");
        if (!Integer.valueOf(1).equals(license.getStatus()))
            throw new IllegalStateException("已吊销授权不能续费");
    }
}
