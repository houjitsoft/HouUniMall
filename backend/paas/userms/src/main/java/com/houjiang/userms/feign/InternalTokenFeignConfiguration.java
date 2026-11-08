package com.houjiang.userms.feign;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 给 userms 的服务间 Feign 调用带上内部令牌。
 *
 * <p><b>为什么需要</b>：注册链路里 userms 会调 base 的
 * {@code /organization/ensurePersonalOrganization} 给新用户建「个人空间」，
 * 那时<b>没有任何登录态</b>。base 侧 2026-10-27 给这类「按显式 userId 操作」的接口
 * 加了闸门（内部令牌 或 本人），所以这里必须把令牌带上，否则注册时个人空间建不出来
 * （只会打一条 warn，用户在组织列表里看不到自己的空间）。
 *
 * <p>令牌取自 Nacos 共享配置 {@code mutual.yaml} 的
 * {@code houcloud.plugin.internal-job-token}，与 job / geo / oa 的 Feign 同一份。
 * 没配时不加头，base 侧退回「只允许本人」，属可预期的降级（不会伪造身份）。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Configuration
public class InternalTokenFeignConfiguration {

    private final String internalJobToken;

    public InternalTokenFeignConfiguration(
            @Value("${houcloud.plugin.internal-job-token:}") String internalJobToken) {
        this.internalJobToken = internalJobToken;
    }

    @Bean
    public RequestInterceptor internalJobTokenInterceptor() {
        return template -> {
            if (StringUtils.hasText(internalJobToken)
                    && !template.headers().containsKey("X-Internal-Job-Token")) {
                template.header("X-Internal-Job-Token", internalJobToken);
            }
        };
    }
}
