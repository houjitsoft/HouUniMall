package com.houjiang.payment.service.cashier;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.payment.entity.PayCashierOrderEntity;
import com.houjiang.payment.entity.PayChannelEntity;
import com.houjiang.payment.entity.PayMerchantQualEntity;
import com.houjiang.payment.mapper.PayChannelMapper;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.service.PayMerchantQualService;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信 JSAPI 支付下单 Handler（收银台 Task 1）
 *
 * <p>职责：
 * <ul>
 *   <li>初始化微信支付配置（DB → WXPayConfig 静态字段）</li>
 *   <li>调用微信 v2 统一下单接口获取 prepay_id</li>
 *   <li>组装前端 JSAPI 调起参数 packageParams(appId/timeStamp/nonceStr/package/signType/paySign)</li>
 *   <li>提供验签 + 查单能力给回调 Controller 复用</li>
 * </ul>
 *
 * <p>与老 {@code WXAppletPayServicesImpl} 的区别：
 * 不耦合积分/老交易表，只围绕 cashier_order 做事，职责单一，便于收银台复用。
 *
 * @author Amos
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WxJsapiPayHandler {

    private final PayConfigMapper payConfigMapper;
    private final PayMerchantQualService merchantQualService;
    private final PayChannelMapper payChannelMapper;

    /** 微信小程序支付配置 paymentMode */
    private static final String PAY_MODE_APPLET = "02";
    /** 公众号支付配置 paymentMode */
    private static final String PAY_MODE_GZ = "04";

    /**
     * 统一回调入口（在 application 可配置），默认指向 payment 服务自身
     */
    @Value("${cashier.wx.jsapi.notify-url:}")
    private String configuredNotifyUrl;

    /**
     * 优先从 cloud 后台 hou_pay_channel 表（configJson）读取渠道配置，
     * 未命中则回退到 hou_pay_merchant_qual / hou_pay_config。
     *
     * @param channelCode 渠道编码（例如 WECHAT_JSAPI）
     * @param terminal    订单终端
     */
    @SuppressWarnings("unchecked")
    public void initConfigForChannel(String channelCode, String terminal) {
        if (StringUtils.hasText(channelCode)) {
            try {
                LambdaQueryWrapper<PayChannelEntity> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PayChannelEntity::getChannelCode, channelCode)
                        .eq(PayChannelEntity::getStatus, 1)
                        .last("limit 1");
                PayChannelEntity channel = payChannelMapper.selectOne(wrapper);
                if (channel != null && StringUtils.hasText(channel.getConfigJson())) {
                    java.util.Map<String, Object> cfg = JSON.parseObject(channel.getConfigJson(), java.util.Map.class);
                    String appId = str(cfg.get("appId"));
                    String appSecret = str(cfg.get("appSecret"));
                    String mchId = str(cfg.get("mchId"));
                    String apiKey = str(cfg.get("apiKey"));
                    String notifyUrl = str(cfg.get("notifyUrl"));
                    if (StringUtils.hasText(appId)) {
                        if ("WX_MINI".equalsIgnoreCase(terminal)) {
                            WXPayConfig.Applet_APP_ID = appId;
                        } else {
                            WXPayConfig.GZ_APP_ID = appId;
                        }
                    }
                    if (StringUtils.hasText(appSecret)) {
                        if ("WX_MINI".equalsIgnoreCase(terminal)) {
                            WXPayConfig.Applet_APP_SECRET = appSecret;
                        } else {
                            WXPayConfig.GZ_APP_SECRET = appSecret;
                        }
                    }
                    if (StringUtils.hasText(mchId)) {
                        WXPayConfig.MCH_ID = mchId;
                    }
                    if (StringUtils.hasText(apiKey)) {
                        WXPayConfig.API_KEY = apiKey;
                    }
                    if (StringUtils.hasText(notifyUrl)) {
                        WXPayConfig.NOTIFY_URL = notifyUrl;
                    }
                    log.info("[wxJsapi] init config from hou_pay_channel, channelCode={}, terminal={}, appId={}, mchId={}",
                            channelCode, terminal, appId, mchId);
                    return;
                }
                log.info("[wxJsapi] hou_pay_channel miss for channelCode={}, fallback to merchantQual", channelCode);
            } catch (Exception e) {
                log.warn("[wxJsapi] load hou_pay_channel failed, fallback. err={}", e.getMessage());
            }
        }
        initConfigForTerminal(terminal);
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }

    /**
     * 优先从新表（hou_pay_merchant_qual）加载资质；失败则回退到老表 hou_pay_config.
     * @param terminal 订单终端（如 WX_MINI / WAP / APP）
     */
    public void initConfigForTerminal(String terminal) {
        if (StringUtils.hasText(terminal)) {
            try {
                PayMerchantQualEntity qual = merchantQualService.getByTerminalChannel(terminal, "WECHAT", "P");
                if (qual != null) {
                    if (StringUtils.hasText(qual.getAppId())) {
                        if ("WX_MINI".equalsIgnoreCase(terminal)) {
                            WXPayConfig.Applet_APP_ID = qual.getAppId();
                        } else {
                            WXPayConfig.GZ_APP_ID = qual.getAppId();
                        }
                    }
                    if (StringUtils.hasText(qual.getAppSecret())) {
                        if ("WX_MINI".equalsIgnoreCase(terminal)) {
                            WXPayConfig.Applet_APP_SECRET = qual.getAppSecret();
                        } else {
                            WXPayConfig.GZ_APP_SECRET = qual.getAppSecret();
                        }
                    }
                    if (StringUtils.hasText(qual.getMchId())) {
                        WXPayConfig.MCH_ID = qual.getMchId();
                    }
                    if (StringUtils.hasText(qual.getApiKey())) {
                        WXPayConfig.API_KEY = qual.getApiKey();
                    }
                    if (StringUtils.hasText(qual.getNotifyUrl())) {
                        WXPayConfig.NOTIFY_URL = qual.getNotifyUrl();
                    }
                    log.info("[wxJsapi] init config from merchantQual table, terminal={}, appId={}, mchId={}",
                            terminal, qual.getAppId(), qual.getMchId());
                    return;
                }
                log.info("[wxJsapi] merchantQual table miss for terminal={}, fallback to legacy hou_pay_config", terminal);
            } catch (Exception e) {
                log.warn("[wxJsapi] load merchantQual failed, fallback to legacy. err={}", e.getMessage());
            }
        }
        // 回退老表
        initConfig();
    }

    /**
     * 初始化微信支付配置（从 pay_config 表读取 appId/appSecret）
     * 小程序用 02，公众号用 04；其余保持 WXPayConfig 默认值。
     * 失败时仅打 warn 日志，不抛异常（DB 配置缺失时允许回退到默认静态值）。
     */
    public void initConfig() {
        try {
            Map<String, Object> applet = payConfigMapper.selectPayConfig(PAY_MODE_APPLET);
            if (applet != null) {
                Object appId = applet.get("appID");
                Object appSecret = applet.get("appSecret");
                if (appId != null) {
                    WXPayConfig.Applet_APP_ID = appId.toString();
                }
                if (appSecret != null) {
                    WXPayConfig.Applet_APP_SECRET = appSecret.toString();
                }
            }
            Map<String, Object> gz = payConfigMapper.selectPayConfig(PAY_MODE_GZ);
            if (gz != null) {
                Object appId = gz.get("appID");
                Object appSecret = gz.get("appSecret");
                if (appId != null) {
                    WXPayConfig.GZ_APP_ID = appId.toString();
                }
                if (appSecret != null) {
                    WXPayConfig.GZ_APP_SECRET = appSecret.toString();
                }
            }
        } catch (Exception e) {
            log.warn("[wxJsapi] init config from DB failed, fallback to default. err={}", e.getMessage());
        }
    }

    /**
     * 小程序 JSAPI 下单：调用微信统一下单返回 prepay_id，并组装前端调起参数
     *
     * @param order  收银台订单
     * @param openId 用户 openId（必须）
     * @param clientIp 客户端 IP
     * @return 前端 wx.requestPayment 所需的 packageParams
     */
    public Map<String, String> unifiedOrder(PayCashierOrderEntity order, String openId, String clientIp) {
        return unifiedOrder(order, openId, clientIp, null);
    }

    /**
     * 带 channelCode 的重载：channelCode 优先从 cloud 后台 hou_pay_channel 表读取签名材料，
     * 其余逻辑与原方法完全一致，保证向后兼容。
     */
    public Map<String, String> unifiedOrder(PayCashierOrderEntity order, String openId, String clientIp, String channelCode) {
        if (order == null) {
            throw new IllegalArgumentException("收银台订单不能为空");
        }
        if (!StringUtils.hasText(openId)) {
            throw new IllegalArgumentException("微信 JSAPI 支付需要 openId");
        }
        // 优先 channelCode -> hou_pay_channel；未命中回退 hou_pay_merchant_qual；再失败回退 hou_pay_config
        if (StringUtils.hasText(channelCode)) {
            initConfigForChannel(channelCode, order.getTerminal());
        } else {
            initConfigForTerminal(order.getTerminal());
        }

        // 区分小程序 vs 公众号：WX_MINI 用 Applet_APP_ID，WAP/公众号用 GZ_APP_ID
        String appId;
        if ("WX_MINI".equalsIgnoreCase(order.getTerminal())) {
            appId = WXPayConfig.Applet_APP_ID;
        } else {
            appId = StringUtils.hasText(WXPayConfig.GZ_APP_ID) ? WXPayConfig.GZ_APP_ID : WXPayConfig.Applet_APP_ID;
        }

        String mchId = WXPayConfig.MCH_ID;
        String apiKey = WXPayConfig.API_KEY;
        String notifyUrl = resolveNotifyUrl(order);

        // 金额：元 → 分，向上取整（精度安全）
        int totalFee = order.getAmount().multiply(new BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP).intValueExact();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("appid", appId);
        params.put("mch_id", mchId);
        params.put("nonce_str", UUID.randomUUID().toString().replace("-", ""));
        params.put("body", StringUtils.hasText(order.getSubject()) ? order.getSubject() : ("收银台-" + order.getCashierNo()));
        params.put("out_trade_no", order.getCashierNo());
        params.put("total_fee", String.valueOf(totalFee));
        params.put("spbill_create_ip", StringUtils.hasText(clientIp) ? clientIp : "127.0.0.1");
        params.put("notify_url", notifyUrl);
        params.put("trade_type", "JSAPI");
        params.put("openid", openId);
        params.put("sign", PaymentKit.createSign(params, apiKey));

        log.info("[wxJsapi] unifiedOrder req, cashierNo={}, appId={}, mchId={}, totalFee={}, notifyUrl={}",
                order.getCashierNo(), appId, mchId, totalFee, notifyUrl);

        String xmlResult = PaymentApi.pushOrder(params);
        Map<String, String> result = PaymentKit.xmlToMap(xmlResult);
        log.info("[wxJsapi] unifiedOrder resp, cashierNo={}, resp={}", order.getCashierNo(), result);

        String returnCode = result.get("return_code");
        if (StrKit.isBlank(returnCode) || !"SUCCESS".equals(returnCode)) {
            throw new IllegalStateException("微信下单失败：" + result.get("return_msg"));
        }
        String resultCode = result.get("result_code");
        if (StrKit.isBlank(resultCode) || !"SUCCESS".equals(resultCode)) {
            throw new IllegalStateException("微信下单业务失败：" + result.get("err_code_des"));
        }
        String prepayId = result.get("prepay_id");
        if (StrKit.isBlank(prepayId)) {
            throw new IllegalStateException("微信下单未返回 prepay_id");
        }

        // 组装前端调起参数
        Map<String, String> pkg = new LinkedHashMap<>();
        pkg.put("appId", appId);
        pkg.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));
        pkg.put("nonceStr", UUID.randomUUID().toString().replace("-", ""));
        pkg.put("package", "prepay_id=" + prepayId);
        pkg.put("signType", "MD5");
        pkg.put("paySign", PaymentKit.createSign(pkg, apiKey));
        // 补充订单定位信息，方便前端展示
        pkg.put("prepayId", prepayId);
        pkg.put("cashierNo", order.getCashierNo());
        return pkg;
    }

    /**
     * 校验微信回调签名
     * @param notifyMap 回调 XML 解析后的 Map（已含 sign 字段）
     * @return true=验签通过
     */
    public boolean verifyNotify(Map<String, String> notifyMap) {
        if (notifyMap == null || notifyMap.isEmpty()) {
            return false;
        }
        initConfig();
        return PaymentKit.verifyNotify(notifyMap, WXPayConfig.API_KEY);
    }

    /**
     * 构造微信异步通知应答 XML
     */
    public String buildNotifyResponse(boolean success, String msg) {
        return "<xml>" +
                "<return_code><![CDATA[" + (success ? "SUCCESS" : "FAIL") + "]]></return_code>" +
                "<return_msg><![CDATA[" + (StringUtils.hasText(msg) ? msg : "OK") + "]]></return_msg>" +
                "</xml>";
    }

    /**
     * 解析 notify_url：优先订单 notifyUrl（本地兜底），其次配置项，最后回退 WXPayConfig 默认
     */
    private String resolveNotifyUrl(PayCashierOrderEntity order) {
        // 回调地址必须是 payment 服务自己的 /api/payment/callback/wx/jsapi
        if (StringUtils.hasText(configuredNotifyUrl)) {
            return configuredNotifyUrl;
        }
        return StringUtils.hasText(WXPayConfig.NOTIFY_URL) ? WXPayConfig.NOTIFY_URL
                : "http://localhost:8207/api/payment/callback/wx/jsapi";
    }

    /**
     * 转 Map 为 XML（便于日志落库）
     */
    public static Map<String, String> parseXml(String xml) {
        if (!StringUtils.hasText(xml)) {
            return new HashMap<>();
        }
        return PaymentKit.xmlToMap(xml);
    }
}
