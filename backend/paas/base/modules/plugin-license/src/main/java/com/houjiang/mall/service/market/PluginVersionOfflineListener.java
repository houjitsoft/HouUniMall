package com.houjiang.mall.service.market;

import com.houjiang.commons.event.PluginVersionOfflineEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Catalog withdrawal blocks new delivery; it cannot revoke purchased runtime or source rights. */
@Slf4j
@Component
public class PluginVersionOfflineListener {
    public static final String REVOKE_REASON = PluginVersionOfflineEvent.REASON_EMERGENCY;
    public PluginVersionOfflineListener(PluginLicenseService licenseService) { }

    @EventListener
    public void onVersionOffline(PluginVersionOfflineEvent event) {
        if (event == null || !StringUtils.hasText(event.getPluginCode())) return;
        log.warn("版本已下架，阻止新交付并保留既有授权: plugin={} version={}",event.getPluginCode(),event.getVersion());
    }
}
