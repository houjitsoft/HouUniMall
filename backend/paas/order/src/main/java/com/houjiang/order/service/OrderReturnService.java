package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.ApplyReturnOrderDTO;
import com.houjiang.order.dto.OrderRemindDeliveryDTO;
import com.houjiang.order.dto.ReturnReportQueryDTO;
import com.houjiang.order.dto.WaitPlatformRefundDTO;
import com.houjiang.order.entity.OrderReturnEntity;
import io.seata.core.exception.TransactionException;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
public interface OrderReturnService extends IService<OrderReturnEntity> {
    /**
     * 未发货退款
     *
     * @param map
     * @return
     */
    ResponseResult applyRefund(Map<String, String> map) throws TransactionException;


    /**
     * 申请退货
     *
     * @param map
     * @return
     */
    ResponseResult applyReturn(Map<String, String> map) throws TransactionException;


    /**
     * 申请退多件货物（退货退款）
     *
     * @param applyReturnOrders
     * @return
     */
    ResponseResult applyReturnMore(ApplyReturnOrderDTO applyReturnOrders) throws TransactionException;

    /**
     * 仅退款
     *
     * @param map
     * @return
     */
    ResponseResult onlyReturnMoney(Map<String, Object> map) throws TransactionException;

    /**
     * 查询退款/退货订单
     *
     * @param paraMap
     * @return
     */
    ResponseResult listReturnOrderByRT(Map<String, Object> paraMap);


    /**
     * 退货详情
     *
     * @param map
     * @return
     */
    ResponseResult returnDetail(Map<String, String> map);

    /**
     * 查询退款详情
     *
     * @param map
     * @return
     */
    ResponseResult refundDetail(Map<String, String> map);

    /**
     * 买家填写物流信息
     *
     * @param map
     * @return
     */
    ResponseResult returnShipping(Map<String, String> map);

    /**
     * 查询退款服务详情
     *
     * @param map
     * @return
     */
    ResponseResult selectordereturn(Map<String, Object> map);


    /**
     * 买家撤销
     *
     * @param map
     * @return
     */
    ResponseResult revokeIntervention(Map<String, Object> map);

    /**
     * 查询退款服务详情有平台介入
     *
     * @param map
     * @return
     */
    ResponseResult selectordereturnplatform(Map<String, Object> map);


    /**
     * 判断是否参加过售后
     *
     * @param map
     * @return com.yf.ShoppingCart.web.entity.ResultResp
     */
    ResponseResult checkAfterSales(Map<String, Object> map);

    /**
     * @Description: 买家取消平台介入
     * @Param: [map]
     * @Return: com.yf.ShoppingCart.web.entity.ResultResp
     */
    ResponseResult revokePlatformIntervention(Map<String, Object> map);


    /**
     * 计算退货价格（退货退款）
     *
     * @param applyReturnOrders
     * @return
     */
    ResponseResult calculatePrice(ApplyReturnOrderDTO applyReturnOrders);

    /**
     * 获取退款订单列表
     *
     * @param paraMap
     * @return
     */
    ResponseResult listRefundOrder(Map<String, Object> paraMap);

    /**
     * 退款订单详情
     *
     * @param paraMap
     * @return
     */
    ResponseResult PlatformReturnDetail(Map<String, Object> paraMap);

    /**
     * 获取退款订单报表导出
     *
     * @param paraMap
     * @return
     */
    ResponseResult listRefundOrderExcel(Map<String, String> paraMap, HttpServletResponse response);

    /**
     * 查看提醒发货列表
     *
     * @param param
     */
    ResponseResult queryOrderRemindDeliveryPort(OrderRemindDeliveryDTO param);

    /**
     * 提醒发货
     *
     * @param param
     */
    ResponseResult OrderRemindDeliveryPort(Map<String, String> param);

    /**
     * 查看提醒发货列表导出
     *
     * @param param
     */
    ResponseResult queryOrderRemindDeliveryPortExcel(OrderRemindDeliveryDTO param);

    /**
     * 获取退款订单
     */
    ResponseResult listReturnOrder(Map<String, Object> paraMap);

    /**
     * 获取退货订单报表导出
     */
    ResponseResult listrefundorderExcel(Map<String, String> paraMap, HttpServletResponse response);


    /**
     * 财务退货及退款汇总
     *
     * @param param
     * @return
     */
    ResponseResult queryReturnsaleReport(ReturnReportQueryDTO param) throws IllegalAccessException;


    /**
     * 平台查询 售后服务 列表 导出
     *
     * @param param
     * @return
     */
    ResponseResult queryReturnsaleReportExcel(ReturnReportQueryDTO param);

    /**
     * 查看退款订单
     *
     * @param map
     * @return
     */
    ResponseResult listRefund(Map<String, Object> map) throws ParseException;

    /**
     * 审核退款订单
     *
     * @param map
     * @return
     */
    ResponseResult forSellerApprove(Map<String, String> map) throws TransactionException;

    /**
     * 京东取消订单 商城端退款
     * @param jdOrderIds
     * @return
     * @throws TransactionException
     */
    ResponseResult jingdongForSellerApprove(List<String> jdOrderIds) throws TransactionException;

    /**
     * 查看退货订单
     *
     * @param paraMap
     * @return
     */
    ResponseResult returnorder(Map<String, Object> paraMap) throws ParseException;

    /**
     * 审核退货订单
     *
     * @param map
     * @return
     */
    ResponseResult retForSellerApprove(Map<String, Object> map);

    /**
     * 京东售后单 退换货 处理
     * @param map
     * @return
     */
    ResponseResult jingDongReturnAudit(Map<String, Object> map);

    /**
     * 商家确认收货
     *
     * @param map
     * @return
     */
    ResponseResult retForSellerReceive(Map<String, String> map) throws TransactionException;

    /**
     * 京东 退货 换货 确认收货后 商品质检审核 通过 不通过
     * @param map
     * @return
     * @throws TransactionException
     */
    ResponseResult jingDongQualityInspectionReceive(Map<String, Object> map) throws TransactionException;

    /**
     * 商家查询退款服务详情
     *
     * @param map
     * @return
     */
    ResponseResult selectshopordereturn(Map<String, Object> map);

    /**
     * 卖家上传平台介入凭证
     *
     * @param map
     * @return
     */
    ResponseResult insertShopIntervention(Map<String, Object> map);

    /**
     * 唤起售后单
     *
     * @param map
     * @return
     */
    ResponseResult callReturnOrder(Map<String, Object> map);

    /**
     * 商家填写物流信息
     *
     * @param map
     * @return
     */
    ResponseResult shopReturnShipping(Map<String, String> map) throws TransactionException;

    /**
     * 平台查询线下支付售后订单
     */
    ResponseResult queryWaitingForPlatformRefund(Map<String, Object> paraMap);

    /**
     * 平台导出线下支付售后订单
     */
    ResponseResult queryWaitingForPlatformRefundExp(Map<String, String> paraMap);

    /**
     * 线下支付订单 平台上传退款支付凭证
     */
    ResponseResult waitingForPlatformRefund(WaitPlatformRefundDTO dto);

    /**
     * 定时任务更新商家超时未处理退款售后
     *
     * @return
     */
    Boolean updateTimeOutReturn();

    /**
     * 定时任务商家同意退货
     *
     * @return
     */
    Boolean updateTimeOutReturnGoods();


    /**
     * 定时任务  买家超时未处理 退款
     *
     * @return
     */
    Boolean updateTimeOutBuyReturn();


    /**
     * 定时任务 退货 买家超时未处理
     *
     * @return
     */
    Boolean updatebuyTimeOutReturn();

    /**
     * 商家同意退货 买家未在有效期内填写物流信息
     *
     * @return
     */
    Boolean updatebuyTimeOutReturnGoods();


    /**
     * 根据订单号查询退款订单号。
     *
     * @param orderId 原始订单号，用于查询对应的退款订单号。
     * @return 返回退款订单号。
     */
    String queryRefundOrderNum(String orderId);


    /**
     * 查询店铺的退货退款现金金额。
     *
     * @param pojo 销售统计DTO
     * @return 退货退款现金金额
     */
    BigDecimal querShopReturnCash(ShopSalesStatisticsDTO pojo);
}
