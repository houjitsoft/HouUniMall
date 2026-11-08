package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.houjiang.commons.config.ALiPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.service.PayStrategyHandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝PC支付
 *
 * @author Amos
 * @date 2023/4/27 9:35
 */
@Service
public class AliPayPlusServiceImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static PayConfigMapper payConfigMapper;

    private static PayConsumeTradeBackMapper payConsumeTradeBackMapper;

    private static PayConsumeRecordMapper payConsumeRecordMapper;

    private static UsermsServiceClient usermsServiceClient;

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setMapper(PayConfigMapper payConfigMapper, PayConsumeTradeBackMapper payConsumeTradeBackMapper, PayConsumeRecordMapper payConsumeRecordMapper) {
        AliPayPlusServiceImpl.payConfigMapper = payConfigMapper;
        AliPayPlusServiceImpl.payConsumeRecordMapper = payConsumeRecordMapper;
        AliPayPlusServiceImpl.payConsumeTradeBackMapper = payConsumeTradeBackMapper;

    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setUsermsServiceClient(UsermsServiceClient usermsServiceClient) {

        AliPayPlusServiceImpl.usermsServiceClient = usermsServiceClient;
    }

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> para, HttpServletRequest request, HttpServletResponse response) {
        //加载支付配置
        this.initAliPayConfig();
        //创建支付单号
        int i = PayToolUtil.buildRandom(6);
        // 创建一个SimpleDateFormat对象，用于将当前日期格式化为yyyyMMdd的字符串格式
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

        // 初始化一个StringBuffer对象，用于构建PLUS订单标识
        StringBuffer plusorder = new StringBuffer("PLUS");

        // 将格式化的当前日期和订单编号追加到plusorder中，形成唯一的订单标识
        plusorder.append(format.format(new Date()));
        plusorder.append(i);

        // 通过token获取用户信息，用于后续订单关联到具体用户
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(para.get("token").toString());

        // 将支付价格转换为BigDecimal类型，确保精确计算
        BigDecimal totalPrice = new BigDecimal(String.valueOf(para.get("payPrice")));

        // 初始化订单名称变量
        String ordername = null;

        // 根据订单类型（thaliType）确定订单名称，区分月卡、季卡和年卡订单
        if (para.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_MOON.getCode())){
            ordername = CommonType.plusThaliType.PLUS_THALI_MOON.getDesc();
        }else if(para.get("thaliType").equals(CommonType.plusThaliType.PLUS_THALI_SEASON.getCode())){
            ordername = CommonType.plusThaliType.PLUS_THALI_SEASON.getDesc();
        }else{
            ordername = CommonType.plusThaliType.PLUS_THALI_YEAR.getDesc();
        }

        String price = String.valueOf(totalPrice);
        try {
            // 创建支付宝客户端
            AlipayClient alipayClient = this.getAlipayClient();

            // 设置请求参数
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

            // 商户订单号，商户网站订单系统中唯一订单号，必填
            String outTradeNo = plusorder.toString();
            // 付款金额，必填
            String totalAmount = price;
            // 订单名称，必填
            String subject = ordername;
            // 商品描述，可空
            String body = "购物";
            // 封装请求支付信息
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            para.put("out_trade_no",outTradeNo);
            model.setPassbackParams(JSON.toJSONString(para));
            model.setOutTradeNo(outTradeNo);
            model.setTotalAmount(totalAmount);
            model.setSubject(subject);
            model.setBody(body);
            model.setTimeoutExpress("30m");
            // 销售产品码 必填
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            alipayRequest.setBizModel(model);
            alipayRequest.setReturnUrl(ALiPayConfig.shopping_return_url_pc);
            alipayRequest.setNotifyUrl(ALiPayConfig.PLUS_NOTIFY_URL);

            // 调用SDK生成表单
            String result = alipayClient.pageExecute(alipayRequest).getBody();
            response.setContentType("text/html;charset=" + ALiPayConfig.CHARSET);

            HashMap<String, Object> payMap = new HashMap<>();
            payMap.put("plusOrderid", outTradeNo);
            payMap.put("userId", ui.getUserId());
            payMap.put("payPrice", totalPrice);
            payMap.put("payType", CommonType.plusPayType.PLUS_ALI_PAY.getCode());
            payMap.put("thaliType", para.get("thaliType"));
            payMap.put("plusEquity", para.get("thaliEquity"));
            //插入plus预购买单
            usermsServiceClient.aLiWxCreatePlusOrder(payMap);
            return ResponseResult.success(result);
        } catch (Exception e) {
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }

}
