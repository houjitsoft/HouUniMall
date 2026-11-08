package com.houjiang.auth.login.channel;

/**
 * 统一登录渠道执行器（SPI）
 *
 * <p>默认实现 {@link ConfigurableOAuthChannelProvider} 已覆盖标准 OAuth2 授权码流程，
 * 绝大多数渠道（微信/企业微信/钉钉/QQ/飞书）靠配置即可接入，无需新增实现类。</p>
 *
 * <p>只有流程确实不标准的渠道才需要另写实现：把它注册为 Spring Bean，
 * {@link LoginChannelRegistry} 会自动发现；若同时存在配置定义的默认实现，
 * 注册为 Bean 的实现优先级更高。</p>
 *
 * @author Amos
 */
public interface LoginChannelProvider {

    /** 渠道编码，与配置键、接口路径参数一致 */
    String code();

    /** 渠道显示名 */
    String displayName();

    /** 凭据是否配置完整；false 时登录页不展示该入口 */
    boolean isConfigured();

    /** 未配置时的用户可读提示 */
    String notConfiguredMessage();

    /**
     * 生成授权页地址（即二维码内容/跳转地址）
     *
     * @param state 本次登录会话标识，同时承担 CSRF 校验作用
     */
    String buildAuthorizeUrl(String state);

    /**
     * 用授权码换取用户身份
     *
     * @param code 第三方下发的授权临时票据
     * @return 渠道无关的用户身份
     * @throws LoginChannelException 换取失败（错误码、网络异常等），message 可直接展示
     */
    LoginIdentity exchange(String code);
}
