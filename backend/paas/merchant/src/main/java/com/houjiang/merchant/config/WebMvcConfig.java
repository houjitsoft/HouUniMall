package com.houjiang.merchant.config;

import com.houjiang.merchant.common.config.GlobalConfig;
import com.houjiang.merchant.common.util.YmlUtils;
import com.houjiang.merchant.MerchantInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

/**
 * Web配置
 * 已移除SaToken拦截器，认证由SecurityConfig + JwtAuthenticationTokenFilter统一处理
 * 
 * @author Amos
 * @since 2026-04-28
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private MerchantInterceptor merchantInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(merchantInterceptor)
                .addPathPatterns("/merchant/**", "/adminapi/**")
                // 服务间 Feign / 内部查询不走商家后台管理员 ThreadLocal
                .excludePathPatterns(
                        "/merchant/internal/**",
                        // Cloud 商城平台财务：由 Spring Security 校验平台 JWT，不建立商家单店上下文
                        "/merchant/platform-finance/**",
                        // C 端 PC 入驻状态（买家 JWT，非 mall_shop_admin）
                        "/merchant/shopBase/info/shop/status/query");
    }

    /**
     * 配置允许跨域
     * 
     * @param registry CORS注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "DELETE", "PUT")
                .maxAge(3600);
    }

    /**
     * 资源目录映射
     * 
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(@NotNull ResourceHandlerRegistry registry) {
        String directory = YmlUtils.get("merchant.upload-directory");
        registry.addResourceHandler("/"+ GlobalConfig.shopPublicPrefix +"/**")
                .addResourceLocations("file:" + directory);
    }

}
