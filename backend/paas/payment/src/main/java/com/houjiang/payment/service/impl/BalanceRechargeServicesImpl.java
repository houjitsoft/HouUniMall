package com.houjiang.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.common.PayCommonUtils;
import com.houjiang.payment.entity.PayUserResourceEntity;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.service.PayConsumeTradeService;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.service.PayUserResourceService;
import io.seata.core.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 余额充值 - 线下支付
 *
 * @author Amos
 * @date 2023/4/27 10:34
 */
@Service
public class BalanceRechargeServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static OrderServiceClient orderServiceClient;


    final Logger log = LoggerFactory.getLogger(BalanceRechargeServicesImpl.class);


    @Autowired
    public void setOrderServiceClient (OrderServiceClient orderServiceClient){
        BalanceRechargeServicesImpl.orderServiceClient = orderServiceClient;
    };

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) throws TransactionException {
        // 初始化微信支付pc、h5配置
        this.initWeChatPayConfig();
        String orderId = (String) map.get("rechargeOrderId");
        // 判断订单是否支付过
        ResponseResult responseResult2 = basePayServiceImpl.isPayBalanceRechargeOrder(orderId);
        if(responseResult2.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            return responseResult2;
        }
        map.put("payCode", CommonType.balanceRechargeOrderStart.OFFLINE_PAY.getCode());
        map.put("rechargeOrderStart",CommonType.balanceRechargeOrderStart.WAITING_AUDIT.getCode());
        //插入记录信息
        ResponseResult recharge = orderServiceClient.updateBalanceRechargeOrder(map);
        if (recharge.getCode() != 1){
            // 回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return recharge;
    }
}
