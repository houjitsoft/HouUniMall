package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.vo.RecordVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXPayPCServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static PayConfigMapper payConfigMapper;

    private static PayConsumeTradeMapper payConsumeTradeMapper;

    private static PayConsumeTradeBackMapper payConsumeTradeBackMapper;

    private static PayConsumeRecordMapper payConsumeRecordMapper;

    private static UsermsServiceClient usermsServiceClient;

    private static OrderServiceClient orderServiceClient;

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setUsermsServiceClient(UsermsServiceClient usermsServiceClient) {

        WXPayPCServicesImpl.usermsServiceClient = usermsServiceClient;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setMapper(PayConfigMapper payConfigMapper, PayConsumeTradeMapper payConsumeTradeMapper,
                          PayConsumeTradeBackMapper payConsumeTradeBackMapper, PayConsumeRecordMapper payConsumeRecordMapper) {
        WXPayPCServicesImpl.payConfigMapper = payConfigMapper;
        WXPayPCServicesImpl.payConsumeTradeMapper = payConsumeTradeMapper;
        WXPayPCServicesImpl.payConsumeTradeBackMapper = payConsumeTradeBackMapper;
        WXPayPCServicesImpl.payConsumeRecordMapper = payConsumeRecordMapper;
    }

    @Autowired
    public void setOrderServiceClient(OrderServiceClient orderServiceClient) {
        WXPayPCServicesImpl.orderServiceClient = orderServiceClient;
    }

    final static Logger log = LoggerFactory.getLogger(WXPayPCServicesImpl.class);

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) {
        // 初始化微信支付pc、h5配置
        this.initWeChatPayConfig();
        String outTradeNo = (String) map.get("orderId");
        String token = (String) map.get("token");
        BigDecimal totalPrice = new BigDecimal(0);
        // 判断订单是否支付过
        ResponseResult responseResult = basePayServiceImpl.isPayOrder(outTradeNo);
        if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            return responseResult;
        }
        // 来源
        Integer orderFromNum = map.get("platForm") == null ? 0 : Integer.parseInt(map.get("platForm").toString());
        // 订单来源存值
        RedisUtil.set(HouConfig.HOU_ORDERFORM + outTradeNo, orderFromNum.toString(), 86400);

        // 处理自营店铺
        if (!StringUtils.isEmpty(RedisUtil.get(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo))) {
            RedisUtil.remove(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo);
        }
        RedisUtil.set(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo, token, 86400);

        String goodsName = "订单编号" + outTradeNo;

        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        String userId = ui.getUserId();

        // 查询商品名与商品价格
        if (PayToolUtil.isInteger(outTradeNo)) {
            GoodsDTO queryGoodsPara = payConsumeTradeMapper.queryGoodsPriceAndName(outTradeNo);
            // 总价
            totalPrice = queryGoodsPara.getTradePaymentAmount();

            GoodsDTO queryGoodsPara2 = payConsumeTradeBackMapper.queryGoodsPriceAndName(outTradeNo, userId);
            //获取积分商品订单的积分
            Integer totalIntegral = queryGoodsPara2.getTotalIntegral();
            //积分商品 0  是  1 否
            integralGoods = queryGoodsPara2.getIntegralGoods();
            amountAotalIntegral = totalIntegral;
        } else {
            List<RecordVO> rp = payConsumeRecordMapper.queryRecordParaList(outTradeNo);
            for (RecordVO recordVO : rp) {
                BigDecimal price = recordVO.getRecordMoney();
                totalPrice = totalPrice.add(price);
            }
            // 获取积分商品的积分
            String totalIntegral = RedisUtil
                    .get(HouConfig.HOU_USERINTEGRALYPAID + "--" + token + "--" + userId);
            if (null != totalIntegral) {
                //amountAotalIntegral 不为null 证明是积分订单
                amountAotalIntegral = Integer.valueOf(totalIntegral);
                //积分商品 0  是  1 否
                integralGoods = 0;
            }
        }
        if (null != integralGoods && integralGoods == 0) {
            //获取用户积分
            Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userId);
            Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userId);
            userIntegral = userIntegral - frozenIntegra;
            if(userIntegral < 0){
                userIntegral = 0;
            }
            if (null != userIntegral && amountAotalIntegral > userIntegral) {
                //账户积分不足
                return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR);
            }
        }
        // 判断是否京东订单 且 是否当前支付是京东余额支付 且 余额是否够支付
        ResponseResult responseResult1 = orderServiceClient.queryJDBalanceIsEnough(outTradeNo, totalPrice);
        if(responseResult1.getCode() ==  CommonStatusEnum.SUCCESS.getCode() && responseResult1.getData().toString().equals("false")){
            // 京东账户余额不足！
            return ResponseResult.fail("京东账户余额不足！");
        }
        // 将订单总金额转换为字符串格式，用于后续支付接口调用
        String payAmount = String.valueOf(totalPrice);
        // 根据订单金额获取格式化后的支付金额
        String price = PayToolUtil.getMoney(payAmount);

        // 初始化参数映射表，用于构建微信支付请求
        Map<String, String> params = new HashMap<String, String>();
        // 设置微信支付应用ID
        params.put("appid", WXPayConfig.APP_ID);
        // 设置微信支付商户号
        params.put("mch_id", WXPayConfig.MCH_ID);
        // 生成随机字符串，作为请求的一部分
        params.put("nonce_str", PayToolUtil.create_nonce_str());
        // 设置商品描述
        params.put("body", goodsName);
        // 设置订单号
        params.put("out_trade_no", outTradeNo);
        // 设置订单总金额
        params.put("total_fee", price);
        // 设置订单支付的IP地址
        params.put("spbill_create_ip", PayToolUtil.getIpAddr(request));
        // 设置通知URL，用于接收微信支付异步通知
        params.put("notify_url", WXPayConfig.NOTIFY_URL);
        // 设置交易类型为Native，用于生成二维码支付
        params.put("trade_type", "NATIVE");

        // 生成签名，确保请求的安全性
        String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
        // 将签名添加到参数映射表中
        params.put("sign", sign);
        // 日志记录请求参数
        log.info("param{}", JSON.toJSONString(params));

        // 调用微信支付API，发起支付请求
        String xmlResult = PaymentApi.pushOrder(params);
        // 将返回的XML数据转换为映射表格式
        Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

        // 获取返回的代码和消息，用于判断支付请求的结果
        String return_code = result.get("return_code");
        String return_msg = result.get("return_msg");
        String err_code_des = result.get("err_code_des");
        // 如果返回代码不为SUCCESS，则处理异常情况
        if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
            // 如果是来自移动端的支付请求，返回H5支付链接
            if(orderFromNum!=0){
                String urlString = Url.getURLEncoderString(WXPayConfig.REDIRECT_URL);
                String mweb_url = result.get("mweb_url") + "&redirect_url=" + urlString;
                return ResponseResult.fail(err_code_des,mweb_url);
            }else{
                // 如果是来自PC端的支付请求，直接返回错误消息
                return ResponseResult.fail(return_msg);
            }
        }
        // 如果支付请求成功，获取二维码链接，返回给前端
        //PC端
        String qrCodeUrl = result.get("code_url");
        return ResponseResult.success(qrCodeUrl);

    }
}
