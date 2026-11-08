package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.dto.payment.AssetTmpInfoDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PayToolUtil;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.common.PayCommonUtils;
import com.houjiang.payment.entity.PayConsumeTradeEntity;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.service.PayConsumeTradeService;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.service.PayUserResourceService;
import com.houjiang.payment.vo.CallBackVO;
import io.seata.core.exception.TransactionException;
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
 * 余额支付
 *
 * @author Amos
 * @date 2023/4/27 10:34
 */
@Service
public class BalancePayServiceImpl extends BasePayServiceImpl implements PayStrategyHandleService {


    private static PayConsumeTradeService payConsumeTradeService;

    private static PayCommonUtils payCommonUtils;

    private static PayUserResourceService payUserResourceService;

    private static OrderServiceClient orderServiceClient;

    final Logger log = LoggerFactory.getLogger(BalancePayServiceImpl.class);
    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setChatService(PayUserResourceService payUserResourceService) {
        BalancePayServiceImpl.payUserResourceService = payUserResourceService;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayCommonUtils(PayCommonUtils payCommonUtils) {
        BalancePayServiceImpl.payCommonUtils = payCommonUtils;
    }
    @Autowired
    public void setOrderServiceClient(OrderServiceClient orderServiceClient) {
        BalancePayServiceImpl.orderServiceClient = orderServiceClient;
    }
    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayConsumeTradeService(PayConsumeTradeService payConsumeTradeService) {
        BalancePayServiceImpl.payConsumeTradeService = payConsumeTradeService;
    }

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) throws TransactionException {

            String token = (String) map.get("token");
            //获取用户信息
            UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);
            PayUserResourceEntity payUserResourceEntity = payUserResourceService.getById(userInfo.getUserId());
            Map<Object, Object> assetDetailMap = new HashMap<>();
            // 接收付款订单编号
            String orderId = (String) map.get("orderIdList");
            // 判断订单是否支付过
            ResponseResult responseResult2 = basePayServiceImpl.isPayOrder(orderId);
            if(responseResult2.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                return responseResult2;
            }

            String passwd = (String) map.get("passwd");
            String paymentNumber = orderId;
            //虚拟订单 0 是 1 否
            Integer virtualGoods = null;
            //门店字体订单
            Integer orderIsShopTake = null;
            // 错误次数
            Integer errorNum = 0;
            Map<String, String> orderIdIsNum = new HashMap<>();
            orderIdIsNum.put("orderId", orderId);
            if (null != paymentNumber) {
                if (paymentNumber.contains("[")) {
                    paymentNumber = paymentNumber.replace("[", "");
                }
                if (paymentNumber.contains("]")) {
                    paymentNumber = paymentNumber.replace("]", "");
                }
            }

            // 1:余额支付 2:购物卡支付
            if (RedisUtil.exists(HouConfig.DETERMINEWHETHERTOPAY + "-" + orderId + "-" + userInfo.getUserId() + "-" + map.get("type") + "")) {
                //订单已支付，请勿重复支付！
                return ResponseResult.fail(BizCodeEnume.ORDER_PAID);
            }
            //获取订单信息
            ResponseResult responseResult = payCommonUtils.getDeteails(orderId, paymentNumber);
            if (responseResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
                return responseResult;
            }
            List<CallBackVO> callBackParaList = payConsumeTradeService.treasureOrder(token, orderId);
            // 验证支付密码
            Integer isExist = payCommonUtils.getPwdExist(passwd, userInfo).getCode();
            if (isExist == 1) {
                BigDecimal amountActuallyPaids = new BigDecimal(0);
                Integer amountAotalIntegral = null;
                //积分商品 0  是  1 否
                Integer integralGoods = 1;
                // 判断纯数字订单
                if (PayToolUtil.isInteger((String) map.get("orderIdList"))) {
                    PayConsumeTradeEntity queryGoodsPara = payConsumeTradeService.getOne(new LambdaUpdateWrapper<PayConsumeTradeEntity>()
                            .eq(PayConsumeTradeEntity::getOrderId, orderId));
                    BigDecimal paymentAmount = queryGoodsPara.getTradePaymentAmount();
                    //获取积分商品订单的积分
                    Integer totalIntegral = queryGoodsPara.getTotalIntegral();
                    //积分商品 0  是  1 否
                    integralGoods = queryGoodsPara.getIntegralGoods();
                    amountAotalIntegral = totalIntegral;
                    amountActuallyPaids = paymentAmount;
                    //订单号是数字
                    orderIdIsNum.put("isNum", "0");
                } else {
                    // 实付金额
                    String amountActuallyPaid = RedisUtil
                            .get(HouConfig.HOU_USERAMOUNTACTUALLYPAID + "--" + token + "--" + userInfo.getUserId());
                    // 获取积分商品的积分
                    String totalIntegral = RedisUtil
                            .get(HouConfig.HOU_USERINTEGRALYPAID + "--" + token + "--" + userInfo.getUserId());
                    // 类型转换
                    amountActuallyPaids = new BigDecimal(amountActuallyPaid);
                    if (null != totalIntegral) {
                        //amountAotalIntegral 不为null 证明是积分订单
                        amountAotalIntegral = Integer.valueOf(totalIntegral);
                        integralGoods = 0;
                    }
                    //订单号不是数字
                    orderIdIsNum.put("isNum", "1");
                }
                String orderNo = null;
                if(integralGoods == 0){
                    orderNo = payConsumeTradeService.queryOrderId(orderId);
                }
                // 查询账户余额
                BigDecimal userBalanceMoney = payUserResourceEntity.getUserMoney();
                ResponseResult integralResult = payCommonUtils.getUserIntegral(integralGoods, userInfo.getUserId(), amountAotalIntegral, orderNo);
                if (integralResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
                    return integralResult;
                }
                // 判断是否京东订单 且 是否当前支付是京东余额支付 且 余额是否够支付
                ResponseResult responseResult1 = orderServiceClient.queryJDBalanceIsEnough(orderId, amountActuallyPaids);
                if(responseResult1.getCode() ==  CommonStatusEnum.SUCCESS.getCode() && responseResult1.getData().toString().equals("false")){
                    // 京东账户余额不足！
                    return ResponseResult.fail("京东账户余额不足！");
                }
                // 判断实付金额与余额
                if (userBalanceMoney.compareTo(amountActuallyPaids) > 0
                        || userBalanceMoney.compareTo(amountActuallyPaids) == 0) {
                    // 扣除购物金额
                    boolean updateMoney = payUserResourceService.update(new LambdaUpdateWrapper<PayUserResourceEntity>()
                            .eq(PayUserResourceEntity::getUserId, userInfo.getUserId())
                            .set(PayUserResourceEntity::getUserMoney, payUserResourceEntity.getUserMoney().subtract(amountActuallyPaids)));
                    //支付
                    List<AssetTmpInfoDTO> tmpInfo = JSON.parseArray(JSON.toJSONString(responseResult.getData()), AssetTmpInfoDTO.class);
                    return payCommonUtils.getPay(updateMoney, token, userInfo.getUserId(), orderId, map.get("type").toString(), virtualGoods, orderIsShopTake, amountActuallyPaids, amountAotalIntegral,
                            orderIdIsNum, callBackParaList, integralGoods, assetDetailMap, tmpInfo);
                } else {
                    //账户余额不足！
                    return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_ACCOUNT_BALANCE);
                }
            } else {
                errorNum++;
                if (errorNum == 3) {
                    //密码错误三次！
                    return ResponseResult.fail(BizCodeEnume.PASSWORD_ERROR_THREE_TIMES);
                }
                return ResponseResult.fail(BizCodeEnume.PASSWORD_ERROR_TWO);
            }
    }
}
