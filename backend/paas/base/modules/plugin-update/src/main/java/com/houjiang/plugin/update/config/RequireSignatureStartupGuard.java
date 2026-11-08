package com.houjiang.plugin.update.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 生产上线闸门：开启 {@code houcloud.update.require-signature} 时公钥表必须非空，
 * 否则启动即失败，避免「以为验签了、其实装不上 / 或默认关着放行」。
 */
@Component
public class RequireSignatureStartupGuard {

    private static final Logger log = LoggerFactory.getLogger(RequireSignatureStartupGuard.class);

    private final UpdateProperties updateProperties;

    @Value("${houcloud.artifact.sign.public-keys:}")
    private String publicKeys;

    public RequireSignatureStartupGuard(UpdateProperties updateProperties) {
        this.updateProperties = updateProperties;
    }

    @PostConstruct
    public void validate() {
        if (!updateProperties.isRequireSignature()) {
            log.warn("houcloud.update.require-signature=false：安装/升级不强制验签。"
                    + "开源上线前请配置 houcloud.artifact.sign.public-keys 并设 HOU_PLUGIN_REQUIRE_SIGNATURE=true");
            return;
        }
        if (!StringUtils.hasText(publicKeys)) {
            // 🔴 2026-10-27：这里原来直接抛异常拦启动。默认值改成 true 之后，
            //    空白机（还没配公钥）会根本起不来 —— 那就把「安全默认」变成了「不可用」。
            //    现在改成：服务照常启动，但安装/升级未签名制品一律拒绝（见
            //    PluginUpdateService#verifySignature），并在日志里给出一次性修复指引。
            log.error("houcloud.update.require-signature=true 但 houcloud.artifact.sign.public-keys 为空："
                    + "所有未签名制品的安装/升级都会被拒绝。修复步骤：\n"
                    + "  1) bash scripts/plugin/gen-sign-keys.sh ~/.unimall/secrets k2026\n"
                    + "  2) 把输出的公钥写进 Nacos base.yaml 的 houcloud.artifact.sign.public-keys\n"
                    + "  3) 重启 base；临时放行（仅内部验证）可设 HOU_PLUGIN_REQUIRE_SIGNATURE=false");
            return;
        }
        log.info("制品强制验签已开启（public-keys 已配置）");
    }
}
