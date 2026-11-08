package com.houjiang.merchant.common.plugin.alipay;

import com.alipay.api.*;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.merchant.common.config.AlipayConfig;
import com.houjiang.merchant.common.entity.setting.MallPay;
import com.houjiang.merchant.common.mapper.setting.MallPayMapper;
import com.houjiang.merchant.common.plugin.alipay.request.AliPayRefundRequest;
import com.houjiang.merchant.common.plugin.alipay.request.AliPaymentRequest;
import com.houjiang.merchant.common.util.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Amos
 */
@Component
public class AliPayDriver {

    private static MallPayMapper devPayConfigMapper;

    /**
     * 注入支付配置依赖
     */
    @Resource
    public void setDevPayConfigMapper(MallPayMapper devPayConfigMapper) {
        AliPayDriver.devPayConfigMapper = devPayConfigMapper;
    }

    public static Object unifiedOrder(AliPaymentRequest params) throws AlipayApiException, UnsupportedEncodingException {

        // 订单参数
        String outTradeNo      = params.getOutTradeNo();
        BigDecimal orderAmount = params.getOrderAmount();
        String description     = params.getDescription();

        // 失效时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Long expireTime = System.currentTimeMillis() + (30 * 60 * 1000);
        String timeExpire = format.format(expireTime) + "+08:00";

        AlipayClient alipayClient = createCertAlipayClient();
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(outTradeNo);
        model.setTotalAmount(String.valueOf(orderAmount));
        model.setSubject(description);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setPassbackParams(URLEncoder.encode(params.getAttach(), "UTF-8"));
        // 商户ID为空
        //model.setSellerId("");
        model.setTimeExpire(timeExpire);
        request.setBizModel(model);
        request.setNotifyUrl(RequestUtils.uri() + "/api/pay/notifyAliPay");
        request.setReturnUrl(params.getUrl());
        return alipayClient.pageExecute(request).getBody();
    }

    public static AlipayTradeRefundResponse refund(AliPayRefundRequest req) throws AlipayApiException {
        AlipayClient alipayClient = createCertAlipayClient();
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(req.getOutTradeNo());
        model.setTradeNo(req.getTransactionId());
        model.setOutRequestNo(req.getOutRefundNo());
        model.setRefundAmount(String.valueOf(req.getRefundAmount()));
        request.setBizModel(model);
        return alipayClient.execute(request);
    }


    public static boolean checkSign(Map<String, String> param) throws AlipayApiException {
        // 使用证书验签
        return checkSignWithCert(param);
    }

    /**
     * 创建证书模式的支付宝客户端
     */
    private static AlipayClient createCertAlipayClient() throws AlipayApiException {
        MallPay config = devPayConfigMapper.selectOne(
                new QueryWrapper<MallPay>()
                        .eq("pay_type", 3)
                        .last("limit 1"));

        Map<String, String> params = MapUtils.jsonToMap(config.getConfig().toString());
        String appId = params.get("alipay_app_id");
        String privateKey = params.getOrDefault("alipay_private_key", "");
        String appCert = params.get("alipay_app_cert");
        String alipayPublicCert = params.get("alipay_public_cert");
        String alipayRootCert = params.get("alipay_root_cert");
        String gateWay = StringUtils.isNotNull(YmlUtils.get("merchant.alidebug")) && YmlUtils.get("merchant.alidebug").equals("true") ? com.houjiang.merchant.common.config.AlipayConfig.GATEWAY_URL_DEBUG : AlipayConfig.GATEWAY_URL;

        try {
            // 创建临时证书文件
            File appCertFile = createTempCertFile(appCert, "app_cert");
            File alipayCertFile = createTempCertFile(alipayPublicCert, "alipay_cert");
            File rootCertFile = createTempCertFile(alipayRootCert, "root_cert");

            // 创建证书请求对象
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(gateWay);
            certAlipayRequest.setAppId(appId);
            certAlipayRequest.setPrivateKey(privateKey);
            certAlipayRequest.setFormat("json");
            certAlipayRequest.setCharset("UTF-8");
            certAlipayRequest.setSignType("RSA2");
            certAlipayRequest.setCertPath(appCertFile.getAbsolutePath());
            certAlipayRequest.setAlipayPublicCertPath(alipayCertFile.getAbsolutePath());
            certAlipayRequest.setRootCertPath(rootCertFile.getAbsolutePath());

            return new DefaultAlipayClient(certAlipayRequest);
        } catch (IOException e) {
            throw new AlipayApiException("创建证书文件失败: " + e.getMessage());
        }
    }

    /**
     * 创建临时证书文件
     */
    private static File createTempCertFile(String certContent, String prefix) throws IOException {
        File tempFile = File.createTempFile(prefix, ".crt");
        // JVM退出时自动删除
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(certContent);
        }

        return tempFile;
    }

    /**
     * 使用证书验签
     */
    private static boolean checkSignWithCert(Map<String, String> param) throws AlipayApiException {
        MallPay config = devPayConfigMapper.selectOne(
                new QueryWrapper<MallPay>()
                        .eq("pay_type", 3)
                        .last("limit 1"));

        Map<String, String> params = MapUtils.jsonToMap(config.getConfig().toString());
        String alipayPublicCert = params.get("alipay_public_cert");

        try {
            // 创建临时证书文件用于验签
            File alipayCertFile = createTempCertFile(alipayPublicCert, "verify_cert");

            return AlipaySignature.rsaCertCheckV1(
                    param,
                    alipayCertFile.getAbsolutePath(),
                    "UTF-8"
            );
        } catch (IOException e) {
            throw new AlipayApiException("证书验签失败: " + e.getMessage());
        }
    }

    public static Map<String, String> getNotifyParam(Map<String, String[]> requestParams){
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<>();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        return params;
    }
}
