package com.houjiang.plugin.engine.core;

import org.pf4j.ExtensionFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * 扩展点实例工厂：委托 Spring 创建并注入宿主 Bean。
 *
 * <p>无 <code>pf4j-spring</code> 时的等价实现。PF4J 在发现扩展点类后调用 {@link #create(Class)}，
 * 这里转交宿主的 {@link AutowireCapableBeanFactory}，使插件扩展点可直接 {@code @Autowired} 宿主服务。
 *
 * @author HouCloud
 */
public class PluginExtensionFactory implements ExtensionFactory {

    private final AutowireCapableBeanFactory beanFactory;

    public PluginExtensionFactory(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public <T> T create(Class<T> extensionClass) {
        return beanFactory.createBean(extensionClass);
    }
}
