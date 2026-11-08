package com.houjiang.payment.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.third.ConsumePointsReqVO;
import com.houjiang.commons.vo.third.UserAndShopInfoReqVo;
import com.houjiang.commons.vo.third.UserPointsInfoVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.common.PayCommonUtils;
import com.houjiang.payment.entity.PayConsumeTradeEntity;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.service.PayConsumeTradeService;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.vo.CallBackVO;
import io.seata.core.exception.TransactionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 嘉币、劲豆支付
 *
 * @author Amos
 */
@Slf4j
@Service
public class PointsPayServicesImpl implements PayStrategyHandleService {

    private static PayCommonUtils payCommonUtils;

    private static PayConsumeTradeService payConsumeTradeService;

    private static UsermsServiceClient usermsServiceClient;


    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayUserResourceService(UsermsServiceClient usermsServiceClient) {
        PointsPayServicesImpl.usermsServiceClient = usermsServiceClient;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayCommonUtils(PayCommonUtils payCommonUtils) {
        PointsPayServicesImpl.payCommonUtils = payCommonUtils;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setPayConsumeTradeService(PayConsumeTradeService payConsumeTradeService) {
        PointsPayServicesImpl.payConsumeTradeService = payConsumeTradeService;
    }

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) throws TransactionException {
        String token = (String) map.get("token");
        //获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(token);

        Map<Object, Object> assetDetailMap = new HashMap<>();
        // 接收付款订单编号
        String orderId = (String) map.get("orderId");

        OrderBaseVO orderBase = payCommonUtils.getOrderBase(null, orderId);

        if (BeanUtil.isEmpty(orderBase)) {
            return ResponseResult.fail(BizCodeEnume.OPERATION_FAILED);
        }
        BigDecimal orderCashPayment = orderBase.getOrderCashPayment();
        Integer customerType = orderBase.getCustomerType();

        //获取用户积分
        UserAndShopInfoReqVo vo = new UserAndShopInfoReqVo();
        vo.setUserId(userInfo.getThirdUserVO().getId());
        vo.setShopId(userInfo.getThirdShopVO().getId());
        ResponseResult<UserPointsInfoVO> pointsInfo = usermsServiceClient.getUserPointsInfo(vo);
        log.info("当前支付的用户嘉币/劲豆:{}", pointsInfo);
        UserPointsInfoVO pointsInfoVO = pointsInfo.getData();
        if (BeanUtil.isEmpty(pointsInfoVO)) {
            return ResponseResult.fail("积分不足");
        }


        BigDecimal balance = pointsInfoVO.getBalance();

        String orderNo = payConsumeTradeService.queryOrderId(orderId);
        // 修改积分
        ConsumePointsReqVO consumePointsReq = new ConsumePointsReqVO();
        consumePointsReq.setShopId(userInfo.getThirdShopVO().getId());
        consumePointsReq.setUserId(userInfo.getThirdUserVO().getId());
        consumePointsReq.setCurrencyId(userInfo.getCurrencyId());
        consumePointsReq.setCurrencyName(userInfo.getCurrencyName());

        if (userInfo.getThirdUserVO().getCustomerTypeId() == 1) {
        }
        if (userInfo.getThirdUserVO().getCustomerTypeId() == 2) {
        }

        consumePointsReq.setConsumeTitle("订单兑换");
//        consumePointsReq.setOrderid(orderNo);
        consumePointsReq.setBusinessUuid(orderNo);
        ResponseResult responseResult = usermsServiceClient.consumePoints(consumePointsReq);
        if (responseResult.getCode() != 1) {
            return ResponseResult.fail("扣减积分失败");
        }

        //支付
        return payCommonUtils.pointsPay(token, balance.subtract(consumePointsReq.getPoints()), userInfo.getCurrencyName(),
                userInfo.getUserId(), orderId, 12, consumePointsReq.getPoints(), assetDetailMap);

    }

}
