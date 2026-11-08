package com.houjiang.mall.service.market;

import com.houjiang.mall.entity.market.MarketPluginLicense;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class OfficialMaintenancePolicyTest {
    @Test void firstPurchaseUsesCalendarMonthsAndKeepsRuntimePerpetual() {
        var bought = LocalDateTime.of(2024, 2, 29, 12, 0);
        var license = paid();
        OfficialMaintenancePolicy.firstPurchase(license, bought);
        assertEquals(LocalDateTime.of(2025, 2, 28, 12, 0), license.getMaintenanceUntil());
        assertNull(license.getExpireTime());
        assertTrue(OfficialMaintenancePolicy.active(license, license.getMaintenanceUntil().minusSeconds(1)));
        assertFalse(OfficialMaintenancePolicy.active(license, license.getMaintenanceUntil()));
        assertEquals(1, license.getStatus());
    }
    @Test void earlyRenewalExtendsRemainingTimeAndLateRenewalStartsAtPayment() {
        var now = LocalDateTime.of(2026, 9, 21, 12, 0);
        var license = paid(); OfficialMaintenancePolicy.firstPurchase(license, now);
        OfficialMaintenancePolicy.renew(license, now.plusMonths(6));
        assertEquals(now.plusMonths(24), license.getMaintenanceUntil());
        OfficialMaintenancePolicy.renew(license, now.plusMonths(30));
        assertEquals(now.plusMonths(42), license.getMaintenanceUntil());
        assertNull(license.getExpireTime());
    }
    @Test void trialOrRevokedGrantCannotObtainPaidMaintenance() {
        var license = paid(); license.setLicenseType("TRIAL");
        assertThrows(IllegalStateException.class, () -> OfficialMaintenancePolicy.firstPurchase(license, LocalDateTime.now()));
        license.setLicenseType("PAID"); license.setStatus(0);
        assertThrows(IllegalStateException.class, () -> OfficialMaintenancePolicy.renew(license, LocalDateTime.now()));
    }
    @Test void refundOfMiddleRenewalRemovesItsContributionFromLaterGrants() {
        var at=LocalDateTime.of(2026,9,21,12,0);
        var first=grant("A",at);var middle=grant("B",at.plusMonths(1));var last=grant("C",at.plusMonths(2));
        var all=java.util.List.of(first,middle,last);
        OfficialMaintenancePolicy.recalculate(all);
        assertEquals(at.plusMonths(36),last.getMaintenanceUntil());
        middle.setStatus(0);
        OfficialMaintenancePolicy.recalculate(all);
        assertEquals(at.plusMonths(24),last.getMaintenanceUntil());
        assertNull(last.getExpireTime());
    }
    @Test void sandboxAndTrialNeverContributeAndLatePurchaseRestartsAtPayment() {
        var at=LocalDateTime.of(2026,9,21,12,0);
        var first=grant("A",at);var sandbox=grant("S",at.plusMonths(1));sandbox.setPurchaseSandbox(1);
        var trial=grant("T",at.plusMonths(2));trial.setLicenseType("TRIAL");
        var last=grant("B",at.plusMonths(20));
        OfficialMaintenancePolicy.recalculate(java.util.List.of(first,sandbox,trial,last));
        assertEquals(at.plusMonths(32),last.getMaintenanceUntil());
    }
    private MarketPluginLicense grant(String order,LocalDateTime at) {
        var l=paid();l.setCreateTime(at);l.setSourceOrderNo(order);l.setPurchaseSandbox(0);return l;
    }
    private MarketPluginLicense paid() {
        var license = new MarketPluginLicense(); license.setLicenseType("PAID"); license.setStatus(1); return license;
    }
}
