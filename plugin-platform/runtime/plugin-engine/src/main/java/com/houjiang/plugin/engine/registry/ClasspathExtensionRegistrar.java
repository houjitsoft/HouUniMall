package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.sdk.annotation.HouExtension;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Arrays;

/**
 * Registers build-time extensions that are supplied by the host classpath.
 *
 * <p>Commercial PF4J plugins are registered by {@code PluginLifecycleManager}.
 * Build-time customizations use Spring auto-configuration to expose the same
 * annotated implementations as beans. This registrar runs once, after all
 * singleton beans are available, and feeds the same immutable registry used by
 * runtime plugins. It deliberately does not scan source folders or manipulate
 * Spring's bean selection, so classpath order cannot silently override Core.
 */
public final class ClasspathExtensionRegistrar implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(ClasspathExtensionRegistrar.class);

    private final ConfigurableListableBeanFactory beanFactory;
    private final ExtensionRegistry registry;
    private final boolean enabled;

    public ClasspathExtensionRegistrar(ConfigurableListableBeanFactory beanFactory,
                                       ExtensionRegistry registry,
                                       boolean enabled) {
        this.beanFactory = beanFactory;
        this.registry = registry;
        this.enabled = enabled;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!enabled) {
            log.debug("Classpath extension registration is disabled");
            return;
        }

        String[] beanNames = beanFactory.getBeanNamesForAnnotation(HouExtension.class);
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            registerBean(beanName);
        }
        if (beanNames.length > 0) {
            log.info("Registered {} build-time extension bean(s)", beanNames.length);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerBean(String beanName) {
        try {
            HouExtension annotation = beanFactory.findAnnotationOnBean(beanName, HouExtension.class);
            if (annotation == null) {
                return;
            }
            Class<?> pointType = annotation.point();
            if (!ExtensionPoint.class.isAssignableFrom(pointType)) {
                log.warn("Ignoring classpath extension {}: point {} is not an ExtensionPoint",
                        beanName, pointType.getName());
                return;
            }

            Object instance = beanFactory.getBean(beanName);
            if (!pointType.isInstance(instance)) {
                log.warn("Ignoring classpath extension {}: bean type {} does not implement {}",
                        beanName, instance.getClass().getName(), pointType.getName());
                return;
            }

            String source = annotation.code();
            if (source == null || source.isBlank()) {
                if ("custom".equalsIgnoreCase(annotation.owner())) {
                    throw new IllegalStateException("Build-time custom extension must declare a stable code: "
                            + beanName);
                }
                source = "classpath:" + beanName;
            } else if (!source.matches("[a-z0-9][a-z0-9._-]*")) {
                throw new IllegalStateException("Build-time extension code must use lowercase stable format: "
                        + source + " (bean=" + beanName + ")");
            }
            registry.register((Class) pointType, (ExtensionPoint<?>) instance,
                    source, annotation.priority(), annotation.owner(), annotation.mode());
            log.info("Registered classpath extension {} for {} (owner={}, mode={}, priority={}, source={})",
                    beanName, pointType.getSimpleName(), annotation.owner(),
                    annotation.mode(), annotation.priority(), source);
        } catch (RuntimeException ex) {
            // A bad customization must fail the host startup rather than leave
            // a partially populated extension snapshot in production.
            throw new IllegalStateException("Failed to register classpath extension bean: " + beanName, ex);
        }
    }
}
