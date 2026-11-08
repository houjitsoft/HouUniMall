package com.houjiang.plugin.engine.core;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringBootConfiguration;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the Spring context for each PF4J plugin.
 *
 * <p>Plugin classes must not be copied into Mall's application context. A
 * child context gives every plugin its own bean definitions and class loader,
 * while still allowing plugin services to use the host's infrastructure
 * beans (data source, transaction manager, security and shared services).
 */
public final class PluginSpringContextManager {

    private final ApplicationContext hostContext;
    private final Map<String, AnnotationConfigApplicationContext> contexts = new ConcurrentHashMap<>();

    public PluginSpringContextManager(ApplicationContext hostContext) {
        this.hostContext = hostContext;
    }

    public AnnotationConfigApplicationContext start(String code, PluginWrapper wrapper) {
        AnnotationConfigApplicationContext existing = contexts.get(code);
        if (existing != null) {
            return existing;
        }
        ClassLoader loader = wrapper.getPluginClassLoader();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setParent(hostContext);
        context.setClassLoader(loader);

        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        List<Class<?>> feignConfigurationTypes = new ArrayList<>();
        List<Class<?>> feignClientTypes = new ArrayList<>();
        List<Class<?>> mapperTypes = new ArrayList<>();
        try {
            // PF4J's class loader is isolated from the host class path. Register
            // candidates from the actual plugin archive so @Service/@Controller
            // discovery is deterministic for both JAR and exploded plugins.
            for (String className : classNames(wrapper)) {
                Class<?> type;
                try {
                    type = Class.forName(className, false, loader);
                } catch (LinkageError | ClassNotFoundException ignored) {
                    continue;
                }
                if (hasFeignClientsAnnotation(type)) {
                    feignConfigurationTypes.add(type);
                }
                if (isFeignClient(type)) {
                    feignClientTypes.add(type);
                }
                // 只收集「本插件 jar」里的 Mapper 接口。禁止再用 MapperScannerConfigurer
                // 按包名 classpath* 扫描：多个嵌入式插件共用 com.houjiang.activity.mapper
                // 时，扫描器会把兄弟插件 jar 里的 Mapper 也登记进当前子上下文，随后
                // MapperFactoryBean 用本插件 ClassLoader 去加载兄弟类 →
                // Could not find class [...ActivityRedpacketBaseMapper]（2026-11-01
                // userms 已装红包再装抽奖转盘实锤）。
                if (type.isInterface() && type.getName().endsWith("Mapper")
                        && type.getPackageName().endsWith(".mapper")) {
                    mapperTypes.add(type);
                }
                if (type.isInterface() || type.isAnnotation() || type.isEnum()
                        || type.isAnnotationPresent(SpringBootApplication.class)
                        || type.isAnnotationPresent(SpringBootConfiguration.class)
                        || !org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation(type, Component.class)) {
                    continue;
                }
                AnnotatedGenericBeanDefinition candidate = new AnnotatedGenericBeanDefinition(type);
                String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(candidate, context.getDefaultListableBeanFactory());
                if (!context.containsBeanDefinition(beanName)) {
                    context.registerBeanDefinition(beanName, candidate);
                }
            }
            // Explicit registration above is required for PF4J isolation;
            // do not run a host class-path scan here (Mall also contains
            // com.houjiang.* packages and would mix them into this context).
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
        }

        // Plugin applications keep @EnableFeignClients on their excluded
        // Spring Boot entry class. Re-apply that registrar to the child
        // context without importing the full host auto-configuration graph.
        // Sentinel's Feign builder resolves the registrar factory bean from the
        // root ApplicationContext. Keep the actual client definitions there;
        // the plugin child context inherits them through its parent.
        registerFeignClients(hostContext, loader, feignConfigurationTypes, feignClientTypes);

        // Plugin mapper interfaces are intentionally not Spring components.
        // Register each interface found in THIS plugin jar as MapperFactoryBean
        // against the host SqlSessionFactory (inherited via parent context).
        registerMapperInterfaces(context, mapperTypes);

        // MapperFactoryBean only binds interfaces. XML statements live inside
        // the isolated plugin archive, so explicitly parse them into the host
        // session factory before the child context refreshes.
        registerMapperXmlResources(code, wrapper, loader);

        previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            context.refresh();
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
        }
        AnnotationConfigApplicationContext raced = contexts.putIfAbsent(code, context);
        if (raced != null) {
            context.close();
            return raced;
        }
        return context;
    }

    /**
     * 按「本插件已枚举的 Mapper Class」显式注册，避免包扫描跨插件串味。
     */
    private static void registerMapperInterfaces(AnnotationConfigApplicationContext context,
                                                 List<Class<?>> mapperTypes) {
        if (mapperTypes == null || mapperTypes.isEmpty()) {
            return;
        }
        for (Class<?> mapperType : mapperTypes) {
            String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(
                    new AnnotatedGenericBeanDefinition(mapperType),
                    context.getDefaultListableBeanFactory());
            if (context.containsBeanDefinition(beanName)) {
                continue;
            }
            // 对齐 ClassPathMapperScanner.processBeanDefinitions：
            // sqlSessionFactoryBeanName 只是扫描器字段；真正落到 MapperFactoryBean 上的是
            // 属性 sqlSessionFactory = RuntimeBeanReference("sqlSessionFactory")（子上下文可
            // 从父宿主解析）。直接写 sqlSessionFactoryBeanName 会 NotWritablePropertyException。
            AbstractBeanDefinition definition = BeanDefinitionBuilder
                    .genericBeanDefinition(MapperFactoryBean.class)
                    .addConstructorArgValue(mapperType)
                    .addPropertyValue("mapperInterface", mapperType)
                    .addPropertyValue("addToConfig", Boolean.TRUE)
                    .addPropertyValue("sqlSessionFactory", new RuntimeBeanReference("sqlSessionFactory"))
                    .setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE)
                    .getBeanDefinition();
            definition.setAttribute("factoryBeanObjectType", mapperType);
            context.registerBeanDefinition(beanName, definition);
        }
    }

    private void registerMapperXmlResources(String code, PluginWrapper wrapper, ClassLoader loader) {
        SqlSessionFactory sessionFactory = hostContext.getBeanProvider(SqlSessionFactory.class).getIfAvailable();
        if (sessionFactory == null) {
            return;
        }
        Configuration configuration = sessionFactory.getConfiguration();
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try (JarFile jar = new JarFile(wrapper.getPluginPath().toFile())) {
            jar.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> entry.getName().startsWith("mapper/") && entry.getName().endsWith(".xml"))
                    .forEach(entry -> parseMapperXml(code, jar, entry, configuration));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot enumerate plugin mapper XML: " + code, e);
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
        }
    }

    private static void parseMapperXml(String code, JarFile jar, JarEntry entry,
                                       Configuration configuration) {
        String resource = "plugin:" + code + "!/" + entry.getName();
        if (configuration.isResourceLoaded(resource)) {
            return;
        }
        try (InputStream input = jar.getInputStream(entry)) {
            XMLMapperBuilder parser = new XMLMapperBuilder(
                    input, configuration, resource, configuration.getSqlFragments());
            parser.parse();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot parse plugin mapper XML: " + resource, e);
        }
    }

    private static boolean hasFeignClientsAnnotation(Class<?> type) {
        try {
            Class<?> annotation = Class.forName(
                    "org.springframework.cloud.openfeign.EnableFeignClients", false,
                    type.getClassLoader());
            @SuppressWarnings("unchecked")
            Class<? extends java.lang.annotation.Annotation> annotationType =
                    (Class<? extends java.lang.annotation.Annotation>) annotation;
            return type.isAnnotationPresent(annotationType);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean isFeignClient(Class<?> type) {
        if (!type.isInterface()) {
            return false;
        }
        try {
            Class<?> annotation = Class.forName(
                    "org.springframework.cloud.openfeign.FeignClient", false,
                    type.getClassLoader());
            return type.isAnnotationPresent(annotation.asSubclass(java.lang.annotation.Annotation.class));
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    private static void registerFeignClients(ApplicationContext context,
                                             ClassLoader loader,
                                             List<Class<?>> configurationTypes,
                                             List<Class<?>> clientTypes) {
        if (configurationTypes.isEmpty() && clientTypes.isEmpty()) {
            return;
        }
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            if (!(context instanceof BeanDefinitionRegistry registry)) {
                throw new IllegalStateException("Plugin Feign registration requires a bean-definition registry");
            }
            Class<?> registrarType = Class.forName(
                    "org.springframework.cloud.openfeign.FeignClientsRegistrar", false, loader);
            var constructor = registrarType.getDeclaredConstructor();
            var resourceLoader = registrarType.getDeclaredMethod("setResourceLoader",
                    org.springframework.core.io.ResourceLoader.class);
            var environment = registrarType.getDeclaredMethod("setEnvironment",
                    org.springframework.core.env.Environment.class);
            var register = registrarType.getDeclaredMethod("registerBeanDefinitions",
                    org.springframework.core.type.AnnotationMetadata.class, BeanDefinitionRegistry.class);
            constructor.setAccessible(true);
            resourceLoader.setAccessible(true);
            environment.setAccessible(true);
            register.setAccessible(true);
            Object registrar = constructor.newInstance();
            resourceLoader.invoke(registrar, context);
            environment.invoke(registrar, context.getEnvironment());
            for (Class<?> type : configurationTypes) {
                register.invoke(registrar, new StandardAnnotationMetadata(type), registry);
            }
            // The registrar's package scanner uses the host class path in some
            // PF4J class-loader combinations. Register each discovered client
            // directly so the child context always contains its
            // FeignClientFactoryBean definition before any service is created.
            if (!clientTypes.isEmpty()) {
                Class<?> feignClient = Class.forName(
                        "org.springframework.cloud.openfeign.FeignClient", false, loader);
                var clientRegister = registrarType.getDeclaredMethod("registerFeignClient",
                        BeanDefinitionRegistry.class, org.springframework.core.type.AnnotationMetadata.class,
                    Map.class);
                clientRegister.setAccessible(true);
                String annotationName = feignClient.getCanonicalName();
                for (Class<?> type : clientTypes) {
                    if (registry.containsBeanDefinition(type.getName())) {
                        continue;
                    }
                    StandardAnnotationMetadata metadata = new StandardAnnotationMetadata(type);
                    Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationName, false);
                    if (attributes != null) {
                        clientRegister.invoke(registrar, registry, metadata, attributes);
                    }
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            throw new IllegalStateException("Failed to register plugin Feign clients", ex);
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
        }
    }

    private static List<String> classNames(PluginWrapper wrapper) {
        List<String> names = new ArrayList<>();
        try (JarFile jar = new JarFile(wrapper.getPluginPath().toFile())) {
            jar.stream().filter(e -> !e.isDirectory() && e.getName().endsWith(".class"))
                    .forEach(e -> names.add(e.getName().substring(0, e.getName().length() - 6).replace('/', '.')));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot enumerate plugin classes: " + wrapper.getPluginId(), e);
        }
        return names;
    }

    public org.springframework.beans.factory.config.AutowireCapableBeanFactory beanFactory(String code) {
        AnnotationConfigApplicationContext context = contexts.get(code);
        if (context == null) {
            throw new IllegalStateException("Plugin Spring context is not active: " + code);
        }
        return context.getAutowireCapableBeanFactory();
    }

    public void stop(String code) {
        AnnotationConfigApplicationContext context = contexts.remove(code);
        if (context != null) {
            context.close();
        }
    }
}
