package com.houjiang.merchant.persistence;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.houjiang.merchant.common.core.basics.IBaseMapper;
import com.houjiang.merchant.common.entity.RechargeOrder;
import com.houjiang.merchant.common.entity.log.UserAccountLog;
import com.houjiang.merchant.common.entity.mall.MallShopAccountPeriod;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchantTableMappingTest {

    private static final String TABLE_PREFIX_PROPERTY =
            "mybatis-plus.global-config.db-config.table-prefix";
    private static final String EXPECTED_TABLE_PREFIX = "hou_merchant_";
    private static final Set<String> NON_PERSISTENT_ENTITY_CLASS_NAMES = Set.of(
            "com.houjiang.merchant.common.entity.server.Cpu",
            "com.houjiang.merchant.common.entity.server.Disk",
            "com.houjiang.merchant.common.entity.server.Jvm",
            "com.houjiang.merchant.common.entity.server.Mem",
            "com.houjiang.merchant.common.entity.server.Sys");

    @Test
    void everyMapperEntityUsesTheConfiguredMerchantTablePrefix() throws Exception {
        String configuredPrefix = loadConfiguredTablePrefix();
        Set<Class<?>> mapperEntities = scanMapperEntities();
        Set<Class<?>> entityClasses = scanEntityClasses();
        Set<Class<?>> persistentEntities = entityClasses.stream()
                .filter(type -> !NON_PERSISTENT_ENTITY_CLASS_NAMES.contains(type.getName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> discoveredNonPersistentEntities = entityClasses.stream()
                .map(Class::getName)
                .filter(NON_PERSISTENT_ENTITY_CLASS_NAMES::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Class<?>> entitiesMissingMapper = new LinkedHashSet<>(persistentEntities);
        entitiesMissingMapper.removeAll(mapperEntities);
        Set<Class<?>> mapperEntitiesOutsideExpectedSet = new LinkedHashSet<>(mapperEntities);
        mapperEntitiesOutsideExpectedSet.removeAll(persistentEntities);

        assertAll(
                () -> assertEquals(NON_PERSISTENT_ENTITY_CLASS_NAMES,
                        discoveredNonPersistentEntities,
                        "Every explicitly excluded monitoring DTO must exist"),
                () -> assertTrue(entitiesMissingMapper.isEmpty(),
                        () -> "Persistent entities missing Mapper: "
                                + formatClassNames(entitiesMissingMapper)),
                () -> assertTrue(mapperEntitiesOutsideExpectedSet.isEmpty(),
                        () -> "Mapper entities outside expected persistent set: "
                                + formatClassNames(mapperEntitiesOutsideExpectedSet)));

        try {
            Map<Class<?>, TableInfo> mappings =
                    initializeMappings(persistentEntities, configuredPrefix);
            assertAll(
                    () -> assertEquals(EXPECTED_TABLE_PREFIX, configuredPrefix,
                            "bootstrap.yml must configure the merchant table prefix"),
                    () -> assertEquals("hou_merchant_user_recharge",
                            mappings.get(RechargeOrder.class).getTableName()),
                    () -> assertEquals("hou_merchant_account_log",
                            mappings.get(UserAccountLog.class).getTableName()),
                    () -> assertEquals("hou_merchant_mall_shop_account_period",
                            mappings.get(MallShopAccountPeriod.class).getTableName()),
                    () -> assertAll(
                            mappings.entrySet().stream()
                                    .map(entry -> () -> assertValidTableName(
                                            entry.getKey(), entry.getValue()))));
        } finally {
            persistentEntities.forEach(TableInfoHelper::remove);
        }
    }

    private String formatClassNames(Set<Class<?>> classes) {
        return classes.stream()
                .map(Class::getName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private void assertValidTableName(Class<?> entityClass, TableInfo tableInfo) {
        String tableName = tableInfo.getTableName();
        String mapping = entityClass.getSimpleName() + " -> " + tableName;
        assertAll(
                () -> assertTrue(tableName.startsWith(EXPECTED_TABLE_PREFIX), mapping),
                () -> assertFalse(
                        tableName.startsWith(EXPECTED_TABLE_PREFIX + EXPECTED_TABLE_PREFIX),
                        mapping));
    }

    private String loadConfiguredTablePrefix() throws Exception {
        List<org.springframework.core.env.PropertySource<?>> propertySources =
                new YamlPropertySourceLoader().load(
                        "merchant-bootstrap", new ClassPathResource("bootstrap.yml"));
        assertFalse(propertySources.isEmpty(), "bootstrap.yml did not produce a property source");
        Object value = propertySources.get(0).getProperty(TABLE_PREFIX_PROPERTY);
        return value == null ? "" : value.toString();
    }

    private Set<Class<?>> scanMapperEntities() throws Exception {
        Set<Class<?>> entities = new LinkedHashSet<>();
        for (Class<?> mapperClass : scanClasses(
                "classpath*:com/houjiang/merchant/common/mapper/**/*.class")) {
            if (!mapperClass.isInterface() || !IBaseMapper.class.isAssignableFrom(mapperClass)
                    || mapperClass == IBaseMapper.class) {
                continue;
            }
            Class<?> entityClass = ResolvableType.forClass(mapperClass)
                    .as(IBaseMapper.class)
                    .getGeneric(0)
                    .resolve();
            assertNotNull(entityClass, () -> "Cannot resolve mapper entity type: " + mapperClass.getName());
            entities.add(entityClass);
        }
        return entities;
    }

    private Set<Class<?>> scanEntityClasses() throws Exception {
        return scanClasses("classpath*:com/houjiang/merchant/common/entity/**/*.class").stream()
                .filter(type -> !type.isInterface())
                .filter(type -> !Modifier.isAbstract(type.getModifiers()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Class<?>> scanClasses(String pattern) throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        Set<Class<?>> classes = new LinkedHashSet<>();
        for (Resource resource : resolver.getResources(pattern)) {
            String className = metadataReaderFactory.getMetadataReader(resource)
                    .getClassMetadata()
                    .getClassName();
            if (!className.contains("$")) {
                classes.add(Class.forName(className, false, getClass().getClassLoader()));
            }
        }
        return classes;
    }

    private Map<Class<?>, TableInfo> initializeMappings(
            Set<Class<?>> entityClasses, String configuredPrefix) {
        Configuration configuration = new Configuration();
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setDbConfig(new GlobalConfig.DbConfig().setTablePrefix(configuredPrefix));
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);

        Map<Class<?>, TableInfo> mappings = new LinkedHashMap<>();
        for (Class<?> entityClass : entityClasses) {
            TableInfoHelper.remove(entityClass);
            TableInfo tableInfo = TableInfoHelper.initTableInfo(
                    new MapperBuilderAssistant(configuration, entityClass.getName()), entityClass);
            mappings.put(entityClass, tableInfo);
        }
        return mappings;
    }
}
