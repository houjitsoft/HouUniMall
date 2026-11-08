package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.CardAmountDTO;
import com.houjiang.payment.dto.RechargeCardDTO;
import com.houjiang.payment.dto.ReturnCardBaseInfoDTO;
import com.houjiang.payment.entity.PayCardBaseEntity;
import io.seata.core.exception.TransactionException;

import java.util.Map;

/**
 * @author Amos
 */
public interface PayCardBaseService extends IService<PayCardBaseEntity> {

    /**
     * 平台 购物卡配置 激活充值卡 查看
     *
     * @param dto
     * @return
     */
     ResponseResult queryDetail(ReturnCardBaseInfoDTO dto);

    /**
     * 充值卡管理列表
     *
     * @param map
     * @return
     */
    ResponseResult queryList(Map<String,Object> map);

    /**
     * 查询购物卡消费支出列表 导出
     *
     * @param param
     * @return
     */
    ResponseResult queryCardConsumeExcel(CardAmountDTO param);

    /**
     * 查询购物卡消费支出列表
     *
     * @param param
     * @return
     */
    ResponseResult queryCardConsume(CardAmountDTO param);

    /**
     * 平台查询 激活充值卡列表 导出
     *
     * @param cardAmountQueryParam
     * @return
     */
    ResponseResult queryCardAmountListExcel(CardAmountDTO cardAmountQueryParam);



    /**
     * 平台查询 激活充值卡列表
     *
     * @param cardAmountQueryParam
     * @return
     */
    ResponseResult queryCardAmountList(CardAmountDTO cardAmountQueryParam);

    /**
     * 平台 激活充值卡
     *
     * @param checkRechargeCardRequest
     * @return
     */
    ResponseResult check(RechargeCardDTO checkRechargeCardRequest) throws TransactionException;
}
