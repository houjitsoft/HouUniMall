package com.houjiang.base.plugin;

import com.houjiang.mall.service.plugin.PluginSupplementalMenuPort;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Connects mall-advertising installation state to the merchant-owned bcenter menu. */
@Component
public class AdvertisingMerchantMenuLifecycleContributor implements PluginSupplementalMenuPort {

    static final String PLUGIN_CODE = "mall-advertising";

    private final MerchantAdvertisingMenuFeign client;

    public AdvertisingMerchantMenuLifecycleContributor(MerchantAdvertisingMenuFeign client) {
        this.client = client;
    }

    @Override
    public void sync(String pluginCode) {
        transition(pluginCode, "sync");
    }

    @Override
    public void enable(String pluginCode) {
        transition(pluginCode, "sync");
    }

    @Override
    public void disable(String pluginCode) {
        transition(pluginCode, "disable");
    }

    @Override
    public void remove(String pluginCode) {
        transition(pluginCode, "remove");
    }

    private void transition(String pluginCode, String transition) {
        if (!PLUGIN_CODE.equals(pluginCode)) {
            return;
        }
        Map<String, Object> response = client.transition(transition);
        Object code = response == null ? null : response.get("code");
        if (!(code instanceof Number number) || number.intValue() != 1) {
            String message = response == null ? "无响应" : String.valueOf(response.getOrDefault("msg", "未知错误"));
            throw new IllegalStateException("商家端广告菜单 " + transition + " 失败: " + message);
        }
    }
}
