package com.houjiang.commons.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 统一配置类
 * 所有服务继承此配置
 * 
 * @author Amos
 * @since 2024-01-01
 */
@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", matchIfMissing = true)
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 3.0 文档信息
     * 
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 配置安全方案
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT认证，格式: Bearer {token}")))
                // 配置 API 信息
                .info(new Info()
                        .title("HouCloud API Documentation")
                        .version("1.0.0")
                        .description("HouCloud 微服务API文档\n\n" +
                                "## 认证说明\n" +
                                "1. 点击右上角 \"Authorize\" 按钮\n" +
                                "2. 输入 JWT Token (格式: Bearer {token})\n" +
                                "3. 点击 \"Authorize\" 确认\n\n" +
                                "## 接口说明\n" +
                                "- 所有接口统一使用 Result<T> 包装返回结果\n" +
                                "- 分页查询统一使用 Page<T> 返回类型\n" +
                                "- 异常情况统一通过全局异常处理器处理")
                        .contact(new Contact()
                                .name("HouCloud Team")
                                .email("support@houcloud.com")
                                .url("https://www.houcloud.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                // 全局安全要求
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"));
    }
}

