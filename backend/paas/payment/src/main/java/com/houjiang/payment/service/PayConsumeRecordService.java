package com.houjiang.payment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.order.ConsumeRecordByBusinessDTO;
import com.houjiang.commons.dto.order.ConsumeRecordParaDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.dto.FinancialDetailsDTO;
import com.houjiang.payment.entity.PayConsumeRecordEntity;

import java.util.Map;

/**
 * @ClassName PayConsumeRecordService
 * @Description  交易明细表-账户收支明细-资金流水表-账户金额变化流水
 * @Author Amos
 * @Date 2023-05-31 15:36:55
 * @Version 1.0
 * @author Amos
 */
public interface PayConsumeRecordService extends IService<PayConsumeRecordEntity> {
     /**
      * 插入购物记录 商家
      * @param crb
      * @return
      */
     boolean insertByBusiness(ConsumeRecordByBusinessDTO crb);

     /**
      * 插入购物记录 买家
      * @param crp
      * @return
      */
     boolean insertByBuyer(ConsumeRecordParaDTO crp);

     /**
      * 最近交易记录查询
      * @param map
      * @return
      */
     ResponseResult queryDealRecord(Map<Object, Object> map);

     /**
      * 删除最近交易记录查询
      * @param map
      * @return
      */
     ResponseResult deleteDealRecord(Map<Object, Object> map);

     /**
      * 最近交易记录详情
      * @param map
      * @return
      */
     ResponseResult queryDealRecordDetails(Map<Object, Object> map);

     /**
      * 取消付款状态
      *
      * @param orderId
      * @return
      */
     ResponseResult updateCancelStatus(String orderId);

     /**
      * 发货成功，修改状态
      *
      * @param orderId
      * @return
      */
     ResponseResult change(String orderId);

    /**
     * 新增交易明细
     *
     * @param returnMap
     * @return
     */
    ResponseResult insertRecord(Map<String, Map<String, String>> returnMap);


     /**
      * 更新退款金额
      *
      * @param feignMap
      */
     void updateRefuse(Map<String, String> feignMap);

    /**
     * 平台查询 销售统计
     *
     * @param param
     * @return
     */
    ResponseResult querySalesStatistics(ShopSalesStatisticsDTO param);


    /**
     * 平台查询 销售统计 导出
     *
     * @param param
     * @return
     */
  ResponseResult querySalesStatisticsExcel(ShopSalesStatisticsDTO param);


    /**
     * 平台查询 资金明细
     *
     * @param param
     * @return
     */
    ResponseResult platformQueryDetails(FinancialDetailsDTO param);


    /**
     * 添加结算单订单
     *
     * @param map
     * @return
     */
    boolean insertSettlementRecord(Map<String, Object> map);


    /**
     * 最近交易查询
     *
     * @param map
     * @return
     */
    ResponseResult recentTransactionOrder(Map<String, Object> map);

    /**
     * 待发货退款
     *
     * @param refundPara
     * @return
     */
    boolean waitShipmentsRefund(RefundParamDTO refundPara);

    /**
     * 插入消费记录
     * @param comRecord
     */
    void insertConsumeRecordport(Map<String, Object> comRecord);
 }
