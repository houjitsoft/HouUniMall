package com.houjiang.plugin.signature;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 制品签名模块装配。
 *
 * <p>本模块刻意只依赖 spring-context：签名算法全部走 JDK 原生 API，装配层只用
 * {@code @Configuration / @Bean / @Value}，不引入任何三方加密库。
 *
 * <p>公钥配置采用「{@code kid=base64} 逗号分隔」的扁平写法，而不是 YAML 嵌套 Map，
 * 原因：嵌套 Map 的读取需要 {@code @ConfigurationProperties}（spring-boot 依赖），
 * 扁平字符串在纯 spring-context 下即可解析，且便于直接写进 Nacos 的 properties 配置。
 *
 * @author HouCloud Plugin Platform
 * @since M0
 */
@Configuration
public class SignatureConfiguration {

    /**
     * 私钥文件路径模板，支持 {@code {kid}} 占位符；为空表示本实例只验签、不签名。
     */
    @Value("${houcloud.artifact.sign.private-key-path:}")
    private String privateKeyPath;

    /**
     * 公钥表，格式 {@code kid1=base64,kid2=base64}。
     */
    @Value("${houcloud.artifact.sign.public-keys:}")
    private String publicKeys;

    /**
     * 注册制品签名器。
     *
     * <p>注意：这里不做任何私钥存在性校验。私钥是懒加载的，缺失只会在真正签名时报错，
     * 以保证「签名密钥未配置」不会让已经在线上跑的 mall 启动失败。
     *
     * @return 制品签名器
     */
    @Bean
    public ArtifactSigner artifactSigner() {
        return new Ed25519ArtifactSigner(privateKeyPath, parsePublicKeys(publicKeys));
    }

    /**
     * 解析 {@code kid=base64} 逗号分隔的公钥配置。
     *
     * @param raw 原始配置串，允许为空
     * @return kid -&gt; base64 公钥；解析失败的单条跳过，不影响其它 kid
     */
    static Map<String, String> parsePublicKeys(String raw) {
        Map<String, String> result = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (String item : raw.split(",")) {
            String trimmed = item.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int idx = trimmed.indexOf('=');
            if (idx <= 0 || idx == trimmed.length() - 1) {
                continue;
            }
            result.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
        }
        return result;
    }
}
