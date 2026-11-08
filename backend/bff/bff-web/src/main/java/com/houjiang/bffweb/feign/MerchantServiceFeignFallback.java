package com.houjiang.bffweb.feign;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Merchant商户服务Feign降级处理类
/**
 * 
 * @description 当Merchant服务不可用时提供降级响应，返回统一的错误提示
 * @author Amos
 * @date 2026-04-28
 */
@Component
public class MerchantServiceFeignFallback implements MerchantServiceFeign {

    /**
     * 构建统一的降级响应
/**
     *
     * <p>与 merchant 端 AjaxResult.failed(code,msg,data,show) 同构：
     * <ul>
     *   <li>code=0 业务失败（不是 code=500，避免前端按 res.code !== 1 拿不到 msg）</li>
     *   <li>show=1 允许前端弹提示</li>
     *   <li>data=[] 避免 null 出现在前端表格中</li>
     * </ul>
     *
     * @param message 错误信息
     * @return Map 降级响应结果
     */
    private Map<String, Object> fallbackResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", message);
        result.put("data", new ArrayList<>());
        result.put("show", 1);
        return result;
    }

    private static final String FALLBACK_MSG = "商户服务暂时不可用，请稍后重试";

    @Override
    public Object login(Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object logout(String token) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object listProvisionedShops(Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object getProvisionedShopDetail(Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object createProvisionedShop(Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object provisionShopOptions() {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object provisionShopAccountOptions(Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object updateProvisionedShopStatus(Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object updateProvisionedShop(Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object advertising(String action, Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object forwardPost(String module, String action, Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object forwardGet(String module, String action, Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object forwardAdminapiPost(String module, String action, Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object forwardAdminapiGet(String module, String action, Map<String, Object> params) {
        return fallbackResponse(FALLBACK_MSG);
    }

    @Override
    public Object uploadFile(String type, MultipartFile file, String cid, String authorization) {
        return fallbackResponse(FALLBACK_MSG);
    }
}
