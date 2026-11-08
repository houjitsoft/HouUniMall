package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.ConsumeWithdrawDTO;
import com.houjiang.payment.entity.PayConsumeWithdrawEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface PayConsumeWithdrawService extends IService<PayConsumeWithdrawEntity> {

    /**
     * 蜂乐pay提现之获取商家结算单号
     *
     * @param map
     * @return
     */
    ResponseResult querySettlementOrder(Map<String, Object> map);

    /**
     * 蜂乐pay提现之计算结算金额
     *
     * @param map
     * @return
     */
    ResponseResult queryMoney(Map<String, Object> map);

    /**
     * 蜂乐pay商家端提现app
     *
     * @param map
     * @return
     */
    ResponseResult queryWithdrawal(Map<String, Object> map);

    /**
     * 蜂乐pay商家端商户信息展示
     *
     * @param map
     * @return
     */
    ResponseResult queryUserInfo(Map<String, Object> map);

    /**
     * 商家提现界面展示
     *
     * @param map
     * @return
     */
    ResponseResult queryBusiness(Map<String, Object> map);

    /**
     * 商家查看提现记录
     *
     * @param map
     * @return
     */
    ResponseResult queryBusinessCheck(Map<String, Object> map);


    /**
     * 平台查询 提现订单列表 导出
     *
     * @param consumeWithdrawDto
     * @return
     */
    ResponseResult platformExecl(ConsumeWithdrawDTO consumeWithdrawDto);


    /**
     * 平台查询 提现订单详情
     *
     * @param consumeWithdrawDto
     * @return
     */
    ResponseResult platformDetail(ConsumeWithdrawDTO consumeWithdrawDto);

    /**
     * 平台审核 提现操作
     *
     * @param map
     * @return
     */
    ResponseResult withdrawalAudit(Map<String, Object> map);


    /**
     * 平台查询 提现订单列表
     *
     * @param consumeWithdrawDto
     * @return
     */
    ResponseResult withdrawDepositList(ConsumeWithdrawDTO consumeWithdrawDto);


    /**
     * 处理供应商提现申请的接口
     * 该方法接收一个参数映射，包含供应商提现所需的信息，并返回处理结果
     *
     * @param map 包含供应商提现申请相关信息的映射，如供应商ID、提现金额等
     * @return 返回一个ResponseResult对象，包含处理结果和相关状态信息
     */
    ResponseResult supplierWithdrawDeposit(Map<String, Object> map);

    /**
     * 供应商提现时进行付款的接口
     * 此方法用于处理供应商提现过程中的付款操作，接收一个参数映射，包含付款所需信息，并返回处理结果
     *
     * @param map 包含供应商付款相关信息的映射，如供应商ID、付款金额等
     * @return 返回一个ResponseResult对象，包含处理结果和相关状态信息
     */
    ResponseResult fkPayForSupplierWithdrawal(Map<Object, Object> map);

    /**
     * 供应商为商户提现的接口
     * 该方法允许供应商为商户执行提现操作，接收一个参数映射，包含提现所需信息，并返回处理结果
     *
     * @param map 包含商户提现相关信息的映射，如商户ID、提现金额等
     * @return 返回一个ResponseResult对象，包含处理结果和相关状态信息
     */
    ResponseResult supplierForMerchantsWithdrawal(Map<Object, Object> map);
}
