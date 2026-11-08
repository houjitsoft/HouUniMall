package com.houjiang.merchant;

import com.houjiang.merchant.common.annotation.MerchantPermission;
import com.houjiang.merchant.controller.ConfigController;
import com.houjiang.merchant.controller.announcement.AnnouncementController;
import com.houjiang.merchant.controller.mall.MallGoodsController;
import com.houjiang.merchant.controller.mall.MallShopGoodsCategoryController;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MerchantPermissionCoverageTest {

    @Test
    void declaredControllerPermissionsExistInTheManagedRoleMenuCatalog() throws Exception {
        Set<String> menuTokens = readManagedRoleMenuTokens();
        Set<Class<?>> fullyMigratedControllers = Set.of(
                MallGoodsController.class,
                MallShopGoodsCategoryController.class,
                AnnouncementController.class);
        Set<String> fullyMigratedHandlers = Set.of(
                ConfigController.class.getSimpleName() + "#getService",
                ConfigController.class.getSimpleName() + "#setService");
        Set<String> missingDeclarations = new HashSet<>();
        Set<String> invalidBindings = new HashSet<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);

        for (Resource resource : resolver.getResources(
                "classpath*:com/houjiang/merchant/controller/**/*.class")) {
            String className = metadataReaderFactory.getMetadataReader(resource)
                    .getClassMetadata().getClassName();
            if (className.contains("$")) {
                continue;
            }
            Class<?> controllerType = Class.forName(
                    className, false, getClass().getClassLoader());
            if (!AnnotatedElementUtils.hasAnnotation(controllerType, RestController.class)) {
                continue;
            }
            for (Method method : controllerType.getDeclaredMethods()) {
                if (AnnotatedElementUtils.findMergedAnnotation(
                        method, RequestMapping.class) == null) {
                    continue;
                }
                MerchantPermission permission = AnnotatedElementUtils.findMergedAnnotation(
                        method, MerchantPermission.class);
                if (permission == null) {
                    permission = AnnotatedElementUtils.findMergedAnnotation(
                            controllerType, MerchantPermission.class);
                }
                String handlerName = controllerType.getSimpleName() + "#" + method.getName();
                if (permission == null || permission.anyOf().length == 0) {
                    if (fullyMigratedControllers.contains(controllerType)
                            || fullyMigratedHandlers.contains(handlerName)) {
                        missingDeclarations.add(handlerName);
                    }
                    continue;
                }
                for (String token : permission.anyOf()) {
                    if (token == null || token.isBlank() || !menuTokens.contains(token)) {
                        invalidBindings.add(handlerName + " -> " + token);
                    }
                }
            }
        }

        assertFalse(menuTokens.isEmpty());
        assertTrue(missingDeclarations.isEmpty(), () ->
                "Fully migrated controllers have unbound routes:\n"
                        + String.join("\n", missingDeclarations.stream().sorted().toList()));
        assertTrue(invalidBindings.isEmpty(), () ->
                "Controller permissions missing from managed menu catalog:\n"
                        + String.join("\n", invalidBindings.stream().sorted().toList()));
    }

    private Set<String> readManagedRoleMenuTokens() throws Exception {
        Set<String> tokens = new HashSet<>();
        new ClassPathResource("merchant-role-menu-access-tokens.txt")
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8)
                .lines()
                .map(String::trim)
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .forEach(tokens::add);
        return tokens;
    }
}
