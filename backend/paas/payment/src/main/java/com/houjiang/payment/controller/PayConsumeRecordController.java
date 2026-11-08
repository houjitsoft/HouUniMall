package com.houjiang.payment.controller;


import com.houjiang.commons.dto.order.ConsumeRecordByBusinessDTO;
import com.houjiang.commons.dto.order.ConsumeRecordParaDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.FinancialDetailsDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName PayConsumeRecordController
 * @Description 交易明细表-账户收支明细-资金流水表-账户金额变化流水
 * @Author Amos
 * @Date 2023-05-31 15:36:55
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/consumeRecord")
public class PayConsumeRecordController extends BaseController {
    /**
     * 插入购物记录 商家
     *
     * @param crb
     */
    @PostMapping("/insertByBusiness")
    public boolean insertByBusiness(@RequestBody ConsumeRecordByBusinessDTO crb) {
        return payConsumeRecordService.insertByBusiness(crb);
    }
    /**
     * 插入购物记录 买家
     *
     * @param crp
     * @return
     */
    @PostMapping("insertByBuyer")
    public boolean insertByBuyer(@RequestBody ConsumeRecordParaDTO crp) {
        boolean isInsertSuccess = payConsumeRecordService.insertByBuyer(crp);
        return isInsertSuccess;
    }
    /**
     * 最近交易记录查询
     */
    @PostMapping("/query/dealRecord")
    public ResponseResult queryDealRecord(Map<Object, Object> map) {
        return payConsumeRecordService.queryDealRecord(map);
    }
    /**
     * 删除最近交易记录查询
     */
    @PostMapping("/delete/dealRecord")
    public ResponseResult deleteDealRecord(Map<Object, Object> map) {
        return payConsumeRecordService.deleteDealRecord(map);
    }
    /**
     * 最近交易记录详情
     */
    @PostMapping("/query/dealRecordDetails")
    public ResponseResult queryDealRecordDetails(Map<Object, Object> map) {
        return payConsumeRecordService.queryDealRecordDetails(map);
    }

    /**
     * 取消付款状态
     *
     * @param orderId
     * @return
     */
    @PostMapping("/updateCancelStatus")
    public ResponseResult updateCancelStatus(@RequestBody String orderId) {
        ResponseResult result = payConsumeRecordService.updateCancelStatus(orderId);
        return result;
    }

    /**
     * 发货成功，修改状态
     *
     * @param orderId
     * @return
     */
    @PostMapping("/change")
    public ResponseResult change(@RequestBody String orderId) {
        ResponseResult isInsertSuccess = payConsumeRecordService.change(orderId);
        return isInsertSuccess;
    }

    /**
     * 新增交易明细
     *
     * @param returnMap
     * @return
     */
    @PostMapping("/insertRecord")
    public ResponseResult insertRecord(@RequestBody Map<String, Map<String, String>> returnMap) {
        ResponseResult torf = payConsumeRecordService.insertRecord(returnMap);
        return torf;
    }

    /**
     * 更新退款金额
     *
     * @param feignMap
     */
    @PostMapping("/updateRefuse")
    public void updateRefuse(@RequestBody Map<String, String> feignMap) {
        payConsumeRecordService.updateRefuse(feignMap);
    }

    /**
     * 平台查询 销售统计
     *
     * @param param
     * @return
     */
    @PostMapping("/queryStatistics")
    public ResponseResult querySalesStatistics(@RequestBody ShopSalesStatisticsDTO param) {
        ResponseResult result=payConsumeRecordService.querySalesStatistics(param);
        return result;
    }


    /**
     * 平台查询 销售统计 导出
     *
     * @param param
     * @return
     */
    @PostMapping("/queryStatisticsExcel")
    public ResponseResult querySalesStatisticsExcel(@RequestBody ShopSalesStatisticsDTO param) {
        ResponseResult result=payConsumeRecordService.querySalesStatisticsExcel(param);
        return result;
    }


    /**
     * 平台查询 资金明细
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/queryDetails")
    public ResponseResult platformQueryDetails(@RequestBody FinancialDetailsDTO param) {
        ResponseResult result=payConsumeRecordService.platformQueryDetails(param);
        return result;
    }


    /**
     * 添加结算单订单
     *
     * @param map
     * @return
     */
    @PostMapping("/insertSettlementRecord")
    @Transactional
    public boolean insertSettlementRecord(@RequestBody Map<String, Object> map) {
        boolean isInsertSuccess = payConsumeRecordService.insertSettlementRecord(map);
        return isInsertSuccess;
    }



    /**
     * 最近交易查询
     *
     * @param map
     * @return
     */
    @PostMapping("/wallet/recentTransactionOrder")
    public ResponseResult recentTransactionOrder(@RequestBody Map<String, Object> map) {
        ResponseResult result=payConsumeRecordService.recentTransactionOrder(map);
        return result;
    }

    /**
     * 待发货退款
     *
     * @param refundPara
     * @return
     */
    @PostMapping("/waitShipmentsRefund")
    public boolean waitShipmentsRefund(@RequestBody RefundParamDTO refundPara){
        boolean isRefundSuccess = payConsumeRecordService.waitShipmentsRefund(refundPara);
        return isRefundSuccess;
    }

    /**
     * 插入消费记录
     * @param comRecord
     */
    @PostMapping("/feign/insertConsumeRecordport")
    public void insertConsumeRecordport(@RequestBody Map<String, Object> comRecord){
        payConsumeRecordService.insertConsumeRecordport(comRecord);
    }

}
