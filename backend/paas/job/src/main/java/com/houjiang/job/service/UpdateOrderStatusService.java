package com.houjiang.job.service;

import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.job.dto.AppJiPushOrderDTO;
import com.houjiang.job.dto.OrderStatusDTO;

import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/5/17 14:29
 */
public interface UpdateOrderStatusService {

    /**
     * 获取待更新状态订单信息
     * @return
     */
    List<AppJiPushOrderDTO> listForUpdateOrder(Integer automaticDeliveryTime);

    /**
     * 更新所有超时未确认收货订单相关记录
     * @param orderId
     * @return
     */
    Integer updateForUpdateOrder(String orderId);

    /**
     * 更新所有待超时订单
     * @param orderId
     * @return
     */
    Integer updateForUpdateGoods(String orderId);

    /**
     * 查询超过n分钟额订单
     * @param minutes
     * @return
     */
    List<String> listTimeoutOrder2(String minutes);

    /**
     * 根据订单Id查询 商品 数量 Id
     * @param orderId
     * @return
     */
    List<WapGoodsSaleSumDTO> queryGoodsXinXi(String orderId);

    /**
     * 查询商品销量
     * @param goodsId
     * @return
     */
    Map<String, Long> queryGoodsCommonStock(Integer goodsId);

    /**
     * 更新商品库存
     * @param param
     * @return
     */
    Integer updateCommonStockByGoodsId(WapGoodsSaleSumDTO param);

    /**
     * 更新商品库存
     * @param param
     * @return
     */
    Integer updateGoodsStockByGoodsId(WapGoodsSaleSumDTO param);

    /**
     * 返回红包
     * @param minutes
     * @return
     */
    List<OrderStatusDTO> listTimeoutOrderDetail2(String minutes);


    /**
     * 查询订单状态及退货退款状态
     * @param paymentNumber
     * @return
     */
    List<Integer> queryOrderStatus(String paymentNumber);

    /**
     * 更新超过24小时未支付订单
     * @param minutes
     * @return
     */
    Integer updateTimeoutOrder2(String minutes);

    /**
     * 更新所有超时未支付订单
     * @param orderId
     * @return
     */
    Integer updateTimeoutOrderGoods(String orderId);

    /**
     * 更新所有超时未支付订单关联的审批
     * @param orderId
     * @return
     */
    Integer editApprovalId(String orderId);


    /**
     * 查询超出48小时商家未处理售后退款订单
     * @return
     */
    void refundOrderHandleJob();

    /**
     * 过期未支付的plus 预支付订单
     */
    void deleteTimeoutPlusOrder(String minutes);

    /**
     * TODO 补充方法说明
     * @param orderIds TODO
     */
    void updateFxCommissionOrder(List<AppJiPushOrderDTO> orderIds);

    /**
     * 付款凭证审核改为 审核驳回
     * @param orderId
     */
    void cancelOrderUpdatePaymentVocher(String orderId);
}
