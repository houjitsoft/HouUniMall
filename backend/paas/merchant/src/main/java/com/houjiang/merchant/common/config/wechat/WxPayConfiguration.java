package com.houjiang.merchant.common.config.wechat;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.houjiang.merchant.common.entity.setting.MallPay;
import com.houjiang.merchant.common.entity.Config;
import com.houjiang.merchant.common.enums.PaymentEnum;
import com.houjiang.merchant.common.mapper.setting.MallPayMapper;
import com.houjiang.merchant.common.mapper.ConfigMapper;
import com.houjiang.merchant.common.util.MapUtils;
import com.houjiang.merchant.common.util.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;
import java.util.Map;

@Configuration
@ConditionalOnClass(WxPayService.class)
@AllArgsConstructor
public class WxPayConfiguration {

    @Resource
    MallPayMapper mallPayMapper;

    @Resource
    ConfigMapper systemConfigMapper;

    /**
     * 微信小程序支付配置
     *
     * @author Amos
     * @return WxPayService
     */
    @Bean
    @ConditionalOnMissingBean
    public WxPayService mnpPayService() {
        MallPay config = mallPayMapper.selectOne(
                new QueryWrapper<MallPay>()
                    .eq("pay_type", PaymentEnum.WX_PAY.getCode())
                    .last("limit 1"));

        Config systemConfig = systemConfigMapper.selectOne(new QueryWrapper<Config>()
                .eq("type", "mnp_setting")
                .eq("name", "app_id")
                .last("limit 1"));

        String paramJson = (config == null || StringUtils.isNull(config.getConfig())) ? "{}" : config.getConfig().toString().replace("[]", "{}");
        Map<String, String> params = MapUtils.jsonToMap(paramJson);
        String appId = StringUtils.isNull(systemConfig) ? "" : systemConfig.getValue();
        String mchId = params.getOrDefault("wechat_mch_id", "");
        String paySignKey  = params.getOrDefault("wechat_pay_sign_key", "");
        byte[] privateKey  = params.getOrDefault("wechat_apiclient_cert", "").getBytes();
        byte[] privateCert = params.getOrDefault("wechat_apiclient_key", "").getBytes();

        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setAppId(appId);
        payConfig.setMchId(mchId);
        payConfig.setApiV3Key(paySignKey);
        payConfig.setPrivateKeyContent(privateKey);
        payConfig.setPrivateCertContent(privateCert);
        payConfig.setUseSandboxEnv(false);
        if (StringUtils.isNotEmpty(params.getOrDefault("wechat_public_serial", ""))) {
            payConfig.setPublicKeyId(params.getOrDefault("wechat_public_serial", ""));
        }
        if (StringUtils.isNotEmpty(params.getOrDefault("wechat_public_cert", ""))) {
            payConfig.setPublicKeyContent(params.getOrDefault("wechat_public_cert", "").getBytes());
        }
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);
        return wxPayService;
    }

    /**
     * 微信公众号支付配置
     *
     * @author Amos
     * @return WxPayService
     */
    @Bean
    @ConditionalOnMissingBean
    public WxPayService wxOaService() {
        MallPay config = mallPayMapper.selectOne(
                new QueryWrapper<MallPay>()
                        .eq("pay_type", PaymentEnum.WX_PAY.getCode())
                        .last("limit 1"));

        Config systemConfig = systemConfigMapper.selectOne(new QueryWrapper<Config>()
                .eq("name", "app_id")
                .eq("type", "oa_setting")
                .last("limit 1"));

        String paramJson = (config == null || StringUtils.isNull(config.getConfig())) ? "{}" : config.getConfig().toString();
        Map<String, String> params = MapUtils.jsonToMap(paramJson);
        String appId = StringUtils.isNull(systemConfig) ? "" : systemConfig.getValue();
        String mchId = params.getOrDefault("wechat_mch_id", "");
        String paySignKey  = params.getOrDefault("wechat_pay_sign_key", "");
        byte[] privateKey  = params.getOrDefault("wechat_apiclient_key", "").getBytes();
        byte[] privateCert = params.getOrDefault("wechat_apiclient_cert", "").getBytes();

        WxPayConfig payConfig = new WxPayConfig();
        payConfig.setMchId(mchId);
        payConfig.setAppId(appId);
        payConfig.setApiV3Key(paySignKey);
        payConfig.setPrivateKeyContent(privateKey);
        payConfig.setPrivateCertContent(privateCert);
        payConfig.setUseSandboxEnv(false);
        if (StringUtils.isNotEmpty(params.getOrDefault("wechat_public_serial", ""))) {
            payConfig.setPublicKeyId(params.getOrDefault("wechat_public_serial", ""));
        }
        if (StringUtils.isNotEmpty(params.getOrDefault("wechat_public_cert", ""))) {
            payConfig.setPublicKeyContent(params.getOrDefault("wechat_public_cert", "").getBytes());
        }
        WxPayService wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);
        return wxPayService;
    }

}
