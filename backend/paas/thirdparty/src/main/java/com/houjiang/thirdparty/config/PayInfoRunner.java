package com.houjiang.thirdparty.config;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.ThreadPoolConfig;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.thirdparty.feign.PayMentServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 小程序访问令牌工具类，用于获取和刷新微信小程序的访问令牌。
 * @author Amos
 */
@Component
/** 此注解的作用就是控制类的加载顺序，这个顺序是从小到大的。比如说启动时先去加载Order的value等于1的类，然后去加载等于2的类。 */
@Order(value = 1)
public class PayInfoRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(PayInfoRunner.class);

    /** 引入线程池 */
    private static final ExecutorService executorService = ThreadPoolConfig.executorService;

    @Value("${shopAppId}")
    private String shopAppId;

    @Value("${shopAppIdSecret}")
    private String shopAppIdSecret;

    @Autowired
    private PayMentServiceClient payMentServiceClient;

    @Override
    public void run(ApplicationArguments args) {
        LOG.info("=== pay 开始加载支付配置");
        if (!initPayConfig()) {
            LOG.warn("=== 支付配置加载失败，payment服务可能未启动，跳过重试");
            // 不再无限重试，避免日志刷屏
        }
    }

    /**
     * 查询微信访问令牌。
     *
     * @return 访问令牌字符串。
     */
    public Boolean initPayConfig() {
        try {
            String pay_config = RedisUtil.getNoUpdateTime(HouConfig.PAY_CONFIG);
            Map<String, String> map = new HashMap<>();
            if (StringUtils.isEmpty(pay_config)) {
                // 查询支付方式配置，获取小程序的APPID和密钥
                Map<String, String> param = new HashMap<>(1);
                param.put("paymentMode", "02");
                ResponseResult resultResp = payMentServiceClient.selectPayConfig(param);
                
                // 检查返回结果和数据的有效性
                if (resultResp == null || resultResp.getData() == null) {
                    LOG.error("=== 获取小程序支付配置失败：返回结果为空");
                    return false;
                }
                
                Map<String, String> outMap2 = (Map<String, String>) resultResp.getData();
                if (outMap2 == null || outMap2.isEmpty()) {
                    LOG.error("=== 获取小程序支付配置失败：配置数据为空");
                    return false;
                }
                
                WXPayConfig.Applet_APP_ID = outMap2.get("appID");
                WXPayConfig.Applet_APP_SECRET = outMap2.get("appSecret");

                // 获取微信公众号的支付配置
                param.put("paymentMode", "04");
                ResponseResult resultResp1 = payMentServiceClient.selectPayConfig(param);
                
                // 检查返回结果和数据的有效性
                if (resultResp1 == null || resultResp1.getData() == null) {
                    LOG.error("=== 获取微信公众号支付配置失败：返回结果为空");
                    return false;
                }
                
                Map<String, String> outMap1 = (Map<String, String>) resultResp1.getData();
                if (outMap1 == null || outMap1.isEmpty()) {
                    LOG.error("=== 获取微信公众号支付配置失败：配置数据为空");
                    return false;
                }
                
                WXPayConfig.GZ_APP_ID = outMap1.get("appID");
                WXPayConfig.GZ_APP_SECRET = outMap1.get("appSecret");

                map.putAll(outMap2);
                map.put("gz_appID", outMap1.get("appID"));
                map.put("gz_appSecret", outMap1.get("appSecret"));
            } else {
                map = JSON.parseObject(pay_config, Map.class);
                if (map != null) {
                    WXPayConfig.Applet_APP_ID = map.get("appID");
                    WXPayConfig.Applet_APP_SECRET = map.get("appSecret");
                    WXPayConfig.GZ_APP_ID = map.get("gz_appID");
                    WXPayConfig.GZ_APP_SECRET = map.get("gz_appSecret");
                } else {
                    RedisUtil.remove(HouConfig.PAY_CONFIG);
                    return false;
                }
            }
            WXPayConfig.SHOP_APP_ID = shopAppId;
            WXPayConfig.SHOP_APP_ID_SECRET = shopAppIdSecret;
            return true;
        } catch (Exception e) {
            LOG.error("=== feign 调用 payment 异常: {}", e.getMessage(), e);
        }
        return false;
    }

}
