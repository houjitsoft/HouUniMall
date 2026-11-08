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
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.service.PayConsumeTradeService;
import com.houjiang.payment.service.PayStrategyHandleService;
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
 * 纯积分支付
 *
 * @author Amos
 */
@Service
public class IntegralPayServicesImpl implements PayStrategyHandleService {

    private static PayCommonUtils payCommonUtils;

    private static PayConsumeTradeService payConsumeTradeService;

    private static UsermsServiceClient usermsServiceClient;

    final Logger log = LoggerFactory.getLogger(GiftCardPayServiceImpl.class);


    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayUserResourceService(UsermsServiceClient usermsServiceClient) {
        IntegralPayServicesImpl.usermsServiceClient = usermsServiceClient;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayCommonUtils(PayCommonUtils payCommonUtils) {
        IntegralPayServicesImpl.payCommonUtils = payCommonUtils;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayConsumeTradeService(PayConsumeTradeService payConsumeTradeService) {
        IntegralPayServicesImpl.payConsumeTradeService = payConsumeTradeService;
    }

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) throws TransactionException {
        String token = (String) map.get("token");
        //获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);

        Map<Object, Object> assetDetailMap = new HashMap<>();
        // 接收付款订单编号
        String orderId = (String) map.get("orderIdList");
        String paymentNumber = orderId;
        //虚拟订单 0 是 1 否
        Integer virtualGoods = null;
        //门店字体订单
        Integer orderIsShopTake = null;
        Map<String, String> orderIdIsNum = new HashMap<>();
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
        BigDecimal amountActuallyPaids = new BigDecimal(0);
        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        // 判断纯数字订单
        if (PayToolUtil.isInteger((String) map.get("orderIdList"))) {
            PayConsumeTradeEntity queryGoodsPara = payConsumeTradeService.getOne(new LambdaUpdateWrapper<PayConsumeTradeEntity>()
                    .eq(PayConsumeTradeEntity::getOrderId, orderId));
            //获取积分商品订单的积分
            Integer totalIntegral = queryGoodsPara.getTotalIntegral();
            //积分商品 0  是  1 否
            integralGoods = queryGoodsPara.getIntegralGoods();
            amountAotalIntegral = totalIntegral;
            amountActuallyPaids = BigDecimal.ZERO;
            //订单号是数字
            orderIdIsNum.put("isNum", "0");
        } else {
            // 实付金额
            String amountActuallyPaid = RedisUtil
                    .get(HouConfig.HOU_USERAMOUNTACTUALLYPAID + "--" + token + "--" + userInfo.getUserId());
            // 获取积分商品的积分
            String totalIntegral = RedisUtil.get(HouConfig.HOU_USERINTEGRALYPAID + "--" + token + "--" + userInfo.getUserId());
            // 类型转换
            amountActuallyPaids = new BigDecimal(amountActuallyPaid != null ? amountActuallyPaid : "0");
            if (null != totalIntegral) {
                //amountAotalIntegral 不为null 证明是积分订单
                amountAotalIntegral = Integer.valueOf(totalIntegral);
                integralGoods = 0;
            }
            //订单号不是数字
            orderIdIsNum.put("isNum", "1");
        }
        //获取用户积分
        Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userInfo.getUserId());
        Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userInfo.getUserId());
        userIntegral = userIntegral - frozenIntegra;
        if(userIntegral < 0){
            userIntegral = 0;
        }
        if (userIntegral == null || userIntegral == 0) {
            return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR);
        }
        String orderNo = payConsumeTradeService.queryOrderId(orderId);
        // 修改积分
        ResponseResult integralResult = payCommonUtils.getUserIntegral(integralGoods, userInfo.getUserId(), amountAotalIntegral, orderNo);
        if (integralResult.getCode() == CommonStatusEnum.FAIL.getCode()) {
            return integralResult;
        }
        //支付
        List<AssetTmpInfoDTO> tmpInfo = JSON.parseArray(JSON.toJSONString(responseResult.getData()), AssetTmpInfoDTO.class);
        return payCommonUtils.getPay(true, token, userInfo.getUserId(), orderId, map.get("type").toString(), virtualGoods, orderIsShopTake, amountActuallyPaids, amountAotalIntegral,
                orderIdIsNum, callBackParaList, integralGoods, assetDetailMap, tmpInfo);

    }
}
