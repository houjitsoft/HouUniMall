package com.houjiang.job.feign.support;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * job → OA/GEO 服务插件内部定时任务端点的 Feign 专用配置。
 *
 * <p>只在声明了 {@code configuration = InternalJobFeignConfiguration.class} 的客户端
 * （{@code GeoServiceClient}、{@code OaServiceClient}）上生效，把内部令牌
 * {@code X-Internal-Job-Token} 仅附加到这两条插件任务链路，避免令牌扩散到其他下游服务。
 *
 * <p><b>注意</b>：本类刻意不加 {@code @Configuration}。因为 {@code JobApplication} 的
 * {@code scanBasePackages = "com.houjiang.*"} 会扫描到它，一旦加注解就会升级为「全局
 * Feign 默认配置」，令牌将随所有 Feign 调用外发。保持普通类即可由 Feign 子上下文按客户端装配。
 *
 * @author Amos
 */
public class InternalJobFeignConfiguration {

    /** 内部任务令牌请求头名称，由归属服务的内部任务安全过滤器校验。 */
    public static final String HEADER_INTERNAL_JOB_TOKEN = "X-Internal-Job-Token";

    @Bean
    public RequestInterceptor internalJobTokenRequestInterceptor(
            @Value("${houcloud.plugin.internal-job-token:}") String internalJobToken) {
        String token = internalJobToken == null ? "" : internalJobToken.trim();
        return template -> {
            if (StringUtils.hasText(token)) {
                template.header(HEADER_INTERNAL_JOB_TOKEN, token);
            }
        };
    }
}
