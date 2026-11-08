package com.houjiang.base.plugin;

import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ProductAccessServiceTest {
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-20T00:00:00Z"), ZoneOffset.UTC);
    @Test void rejectsUnknownProductWithoutReadingStore() {
        var service = new ProductAccessService((code, instance) -> { throw new AssertionError(); }, clock);
        assertThatThrownBy(() -> service.snapshot("unknown", "org-1")).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void disabledInstallationCannotBeEnabledByLicense() {
        var service = new ProductAccessService((code, instance) -> new ProductAccessService.State(false, "r1", clock.millis()+100000), clock);
        assertThat(service.snapshot("oa", "org-1").enabled()).isFalse();
    }
    @Test void snapshotNeverOutlivesLicenseOrThirtySeconds() {
        var service = new ProductAccessService((code, instance) -> new ProductAccessService.State(true, "r2", clock.millis()+5000), clock);
        assertThat(service.snapshot("oa", "org-1").validUntil()).isEqualTo(clock.millis()+5000);
    }
    @Test void missingInstanceCannotFallBackToGlobalLicense() {
        var service = new ProductAccessService((code, instance) -> { throw new AssertionError(); }, clock);
        assertThatThrownBy(() -> service.snapshot("oa", "")).isInstanceOf(IllegalArgumentException.class);
    }
}
