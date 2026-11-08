package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.houjiang.commons.config.ALiPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.*;
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
 * 支付宝PC支付
 *
 * @author Amos
 * @date 2023/4/27 9:35
 */
@Service
public class AliPayAPPServiceImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    /**
     * PayConfigMapper的静态实例。这个接口的作用是映射支付配置相关的数据库操作，
     * 通过这个实例，可以方便地执行支付配置的查询和更新等操作。
     */
    private static PayConfigMapper payConfigMapper;

    /**
     * PayConsumeTradeBackMapper的静态实例。这个接口的作用是映射支付消费回调相关的数据库操作，
     * 通过这个实例，可以方便地执行支付消费回调的查询和更新等操作。
     */
    private static PayConsumeTradeBackMapper payConsumeTradeBackMapper;

    /**
     * PayConsumeRecordMapper的静态实例。这个接口的作用是映射支付消费记录相关的数据库操作，
     * 通过这个实例，可以方便地执行支付消费记录的查询和更新等操作。
     */
    private static PayConsumeRecordMapper payConsumeRecordMapper;

    /**
     * 用户服务客户端的静态实例。这个实例用于与用户管理服务进行通信。
     * 使用静态初始化块在程序启动时初始化客户端。
     */
    private static UsermsServiceClient usermsServiceClient;

    private static OrderServiceClient orderServiceClient;


    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setMapper(PayConfigMapper payConfigMapper, PayConsumeTradeBackMapper payConsumeTradeBackMapper, PayConsumeRecordMapper payConsumeRecordMapper) {
        AliPayAPPServiceImpl.payConfigMapper = payConfigMapper;
        AliPayAPPServiceImpl.payConsumeRecordMapper = payConsumeRecordMapper;
        AliPayAPPServiceImpl.payConsumeTradeBackMapper = payConsumeTradeBackMapper;

    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setUsermsServiceClient(UsermsServiceClient usermsServiceClient) {

        AliPayAPPServiceImpl.usermsServiceClient = usermsServiceClient;
    }
    @Autowired
    public void setOrderServiceClient(OrderServiceClient orderServiceClient) {
        AliPayAPPServiceImpl.orderServiceClient = orderServiceClient;
    }
    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> para, HttpServletRequest request, HttpServletResponse response) {
        //获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(para.get("token").toString());
        if (null != userInfo) {
            //加载支付配置
            this.initAliPayConfig();
            //订单号
            String orderId = (String) para.get("orderId");
            // 判断订单是否支付过
            ResponseResult responseResult = basePayServiceImpl.isPayOrder(orderId);
            if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                return responseResult;
            }
            //用户id
            String userId = userInfo.getUserId();
            //订单支付金额
            BigDecimal totalPrice = new BigDecimal(0);
            //商品名称
            String goodsName = null;
            //积分订单支付金额
            Integer amountAotalIntegral = null;
            //积分商品 0  是  1 否
            Integer integralGoods = CommonType.goodsType.GOODS_TYPE_ORDINARY.getCode();
            //判断当前订单号是否是 支付单号 或 是订单号
            if (MathUtil.isInteger(orderId)) {
                GoodsDTO queryGoodsPara = payConsumeTradeBackMapper.queryGoodsPriceAndName(orderId, userId);
                totalPrice = queryGoodsPara.getTradePaymentAmount();
                goodsName = queryGoodsPara.getTradeDesc();
                //获取积分商品订单的积分
                Integer totalIntegral = queryGoodsPara.getTotalIntegral();
                //积分商品 0  是  1 否
                integralGoods = queryGoodsPara.getIntegralGoods();
                amountAotalIntegral = totalIntegral;
            } else {
                RecordVO rp = payConsumeRecordMapper.queryRecordPara(orderId);
                totalPrice = rp.getRecordMoney();
                goodsName = rp.getRecordDesc();

                // 获取积分商品的积分
                String totalIntegral = RedisUtil
                        .get(HouConfig.HOU_USERINTEGRALYPAID + "--" + para.get("token") + "--" + userId);
                if (null != totalIntegral) {
                    //amountAotalIntegral 不为null 证明是积分订单
                    amountAotalIntegral = Integer.valueOf(totalIntegral);
                    //积分商品 0  是  1 否
                    integralGoods = CommonType.goodsType.GOODS_TYPE_INTEGRAL.getCode();
                }
            }
            //如果是积分商品判断 当前用户积分是否支持购买
            if (null != integralGoods && integralGoods == 0) {
                //获取用户积分
                Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userId);
                Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userId);
                userIntegral = userIntegral - frozenIntegra;
                if(userIntegral < 0){
                    userIntegral = 0;
                }
                if (null != userIntegral && amountAotalIntegral > userIntegral) {
                    return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR.getCode(), BizCodeEnume.PAY_INTEGRAL_ERROR.getMsg());
                }
            }
            // 判断是否京东订单 且 是否当前支付是京东余额支付 且 余额是否够支付
            ResponseResult responseResult1 = orderServiceClient.queryJDBalanceIsEnough(orderId, totalPrice);
            if(responseResult1.getCode() ==  CommonStatusEnum.SUCCESS.getCode() && responseResult1.getData().toString().equals("false")){
                // 京东账户余额不足！
                return ResponseResult.fail("京东账户余额不足！");
            }
            try {
                //查询 封装支付参数
                List<CallBackVO> callBackParaList = new ArrayList<CallBackVO>();
                callBackParaList = treasureOrder(userInfo, orderId);

                // 创建支付宝客户端
                AlipayClient alipayClient = this.getAlipayClient();

                // 设置请求参数
                AlipayTradeAppPayRequest alipayRequest = new AlipayTradeAppPayRequest();

                // 商户订单号，商户网站订单系统中唯一订单号，必填
                String out_trade_no = orderId;
                // 付款金额，必填
                String total_amount = String.valueOf(totalPrice);
                // 订单名称，必填
                String subject = goodsName;
                // 商品描述，可空
                String body = "购物";
                // 封装请求支付信息
                AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
                model.setPassbackParams(JsonUtils.toString(callBackParaList));
                model.setOutTradeNo(out_trade_no);
                model.setTotalAmount(total_amount);
                model.setSubject(subject);
                model.setBody(body);
                model.setTimeoutExpress("30m");
                // 销售产品码 必填
                model.setProductCode("QUICK_MSECURITY_PAY");
                alipayRequest.setBizModel(model);
                alipayRequest.setReturnUrl(ALiPayConfig.shopping_notify_url_h5);
                alipayRequest.setNotifyUrl(ALiPayConfig.shopping_notify_url);
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

    private List<CallBackVO> treasureOrder(UserInfoVO userInfo, String orderId) {
        List<CallBackVO> callBackParaList = null;
        try {
            String userId = userInfo.getUserId();
            String userName = userInfo.getUserName();

            callBackParaList = new ArrayList<CallBackVO>();
            CallBackVO callBackPara = new CallBackVO();
            callBackPara.setUserId(userId);
            callBackPara.setUserName(userName);
            String treasureShopId = null;

            if (MathUtil.isInteger(orderId)) {
                GoodsDTO queryGoodsPara = payConsumeTradeBackMapper.queryGoodsPriceAndName(orderId, userId);
                BigDecimal paymentAmount = queryGoodsPara.getTradePaymentAmount();
                treasureShopId = orderServiceClient.queryTreasureShopIdByInt(orderId);
                //订单金额
                callBackPara.setOrderFullAmount(paymentAmount.toString());
                //店铺id
                callBackPara.setShopId(treasureShopId);
                callBackParaList.add(callBackPara);
            } else {

                List<RecordVO> rp = payConsumeRecordMapper.queryRecordParaList(orderId);
                for (int i = 0; i < rp.size(); i++) {
                    callBackPara = new CallBackVO();
                    //买家用户名
                    callBackPara.setUserName(userName);
                    //买家用户id
                    callBackPara.setUserId(userId);
                    String[] str = rp.get(i).getRecordPayorder().split(",");

                    List<String> shopIdList = Arrays.asList(str);

                    for (int j = 0; j < shopIdList.size(); j++) {
                        //店铺id
                        treasureShopId = orderServiceClient.queryTreasureShopIdByInt(shopIdList.get(j));
                        callBackPara.setShopId(treasureShopId);
                    }
                    callBackPara.setOrderFullAmount(rp.get(i).getRecordMoney().toString());
                    callBackParaList.add(callBackPara);
                }
            }

        } catch (Exception e) {
        }
        return callBackParaList;
    }


}
