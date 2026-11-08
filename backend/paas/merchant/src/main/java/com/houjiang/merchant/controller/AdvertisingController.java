package com.houjiang.merchant.controller;

import com.houjiang.merchant.MerchantThreadLocal;
import com.houjiang.merchant.common.annotation.MerchantPermission;
import com.houjiang.merchant.common.core.AjaxResult;
import com.houjiang.merchant.service.AdvertisingMenuLifecycleService;
import com.houjiang.merchant.service.AdvertisingPluginClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Authenticated bcenter adapter. Browser-provided tenant identities are never forwarded. */
@RestController
@RequestMapping("/merchant/advertising")
@MerchantPermission(anyOf = AdvertisingMenuLifecycleService.MENU_ACCESS)
public class AdvertisingController {

    private final AdvertisingPluginClient pluginClient;

    public AdvertisingController(AdvertisingPluginClient pluginClient) {
        this.pluginClient = pluginClient;
    }

    @PostMapping("/list")
    public AjaxResult<Object> list(@RequestBody(required = false) Map<String, Object> ignored) {
        return AjaxResult.success(pluginClient.list(trustedIdentity()));
    }

    @PostMapping("/positions")
    public AjaxResult<Object> positions(@RequestBody(required = false) Map<String, Object> ignored) {
        return AjaxResult.success(pluginClient.positions(Map.of()));
    }

    @PostMapping("/save")
    public AjaxResult<Object> save(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> request = trustedIdentity();
        request.put("application", application(body));
        return AjaxResult.success(pluginClient.save(request));
    }

    @PostMapping("/submit")
    public AjaxResult<Object> submit(@RequestBody(required = false) Map<String, Object> body) {
        return AjaxResult.success(pluginClient.submit(applicationAction(body)));
    }

    @PostMapping("/withdraw")
    public AjaxResult<Object> withdraw(@RequestBody(required = false) Map<String, Object> body) {
        return AjaxResult.success(pluginClient.withdraw(applicationAction(body)));
    }

    private static Map<String, Object> trustedIdentity() {
        int shopId = MerchantThreadLocal.getShopId();
        int adminId = MerchantThreadLocal.getAdminId();
        if (shopId <= 0 || adminId <= 0) {
            throw new IllegalStateException("商家身份无效，请重新登录");
        }
        Map<String, Object> trusted = new LinkedHashMap<>();
        trusted.put("shopId", shopId);
        trusted.put("actorId", adminId);
        return trusted;
    }

    private static Map<String, Object> applicationAction(Map<String, Object> body) {
        Map<String, Object> request = trustedIdentity();
        request.put("applicationId", positiveId(body == null ? null : body.get("applicationId")));
        return request;
    }

    private static long positiveId(Object value) {
        long id;
        if (value instanceof Number number) {
            id = number.longValue();
        } else {
            try {
                id = Long.parseLong(String.valueOf(value));
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException("applicationId 不能为空", exception);
            }
        }
        if (id <= 0) throw new IllegalArgumentException("applicationId 必须大于 0");
        return id;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> application(Map<String, Object> body) {
        if (body == null) return Map.of();
        Object nested = body.get("application");
        if (nested instanceof Map<?, ?> map) {
            return new LinkedHashMap<>((Map<String, Object>) map);
        }
        Map<String, Object> sanitized = new LinkedHashMap<>(body);
        sanitized.remove("shopId");
        sanitized.remove("actorId");
        return sanitized;
    }
}
