package com.houjiang.base.plugin;

import java.time.Clock;
import java.util.Set;

/** Bounded authorization snapshot. A revoked/disabled state propagates within 30 seconds. */
public final class ProductAccessService {
    private static final Set<String> PRODUCTS = Set.of("crm","cms","erp","finance","geo","im","logging","mes","oa","orderfood","pm","rider","supplier","webpos");
    public record State(boolean enabled, String revision, long licenseUntil) {}
    public record Snapshot(boolean enabled, String revision, long validUntil) {}
    @FunctionalInterface public interface Store { State read(String code, String instance); }
    private final Store store;
    private final Clock clock;
    public ProductAccessService(Store store, Clock clock) { this.store = store; this.clock = clock; }
    public Snapshot snapshot(String code, String instance) {
        if (!PRODUCTS.contains(code) || instance == null || instance.isBlank()) throw new IllegalArgumentException("Unknown product or empty instance");
        State state = store.read(code, instance);
        long now = clock.millis();
        boolean enabled = state.enabled() && state.licenseUntil() > now;
        return new Snapshot(enabled, state.revision(), enabled ? Math.min(now+30_000, state.licenseUntil()) : now+5_000);
    }
}
