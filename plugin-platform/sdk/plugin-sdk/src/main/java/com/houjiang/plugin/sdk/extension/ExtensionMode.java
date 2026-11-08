package com.houjiang.plugin.sdk.extension;

/**
 * Describes how an extension participates in an extension point.
 *
 * <p>{@link #APPEND} adds a hook to the ordered chain. {@link #REPLACE}
 * explicitly replaces the official implementation for the same extension
 * point while leaving other non-official hooks available. Replacement is
 * intentionally exclusive: two custom replacements for one point are a
 * startup error instead of an ordering decision.</p>
 */
public enum ExtensionMode {

    /** Execute alongside the other implementations in priority order. */
    APPEND,

    /** Suppress official implementations for this extension point. */
    REPLACE
}
