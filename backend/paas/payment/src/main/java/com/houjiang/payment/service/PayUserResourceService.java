package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.CardMemberDetailDTO;
import com.houjiang.payment.entity.PayUserResourceEntity;
import io.seata.core.exception.TransactionException;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author Amos
 */
public interface PayUserResourceService extends IService<PayUserResourceEntity> {

    /**
     * 钱包余额展示
     *
     * @param map
     * @return
     */
    ResponseResult balanceDisplay(Map<String, Object> map);

    /**
     * 增加卖家金额
     *
     * @param am
     * @return
     */
    ResponseResult addMoney(Map<String, Object> map);

    /**
     * 用户余额提现
     *
     * @param map
     * @param request
     * @return
     */
    ResponseResult withdrawal(Map<Object, Object> map, HttpServletRequest request);

    /**
     * 用户转账
     *
     * @param map
     * @return
     */
    ResponseResult transfer(Map<Object, Object> map);


    /**
     * 平台查询 会员充值 会员充值
     *
     * @param param
     * @return
     * @throws TransactionException
     */
    ResponseResult updateCardMemberDetailPort(CardMemberDetailDTO param) throws TransactionException;


    /**
     * 平台查询 会员充值列表
     *
     * @param map
     * @return
     */
    ResponseResult query(Map<String, Object> map);

    /**
     * 修改个人余额
     * @param userId
     * @param rechargeAmount
     * @return
     */
    ResponseResult updateUserBalance(String userId, BigDecimal rechargeAmount);


    /**
     * 查询个人余额
     * @param userId
     * @return
     */
    String queryAccountBalance(String userId);


    /**
     * 修改会员余额
     * @param manageMoney
     */
    void updateCardMemberDetailPortFeign(Map<String, Object> manageMoney);


    /**
     * 修改会员余额
     * @param manageMoney
     * @return
     */
    Integer updateCardMemberDetailPort2(Map<String, Object> manageMoney);
}
