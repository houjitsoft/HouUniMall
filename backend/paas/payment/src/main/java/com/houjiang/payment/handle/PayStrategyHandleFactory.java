package com.houjiang.payment.handle;

import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.service.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @PROJECT_NAME: UniMall
 * @DESCRIPTION: 策略工厂, 配置具有实现类
 * @USER: Amos
 * @DATE: 2023/1/14 23:17
 * @author Amos
 */

public class PayStrategyHandleFactory  {


    private static Map<String, PayStrategyHandleService> payinfoMap;

    public static void init() {
        payinfoMap = new HashMap<>();

        //余额支付
        payinfoMap.put("1", new BalancePayServiceImpl());
        //购物卡支付
        payinfoMap.put("2", new GiftCardPayServiceImpl());
        //阿里PC支付
        payinfoMap.put("3", new AliPayPCServiceImpl());
        //阿里H5支付
        payinfoMap.put("4", new AliPayH5ServiceImpl());
        //阿里APP支付
        payinfoMap.put("5", new AliPayAPPServiceImpl());
        //阿里plus支付
        payinfoMap.put("6", new AliPayPlusServiceImpl());
        //微信PC支付
        payinfoMap.put("7", new WXPayPCServicesImpl());
        //微信H5支付
        payinfoMap.put("8", new WXPayH5ServicesImpl());
        //微信APP支付
        payinfoMap.put("9", new WXPayAPPServicesImpl());
        //微信plus支付
        payinfoMap.put("10", new WXPayPlusServicesImpl());
        //微信小程序支付
        payinfoMap.put("11", new WXAppletPayServicesImpl());
        //纯积分支付
        payinfoMap.put("12", new IntegralPayServicesImpl());
        //质保金支付
        payinfoMap.put("13", new RetentionPayServicesImpl());
        //余额充值 - 线下支付
        payinfoMap.put("14", new BalanceRechargeServicesImpl());
        //微信余额充值- PC支付
        payinfoMap.put("15", new WXPayPCBalanceRechargeServicesImpl());
        //微信余额充值- H5支付
        payinfoMap.put("16", new WXPayH5BalanceRechargeServicesImpl());
        //微信余额充值- 小程序支付
        payinfoMap.put("17", new WXAppletBalanceRechargePayServicesImpl());
        //微信余额充值- app支付
        payinfoMap.put("18", new WXPayAPPBalanceRechargeServicesImpl());
        //阿里余额充值- PC支付
        payinfoMap.put("19", new AliPayPCBalanceRechargeServiceImpl());
        //阿里余额充值- H5支付
        payinfoMap.put("20", new AliPayH5BalanceRechargeServiceImpl());
        //阿里余额充值- app支付
        payinfoMap.put("21", new AliPayAPPBalanceRechargeServiceImpl());
        //微信 JSAPI 方式支付  在微信中点 h5 链接支付
        payinfoMap.put("22", new WXAppletPayByH5ServicesImpl());
        // 嘉币、劲豆支付
        payinfoMap.put("23", new PointsPayServicesImpl());

    }

    public static PayStrategyHandleService getPayStrategyHandle(Map<Object, Object> payInfo) {
        return payinfoMap.get(payInfo.get("type").toString());
    }


}
