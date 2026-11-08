package com.houjiang.mall.service.market;

/**
 * 制品回源地址解析器：把 {@code hou_market_plugin_version.jar_url} 里的
 * 「规范指针」换成带令牌、可直接下载的地址。
 *
 * <p><b>为什么需要这层接口</b>：上传侧写入的是
 * {@code /base/plugin-center/artifact/raw?aid=xxx}（规范指针，不含令牌、相对路径），
 * 而安装侧的下载器只认 {@code http(s)://} / {@code file:}。缺了换票这一步，
 * 市场在线安装会 100% 失败在「不支持的安装包地址协议」。
 *
 * <p>换票逻辑本身住在 plugin-artifact（令牌服务、授权判定、制品表都在那边），
 * 而 plugin-artifact 已经依赖 plugin-market，因此这里只放接口：
 * market 侧面向接口调用，artifact 侧提供实现，避免 Maven 循环依赖。
 *
 * @author Amos
 * @since 2026-10-27
 */
public interface ArtifactUrlResolver {

    /**
     * 换取某插件某版本的可下载地址（含短期令牌）。
     *
     * @param pluginCode 插件编码
     * @param version    版本号
     * @return 带令牌的回源地址（相对路径，由下载器补全 base 主机）
     * @throws IllegalStateException 未登录、未购买、制品不存在或签发失败
     */
    String resolveTokenizedUrl(String pluginCode, String version);
}
