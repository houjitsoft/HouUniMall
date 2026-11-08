package com.houjiang.permission.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.Duration;

/** Runtime policy for permission-center management API authorization. */
@Component
@ConfigurationProperties(prefix = "houcloud.permission.authorization")
public class PermissionAuthorizationProperties {

    private static final Duration MAX_OBSERVATION_WINDOW = Duration.ofHours(72);

    public enum Mode {
        OBSERVE,
        ENFORCE
    }

    private Mode mode = Mode.ENFORCE;
    private Instant observeUntil;

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode == null ? Mode.ENFORCE : mode;
    }

    public Instant getObserveUntil() {
        return observeUntil;
    }

    public void setObserveUntil(Instant observeUntil) {
        this.observeUntil = observeUntil;
    }

    public boolean isObservationActive(Clock clock) {
        Instant now = Instant.now(clock);
        return mode == Mode.OBSERVE
                && observeUntil != null
                && observeUntil.isAfter(now)
                && !observeUntil.isAfter(now.plus(MAX_OBSERVATION_WINDOW));
    }
}
