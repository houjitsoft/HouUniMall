package com.houjiang.job.mapper;

import com.houjiang.commons.dto.order.AddInventoryDTO;
import com.houjiang.commons.dto.order.CancelParaEntityDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.vo.goods.WapGoodsSaleSumUpdateVO;
import com.houjiang.job.dto.AppJiPushOrderDTO;
import com.houjiang.job.dto.BusinessOvertimeOrderDTO;
import com.houjiang.job.dto.OrderStatusDTO;
import com.houjiang.job.dto.RefundParamTwoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @ClassName UpdateOrderStatusMapper
 * @Description 订单状态
 * @Author Amos
 * @Date 2023/5/17 17:12
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface UpdateOrderStatusMapper{

    /**
     * 获取待更新状态订单信息
     * @return
     */
    List<AppJiPushOrderDTO> listForUpdateOrder(@Param("automaticDeliveryTime") Integer automaticDeliveryTime);

    /**
     * 更新所有超时未确认收货订单相关记录
     * @param orderId
     * @return
     */
    Integer updateForUpdateOrder(@Param("orderId") String orderId, @Param("orderLastReturnTime") Date orderLastReturnTime);

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
    List<String> listTimeoutOrder2(@Param("minutes") String minutes);

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
    Map<String, Long> queryGoodsCommonStock(@Param("goodsId") Integer goodsId);

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
    List<OrderStatusDTO> listTimeoutOrderDetail2(@Param("minutes") String minutes);

    /**
     * 查询订单状态及退货退款状态
     * @param paymentNumber
     * @return
     */
    List<Integer> queryOrderStatus(@Param("paymentNumber") String paymentNumber);

    /**
     * 更新超过24小时未支付订单
     * @param minutes
     * @return
     */
    Integer updateTimeoutOrder2(@Param("minutes") String minutes);

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
    List<BusinessOvertimeOrderDTO> queryReturnOrderNotHandle();

    /**
     * 查询超出24小时退款商家未同意订单
     *
     * @return
     */
    List<BusinessOvertimeOrderDTO> queryBusinessOvertimeOrder();

    /**
     * 查询支付数量
     *
     * @param param
     * @return
     */
    String queryPaymentNumber(BusinessOvertimeOrderDTO param);


    /**
     * 查询退款所需参数
     * @param orderId
     * @return
     */
    RefundParamTwoDTO queryRefundPara(@Param("orderId") String orderId);


    /**
     * 更新订单表状态
     * @param param
     * @return
     */
    Integer updateBusinessOvertimeOrderBase(BusinessOvertimeOrderDTO param);


    /**
     * 更新退款
    /**
     * 更新返回订单
     *
     * @param param
     * @return
     */
    Integer updateBusinessOvertimeOrderReturn(BusinessOvertimeOrderDTO param);

    /**
     * 查询超过48小时商家未处理的订单
     * @return
     */
    List<BusinessOvertimeOrderDTO> queryTuiHuoOrderNotHandle();

    /**
     *更新退款信息
/**
     * @param orderGoodsMap
     * @return
     */
    Integer updateOrderReturnById(@Param("param") Map<String, Object> orderGoodsMap);


    /**
     * 根据付单号查退款状态
     * @param uOrderId
     * @return
     */
    List<Integer> queryOrderRecordStatus(@Param("uOrderId") String uOrderId);

    /**
     * 查询待取消的未支付订单。
     * <p>
     * 该方法用于检索那些尚未支付且需要被取消的订单。返回一个字符串列表，每个字符串代表一个订单号。
     */
    List<String> queryUnpaidOrdersToBeCancelled();

    /**
     * 根据订单号查询商品详情。
     * <p>
     * 通过指定的订单号，查询与该订单相关商品的详细信息。返回一个包含商品详情的DTO列表。
     *
     * @param orderId 订单号，用于查询商品详情。
     * @return 商品详情列表，每个详情包含商品的库存信息等。
     */
    List<AddInventoryDTO> queryGoodsDetails(@Param("orderId") String orderId);

    /**
     * 更新商品销售汇总信息。
     * <p>
     * 该方法用于根据传入的WapGoodsSaleSumUpdateVO对象更新商品的销售汇总信息。
     *
     * @param wgssu 包含商品销售汇总更新信息的VO对象。
     */
    void updateSaleSum2(@Param("param") WapGoodsSaleSumUpdateVO wgssu);

    /**
     * 根据订单号和状态更新订单及其商品的状态。
     * <p>
     * 该方法用于更新指定订单的状态，包括订单的取消状态、原因和日期，同时更新相关商品的销售状态。
     *
     * @param orderId           订单号。
     * @param orderStatus       订单的新状态。
     * @param orderCancelIdentity 订单的取消身份标识。
     * @param orderCancelReason 订单被取消的原因。
     * @param orderCancelDate   订单被取消的日期。
     * @return 更新操作是否成功的布尔值。
     */
    boolean updateByOrderBase(@Param("orderId") String orderId,
                              @Param("orderStatus") Integer orderStatus,
                              @Param("orderCancelIdentity") Integer orderCancelIdentity,
                              @Param("orderCancelReason") String orderCancelReason,
                              @Param("orderCancelDate") String orderCancelDate);

    /**
     * 根据订单号和状态更新订单商品的状态。
     * <p>
     * 该方法用于更新指定订单下商品的状态，主要针对订单状态的变更。
     *
     * @param orderId    订单号。
     * @param orderStatus 订单的新状态。
     * @return 更新操作是否成功的布尔值。
     */
    boolean updateByOrderGoods(@Param("orderId") String orderId, @Param("orderStatus") Integer orderStatus);

    /**
     * 根据订单号和状态更新订单状态。
     * <p>
     * 该方法用于批量更新指定订单的状态，不涉及订单的其他详细信息变更。
     *
     * @param status 新的订单状态。
     * @param orderId 订单号。
     */
    void updateOrderStatus(@Param("orderStatus") Integer status, @Param("orderId") String orderId);

    /**
     * 根据订单号查询退款编号和红包ID。
     * <p>
     * 该方法用于在订单取消或退款时，查询相关的退款编号和红包ID，以便进行后续的退款操作。
     *
     * @param orderId 订单号。
     * @return 包含退款编号和红包ID的DTO对象。
     */
    CancelParaEntityDTO queryPaymentNumberAndRedpacketId(@Param("orderId") String orderId);

    /**
     * 删除过期的加购订单。
     * <p>
     * 该方法用于定期清理那些超过指定时间未支付的加购订单，以释放库存和保持数据清洁。
     *
     * @param minutes 加购订单的过期时间，单位为分钟。
     */
    void deleteTimeoutPlusOrder(@Param("minutes") String minutes);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> selectFxCommissionOrderId(@Param("orderIds") List<String> orderIds);

    /**
     * TODO 补充方法说明
     */
    void updateFxCommissionOrder(@Param("orderIdList") List<String> orderIdList);

    /**
     * 付款凭证审核改为 审核驳回
     * @param orderId
     */
    void cancelOrderUpdatePaymentVocher(@Param("orderId")String orderId);
}
