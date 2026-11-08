package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.ConsumeWithdrawDTO;
import io.seata.core.exception.TransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName PayConsumeWithdrawController
 * @Description
 * @Author Amos
 * @Date 2023/7/18 14:55
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/consumeWithdraw")
public class PayConsumeWithdrawController extends BaseController {

    /**
     * 蜂乐pay提现之获取商家结算单号
     *
     * @param map
     * @return
     */
    @PostMapping("/query/settlementOrder")
    public ResponseResult querySettlementOrder(@RequestBody Map<String, Object> map) {
        ResponseResult result = payConsumeWithdrawService.querySettlementOrder(map);
        return result;
    }

    /**
     * 蜂乐pay提现之计算结算金额
     *
     * @param map
     * @return
     */
    @PostMapping("/query/money")
    public ResponseResult queryMoney(@RequestBody Map<String, Object> map) {
        ResponseResult result = payConsumeWithdrawService.queryMoney(map);
        return result;
    }

    /**
     * 蜂乐pay商家端提现app
     *
     * @param map
     * @return
     */
    @PostMapping("/query/withdrawal")
    public ResponseResult queryWithdrawal(@RequestBody Map<String, Object> map) {
        ResponseResult result = payConsumeWithdrawService.queryWithdrawal(map);
        return result;
    }

    /**
     * 蜂乐pay商家端商户信息展示
     *
     * @param map
     * @return
     */
    @PostMapping("/query/userInfo")
    public ResponseResult queryUserInfo(@RequestBody Map<String, Object> map) {
        ResponseResult result = payConsumeWithdrawService.queryUserInfo(map);
        return result;
    }

    /**
     * 商家提现界面展示
     *
     * @param map
     * @return
     */
    @PostMapping("/query/business")
    public ResponseResult queryBusiness(@RequestBody Map<String, Object> map) {
        ResponseResult result = payConsumeWithdrawService.queryBusiness(map);
        return result;
    }

    /**
     * 商家查看提现记录
     *
     * @param map
     * @return
     */
    @PostMapping("/query/businessCheck")
    public ResponseResult queryBusinessCheck(@RequestBody Map<String, Object> map) {
        ResponseResult result = payConsumeWithdrawService.queryBusinessCheck(map);
        return result;
    }


    /**
     * 平台查询 提现订单列表 导出
     *
     * @param consumeWithdrawDto
     * @return
     */
    @PostMapping("/platform/excel")
    public ResponseResult platformExecl(@RequestBody ConsumeWithdrawDTO consumeWithdrawDto) {
        ResponseResult result=payConsumeWithdrawService.platformExecl(consumeWithdrawDto);
        return result;
    }

    /**
     * 平台查询 提现订单详情
     *
     * @param consumeWithdrawDto
     * @return
     */
    @PostMapping("/platform/detail")
    public ResponseResult platformDetail(@RequestBody ConsumeWithdrawDTO consumeWithdrawDto) {
        ResponseResult result=payConsumeWithdrawService.platformDetail(consumeWithdrawDto);
        return result;
    }


    /**
     * 平台审核 提现操作
     *
     * @param map
     * @return
     */
    @PostMapping("/platform/withdrawalAudit")
    public ResponseResult withdrawalAudit(@RequestBody Map<String, Object> map) {
        ResponseResult result=payConsumeWithdrawService.withdrawalAudit(map);
        return result;
    }


    /**
     * 平台查询 提现订单列表
     *
     * @param consumeWithdrawDto
     * @return
     */
    @PostMapping("/platform/withdrawDepositList")
    public ResponseResult withdrawDepositList(@RequestBody ConsumeWithdrawDTO consumeWithdrawDto) {
        ResponseResult result=payConsumeWithdrawService.withdrawDepositList(consumeWithdrawDto);
        return result;
    }

    /**
     * 供应商提现界面展示
     *
     * @param map
     * @return
     */
    @PostMapping("/supplierWithdrawDeposit")
    public ResponseResult supplierWithdrawDeposit(@RequestBody Map<String, Object> map) {
        ResponseResult rr = payConsumeWithdrawService.supplierWithdrawDeposit(map);
        return rr;
    }

    /**
     * 提现之获取供应商结算单号
     *
     * @param map
     * @return
     */
    @PostMapping("/fkPayForSupplierWithdrawal")
    public ResponseResult fkPayForSupplierWithdrawal(@RequestBody Map<Object, Object> map) {
        ResponseResult rr = payConsumeWithdrawService.fkPayForSupplierWithdrawal(map);
        return rr;
    }

    /**
     * 供应商端提现
     *
     * @param map
     * @return
     */
    @PostMapping("/supplierForMerchantsWithdrawal")
    @Transactional
    public ResponseResult supplierForMerchantsWithdrawal(@RequestBody Map<Object, Object> map) throws TransactionException {
        ResponseResult rr = payConsumeWithdrawService.supplierForMerchantsWithdrawal(map);
        return rr;
    }

}
