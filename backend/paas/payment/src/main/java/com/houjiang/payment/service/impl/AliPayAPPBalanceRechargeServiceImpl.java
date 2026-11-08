package com.houjiang.payment.service.impl;

import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.houjiang.commons.config.ALiPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.MathUtil;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.vo.CallBackVO;
import com.houjiang.payment.vo.RecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 余额充值 - 支付宝app支付
 *
 * @author Amos
 * @date 2023/4/27 9:35
 */
@Service
public class AliPayAPPBalanceRechargeServiceImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static OrderServiceClient orderServiceClient;

    @Autowired
    public void setOrderServiceClient (OrderServiceClient orderServiceClient){
        AliPayAPPBalanceRechargeServiceImpl.orderServiceClient = orderServiceClient;
    };

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> para, HttpServletRequest request, HttpServletResponse response) {
        //获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(para.get("token").toString());
        if (null != userInfo) {
            //加载支付配置
            this.initAliPayConfig();
            //订单号
            String outTradeNo = (String) para.get("rechargeOrderId");
            // 判断订单是否支付过
            ResponseResult responseResult = basePayServiceImpl.isPayBalanceRechargeOrder(outTradeNo);
            if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                return responseResult;
            }
            OrderBalanceRechargeVO orderBalanceRechargeVO = orderServiceClient.queryRechargeOrderById(outTradeNo);
            //商品名称
            String goodsName = "订单编号" + outTradeNo;
            String payAmount = String.valueOf(orderBalanceRechargeVO.getRechargeAmount());
            try {
                // 创建支付宝客户端
                AlipayClient alipayClient = this.getAlipayClient();
                // 设置请求参数
                AlipayTradeAppPayRequest alipayRequest = new AlipayTradeAppPayRequest ();
                // 商户订单号，商户网站订单系统中唯一订单号，必填
                String out_trade_no = outTradeNo;
                // 付款金额，必填
                String total_amount = payAmount;
                // 订单名称，必填
                String subject = goodsName;
                // 商品描述，可空
                String body = "余额充值";
                // 封装请求支付信息
                AlipayTradeAppPayModel model = new AlipayTradeAppPayModel ();
//                model.setPassbackParams(JsonUtils.toString(callBackParaList));
                model.setOutTradeNo(out_trade_no);
                model.setTotalAmount(total_amount);
                model.setSubject(subject);
                model.setBody(body);
                model.setTimeoutExpress("30m");
                // 销售产品码 必填
                model.setProductCode("QUICK_MSECURITY_PAY");
                alipayRequest.setBizModel(model);
                alipayRequest.setReturnUrl(ALiPayConfig.BALANCE_RECHARGE_NOTIFY_URL_H5);
                alipayRequest.setNotifyUrl(ALiPayConfig.BALANCE_RECHARGE_NOTIFY_URL);
                // 调用SDK生成表单
                String result = alipayClient.sdkExecute(alipayRequest).getBody();
                response.setContentType("text/html;charset=" + ALiPayConfig.CHARSET);

                return ResponseResult.success(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL.getCode(), BizCodeEnume.TOKEN_IS_ILLEGAL.getMsg());
    }
}
