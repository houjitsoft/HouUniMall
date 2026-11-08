package com.houjiang.plugin.engine.spi;

/**
 * Resolves a validated business service name through trusted discovery/configuration.
 * Never derive an endpoint from plugin manifest host URLs or browser parameters.
 */
@FunctionalInterface
public interface PluginServiceEndpointResolver {
    /** @return an HTTP(S) service endpoint, or null when the service is unavailable. */
    String resolve(String hostService);
}
