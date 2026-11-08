package com.houjiang.base.service.impl;

import com.houjiang.base.service.IOrderfoodConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Amos
 */
@Service
public class OrderfoodConfigServiceImpl implements IOrderfoodConfigService {

    @Override
    public Object get(String key) {
        // 临时返回模拟数据，实际应该从数据库获取
        Map<String, Object> config = new HashMap<>();
        
        // 系统配置
        config.put("timeout", "7200");
        config.put("default_points", "100");
        config.put("appid", "wx123456789");
        config.put("appsecret", "secret123456");
        config.put("mchid", "1234567890");
        config.put("points", "1");
        config.put("addup", "1");
        config.put("max", "10000");
        config.put("tel", "400-123-4567");
        config.put("pact", "1");
        config.put("avatar", "https://example.com/avatar.jpg");
        config.put("username", "点餐系统");
        config.put("score", "100");
        
        // 升级配置
        config.put("upgrade", "[{\"key\":1,\"value\":100},{\"key\":2,\"value\":500},{\"key\":3,\"value\":1000}]");
        
        // 图片配置
        config.put("img", "[\"https://example.com/img1.jpg\",\"https://example.com/img2.jpg\"]");
        
        // 分享配置
        config.put("share_title", "点餐系统");
        config.put("share_route", "/pages/index/index");
        
        // 内容图片
        config.put("con_img", "[\"https://example.com/con1.jpg\",\"https://example.com/con2.jpg\"]");
        
        // 功能开关
        config.put("iszq", "1");
        config.put("iswm", "1");
        config.put("isyy", "1");
        config.put("ists", "1");
        config.put("isqb", "1");
        config.put("ispayh", "1");
        
        // 其他配置
        config.put("app_filings", "备案号123456");
        config.put("mini_filings", "小程序备案号");
        config.put("business_license", "营业执照号");
        config.put("refund_switch", "1");
        config.put("cwf", "1");
        config.put("iscwf", "1");
        config.put("isnews", "1");
        config.put("login_type", "1");
        config.put("tx_type", "[1,2,3]");
        config.put("is_shlogin", "1");

        if (key == null) {
            return config;
        } else {
            return config.get(key);
        }
    }

    @Override
    public Map<Integer, Integer> getUpgrade() {
        Map<Integer, Integer> map = new HashMap<>();
        map.put(1, 100);
        map.put(2, 500);
        map.put(3, 1000);
        return map;
    }
}
