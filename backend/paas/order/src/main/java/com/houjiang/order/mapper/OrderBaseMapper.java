package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.base.BaseExpressDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.dto.goods.SynchronousGoodsDTO;
import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.order.AddInventoryDTO;
import com.houjiang.commons.dto.order.PlatFormMemberSelfQueryDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.payment.AssetTmpInfoDTO;
import com.houjiang.commons.dto.payment.RefundAccountParamDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.BaseCurrencySymbolVO;
import com.houjiang.commons.vo.base.BaseDistrictVO;
import com.houjiang.commons.vo.base.KdniaoExpressVO;
import com.houjiang.commons.vo.fx.ShareGoodsVO;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.goods.PurchaseOrderVO;
import com.houjiang.commons.vo.goods.WapGoodsSaleSumUpdateVO;
import com.houjiang.commons.vo.job.AcquiescenceVO;
import com.houjiang.commons.vo.order.*;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.shop.AppShopBaseSalesInfoVO;
import com.houjiang.commons.vo.shop.ShopShippingAddressVO;
import com.houjiang.commons.vo.shop.ShopdpexpressVO;
import com.houjiang.commons.vo.shop.ShopkdniaoVO;
import com.houjiang.commons.vo.user.UserInfoOrderDetailVO;
import com.houjiang.commons.vo.yiyatong.base.SubmitOrderVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderBaseMapper
 * @Description 订单详细信息
 * @Author Amos
 * @Date 2023-05-31 11:56:58
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderBaseMapper extends BaseMapper<OrderBaseEntity> {


    /**
     * 新增供应商订单信息
     *
     * @param respData
     */
    void insertSupperOrderBase(@Param("respData") Map<String, Object> respData);

    /**
     * 查询购买积分商品所需积分
     *
     * @param goodsId
     * @return
     */
    Integer getGoodsIntegral(Integer goodsId);

    /**
     * 新增订单信息
     *
     * @param orderId
     * @param shopId
     * @param orderGateShopId
     * @param shopName
     * @param userId
     * @param userName
     * @param sellerId
     * @param sellerUserName
     * @param orderDate
     * @param orderGoodsTime
     * @param orderReceiverName
     * @param orderReceiverAddress
     * @param orderReceiverContact
     * @param orderReceiverProvinceId
     * @param orderReceiverCityId
     * @param orderReceiverAreaId
     * @param paymentNumber
     * @param invoiceName
     * @param invoiceId
     * @param shopPrice
     * @param orderBasePaymentAmount
     * @param orderDiscountFee
     * @param shippingFee
     * @param orderBuyerEvaluationStatusInteger
     * @param buyerMessage
     * @param orderStatus
     * @param orderDiscountType
     * @param couponPrice
     * @param voucherId
     * @param orderFromNum
     * @param redpacketId
     * @param redPacketPrice
     * @param redPacketSumMoney
     * @param platformVoucherCode
     * @param platformVoucherPrice
     * @param platformVoucherShopPrice
     * @param districtId
     * @param parentShopId
     * @param inviteSubsiteId
     * @param treasureOrderShopId
     * @param orderPromotionId
     * @param groupbuyStatus
     * @param orderBuyId
     * @param smallOrder
     * @param liveId
     * @param accountId
     * @param virtualGoods
     * @param periodOfValidity
     * @param pastDue
     * @param shopDistributorId
     * @param orderType
     * @param deliverShopId
     * @param deliverShopName
     * @param orderSupplyPrice
     * @param shareUserId
     * @param integralGoods
     * @param totalIntegral
     * @param orderIsShopTake
     * @param dropShipping
     * @param dealerCommission
     * @param supplyCommission
     * @param payMentCode
     * @param manjianDiscount
     * @param manzheDiscount
     * @param giveawayInfo
     * @param orderBaseCommission
     * @param baseMemberDiscount
     * @param plusOrder
     * @return
     */
    boolean insertByOrderBase(@Param("orderId") String orderId, @Param("shopId") Integer shopId, @Param("orderGateShopId") Integer orderGateShopId,
                              @Param("shopName") String shopName, @Param("userId") String userId, @Param("userName") String userName,
                              @Param("sellerId") String sellerId, @Param("sellerUserName") String sellerUserName,
                              @Param("orderDate") String orderDate, @Param("orderGoodsTime") String orderGoodsTime,
                              @Param("orderReceiverName") String orderReceiverName,
                              @Param("orderReceiverAddress") String orderReceiverAddress,
                              @Param("orderReceiverContact") String orderReceiverContact,
                              @Param("orderReceiverProvinceId") Integer orderReceiverProvinceId,
                              @Param("orderReceiverCityId") Integer orderReceiverCityId,
                              @Param("orderReceiverAreaId") Integer orderReceiverAreaId, @Param("paymentNumber") String paymentNumber,
                              @Param("invoiceName") String invoiceName, @Param("invoiceId") Integer invoiceId,
                              @Param("shopPrice") BigDecimal shopPrice,
                              @Param("orderBasePaymentAmount") BigDecimal orderBasePaymentAmount,
                              @Param("orderDiscountFee") BigDecimal orderDiscountFee, @Param("shippingFee") BigDecimal shippingFee,
                              @Param("orderBuyerEvaluationStatusInteger") Integer orderBuyerEvaluationStatusInteger,
                              @Param("buyerMessage") String buyerMessage, @Param("orderStatus") Integer orderStatus,
                              @Param("orderDiscountType") Integer orderDiscountType, @Param("couponPrice") BigDecimal couponPrice,
                              @Param("voucherId") String voucherId, @Param("orderFromNum") Integer orderFromNum,
                              @Param("redpacketId") String redpacketId, @Param("redPacketPrice") BigDecimal redPacketPrice,
                              @Param("redPacketSumMoney") BigDecimal redPacketSumMoney, @Param("platformVoucherCode") String platformVoucherCode, @Param("platformVoucherPrice") BigDecimal platformVoucherPrice, @Param("platformVoucherShopPrice") BigDecimal platformVoucherShopPrice, @Param("districtId") Integer districtId, @Param("parentShopId") Integer parentShopId,
                              @Param("inviteSubsiteId") Integer inviteSubsiteId, @Param("treasureOrderShopId") Integer treasureOrderShopId, @Param("orderPromotionId") Integer orderPromotionId, @Param("groupbuyStatus") Integer groupbuyStatus, @Param("orderBuyId") String orderBuyId, @Param("smallOrder") Integer smallOrder, @Param("liveId") String liveId, @Param("accountId") String accountId,
                              @Param("virtualGoods") Integer virtualGoods, @Param("periodOfValidity") String periodOfValidity,
                              @Param("pastDue") Integer pastDue, @Param("shopDistributorId") Integer shopDistributorId, @Param("orderType") Integer orderType, @Param("deliverShopId") Integer deliverShopId, @Param("deliverShopName") String deliverShopName, @Param("orderSupplyPrice") BigDecimal orderSupplyPrice, @Param("shareUserId") String shareUserId, @Param("integralGoods") Integer integralGoods, @Param("totalIntegral") Integer totalIntegral, @Param("orderIsShopTake") Integer orderIsShopTake, @Param("dropShipping") Integer dropShipping, @Param("dealerCommission") BigDecimal dealerCommission, @Param("supplyCommission") BigDecimal supplyCommission,
                              @Param("payMentCode") Integer payMentCode, @Param("manjianDiscount") BigDecimal manjianDiscount, @Param("manzheDiscount") BigDecimal manzheDiscount, @Param("giveawayInfo") String giveawayInfo, @Param("orderBaseCommission") BigDecimal orderBaseCommission, @Param("baseMemberDiscount") BigDecimal baseMemberDiscount, @Param("plusOrder") Integer plusOrder);

    /**
     * 查询店铺发票开通状态
     *
     * @param shopId
     * @return
     */
    Integer checkInvoiceSwitchStatus(Integer shopId);

    /**
     * 更新秒杀订单取消时间
     *
     * @param map
     */
    void updateSeckillCancelTime(@Param("param") Map<String, Object> map);

    /**
     * 修改拼团订单状态
     *
     * @param orderIdList
     * @return
     */
    boolean updateGroupBookingBaseStatus(@Param("orderIdList") List<String> orderIdList);

    /**
     * 查询支付单号
     *
     * @param orderId
     * @return
     */
    String queryPayNumber(@Param("orderId") String orderId);

    /**
     * 拼团超时，自动取消
     *
     * @param orderId
     * @param cancelTime
     * @return
     */
    boolean updateGroupBookingBaseOrderStatus(@Param("orderId") String orderId, @Param("cancelTime") String cancelTime);

    /**
     * 取消订单
     *
     * @param payNumber
     * @return
     */
    boolean updateGroupBookingGoodsOrderStatus(String payNumber);

    /**
     * 查询订单 用户信息 及分享人用户信息
     *
     * @param orderIdList
     * @return
     */
    List<ShareGoodsVO> queryFxOrderList(@Param("orderIdList") List<String> orderIdList);

    /**
     * 更新订单最晚售后时间
     *
     * @param orderId
     * @param returnTime
     */
    void updateOrderAfterSaleTime(@Param("orderId") String orderId, @Param("returnTime") String returnTime);

    /**
     * 根据支付单号查询
     *
     * @param paymentNumber
     * @return
     */
    PayDetailsVO querypaymentAmount(@Param("paymentNumber") String paymentNumber);

    /**
     * 根据订单号查询
     *
     * @param paymentNumber
     * @return
     */
    PayDetailsVO querypaymentByOrder(@Param("paymentNumber") String paymentNumber);

    /**
     * 查询订单创建时间及订单状态
     *
     * @param orderId
     * @return
     */
    OrderListVO queryOrderCreateTime(@Param("orderId") String orderId);

    /**
     * 查询订单商品信息
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsListVO> queryGoodsList(@Param("orderId") String orderId);

    /**
     * 判断商品是否参与秒杀
     *
     * @param goodsId
     * @return
     */
    Integer queryCountForSeckillGoods(@Param("goodsId") Integer goodsId);

    /**
     * 查询取消时间
     *
     * @param seckillId
     * @return
     */
    Integer queryCancelTime(@Param("seckillId") Integer seckillId);

    /**
     * 查询用户积分商品下单数量
     *
     * @param userId
     * @return
     */
    Integer queryintegraOrdersCount(String userId);


    /**
     * 查询商品数量
     *
     * @param orderId
     * @return
     */
    Integer queryGoodNumByOrderId(@Param("orderId") String orderId);

    /**
     * 查询订单数量
     *
     * @param userId
     * @return
     */
    MessageRemindNumVO selectOrderBaseNum(String userId);

    /**
     * 查询返回订单数量
     *
     * @param userId
     * @return
     */
    Integer selectOrderReturnNum(String userId);

    /**
     * 查询商品详情
     *
     * @param orderId
     * @return
     */
    List<AddInventoryDTO> queryGoodsDetails(@Param("orderId") String orderId);

    /**
     * 更新订单商品
     *
     * @param orderId
     * @param orderStatus
     * @return
     */
    boolean updateByOrderGoodsForDefaultFinish(@Param("orderId") String orderId,
                                               @Param("orderStatus") Integer orderStatus);

    /**
     * 更新订单
     *
     * @param orderId
     * @param orderStatus
     * @return
     */
    boolean updateByOrderBaseForDefaultFinish(@Param("orderId") String orderId,
                                              @Param("orderStatus") Integer orderStatus);


    /**
     * 更新售后时间
     *
     * @param orderId
     * @param orderLastReturnTime
     */
    void updateByOrderBaseorderLastReturnTime(String orderId, Date orderLastReturnTime);

    /**
     * 更新延迟时间
     *
     * @param userId
     * @param orderId
     * @return
     */
    boolean updateDelayTime(@Param("userId") String userId, @Param("orderId") String orderId, @Param("orderReceiverDate") Date orderReceiverDate);

    /**
     * 查询订单详细信息
     *
     * @param userId
     * @param orderId
     * @return
     */
    SoldOrderByCommonalityDetailsVO soldOrderByCommonalityDetails(@Param("userId") String userId,
                                                                  @Param("orderId") String orderId);

    /**
     * 查询是否改价
     *
     * @param map
     * @return
     */
    Integer queryOrderUpdatePrice(Map<String, Object> map);

    /**
     * 查询订单商品详情
     *
     * @param userId
     * @param orderId
     * @return
     */
    List<SoldOrderByGoodsDetailsVO> soldOrderByGoodsDetails(@Param("userId") String userId,
                                                            @Param("orderId") String orderId);

    /**
     * 获取基础订单
     *
     * @param orderId
     * @return
     */
    List<OrderBaseVO> getOrderBaseById(@Param("orderId") String orderId);

    /**
     * 获取供应商商品Id
     *
     * @param orderId
     * @param goodsId
     * @return
     */
    Integer queryCommonId(@Param("orderId") String orderId, @Param("goodsId") String goodsId);

    /**
     * 待付款
     *
     * @param shopId
     * @return
     */
    Integer WaitingForPaymentTotal(@Param("shopGateId") Integer shopGateId, @Param("shopId") Integer shopId, @Param("orderSource") Integer orderSource);

    /**
     * 待发货
     *
     * @param shopId
     * @return
     */
    Integer WaitForTheDeliveryTotal(@Param("shopGateId") Integer shopGateId,@Param("shopId") Integer shopId, @Param("orderSource") Integer orderSource);

    /**
     * 已发货
     *
     * @param shopId
     * @return
     */
    Integer shippedTotal(@Param("shopGateId") Integer shopGateId,@Param("shopId") Integer shopId, @Param("orderSource") Integer orderSource);

    /**
     * 已完成
     *
     * @param shopId
     * @return
     */
    Integer OffTheStocksTotal(@Param("shopGateId") Integer shopGateId,@Param("shopId") Integer shopId, @Param("orderSource") Integer orderSource);

    /**
     * 退货
     *
     * @param shopId
     * @return
     */
    Integer SalesReturnTotal(@Param("shopGateId") Integer shopGateId,@Param("shopId") Integer shopId, @Param("orderSource") Integer orderSource);

    /**
     * 退款订单
     *
     * @param shopId
     * @return
     */
    Integer RefundTotal2(@Param("shopGateId") Integer shopGateId,@Param("shopId") Integer shopId, @Param("orderSource") Integer orderSource);

    /**
     * 退款订单
     *
     * @param shopId
     * @return
     */
    Integer evaluateOrder(@Param("shopGateId") Integer shopGateId,@Param("shopId") Integer shopId);

    /**
     * 销量排名
     *
     * @param shopId
     * @return
     */
    List<SalesRankingVO> querySalesRanking(@Param("shopGateId") Integer shopGateId,@Param("shopId") Integer shopId);


    /**
     * 查询每日交易额及订单量
     *
     * @param shopId
     * @return
     */
    SaleRoomVO querySaleByDay(@Param("shopId") Integer shopId, @Param("shopType") Integer shopType);

    /**
     * 查询每日营销额
     *
     * @param shopId
     * @return
     */
    BigDecimal querysalesVolumeByDay(@Param("shopId") Integer shopId, @Param("shopType") Integer shopType);

    /**
     * 查询每周交易额及订单量
     *
     * @param shopId
     * @return
     */
    SaleRoomVO querySaleByWeek(@Param("shopId") Integer shopId, @Param("shopType") Integer shopType);

    /**
     * 查询每周营销额
     *
     * @param shopId
     * @return
     */
    BigDecimal querySalesVolumeByWeek(@Param("shopId") Integer shopId, @Param("shopType") Integer shopType);

    /**
     * 查询每月交易额及订单量
     *
     * @param shopId
     * @return
     */
    SaleRoomVO querySaleByMonth(@Param("shopId") Integer shopId, @Param("shopType") Integer shopType);

    /**
     * 查询每月营销额
     *
     * @param shopId
     * @return
     */
    BigDecimal querySalesVolumeByMonth(@Param("shopId") Integer shopId, @Param("shopType") Integer shopType);


    /**
     * 查询待评价虚拟订单商品信息
     *
     * @param page
     * @param mapOrder
     * @return
     */
    IPage<OrderListVO> queryWaitingEvaluationVirtualOrder(IPage<OrderListVO> page, @Param("param") Map<String, Object> mapOrder);

    /**
     * 查询待评价非虚拟订单商品信息
     *
     * @param page
     * @param mapOrder
     * @return
     */
    IPage<OrderListVO> queryWaitingEvaluationOrder(IPage<OrderListVO> page, @Param("param") Map<String, Object> mapOrder);

    /**
     * 获取商品状态
     *
     * @param commonId
     * @return
     */
    int selCommonStateBySPU(@Param("commonId") Integer commonId);


    /**
     * 查询待评价订单商品信息
     *
     * @param page
     * @param mapOrder
     * @return
     */
    IPage<OrderListVO> queryIntegralWaitingEvaluationOrder(IPage<OrderListVO> page, Map<String, Object> mapOrder);

    /**
     * 查询业务
     *
     * @param userId
     * @param businessId
     * @return
     */
    Map<String, Object> queryBusiness(@Param("userId") String userId, @Param("businessId") String businessId);


    /**
     * 查询员工列表
     *
     * @param businessId
     * @return
     */
    List<String> queryEmployeesList(@Param("businessId") String businessId);

    /**
     * 查询订单商品信息
     *
     * @param page
     * @param param
     * @return
     */
    IPage<OrderBaseEntity> queryOrderGoodsInfo(IPage<OrderBaseEntity> page, @Param("param") Map<String, Object> param);

    /**
     * 获取发票交换信息
     *
     * @param shopId
     * @return
     */
    Map<String, Object> getInvoiceSwitchInfo(@Param("shopId") Integer shopId);

    /**
     * 查询交易 订单明细列表
     */
    IPage<SellingOrderVO> selectSellingOrderNew(IPage<SellingOrderVO> page, @Param("param") SellingInputDTO param);

    /**
     * 查询交易 订单明细 订单总金额
     */
    BigDecimal querySellingOrderMoneynNew(@Param("param") SellingInputDTO param);

    /**
     * 查询交易 订单明细列表导出
     */
    List<SellingOrderVO> orderSellingDaoTransactionExp(@Param("param") SellingInputDTO param);

    /**
     * 查询订单信息
     */
    OrderbaseDetailsparaVO queryorderDetailparam(@Param("orderId") String orderId);

    /**
     * 查询订单商品信息
     */
    List<GoodsAllByidVO> getGoodsAllByIdparam(@Param("orderId") String orderId);

    /**
     * 获取支付凭证信息
     */
    OdbPaymentVocherVO queryPaymentVocher(@Param("orderId") String orderId);

    /**
     * 查询卖出交易订单信息
     */
    IPage<SellingOrderVO> selectSellingOrder(IPage<SellingOrderVO> page, @Param("param") SellingInputDTO param);

    /**
     * 查询卖出交易订单总金额
     */
    BigDecimal querySellingOrderMoney(@Param("param") SellingInputDTO param);


    /**
     * 更新基础订单
     *
     * @param param
     * @return
     */
    Integer updateOrderBaseById(@Param("param") Map<String, Object> param);


    /**
     * 更新订单基本返回方法状态
     *
     * @param returnMethodStatus
     * @param orderId
     * @return
     */
    Integer updateOrderBaseReturnMethodStatus(@Param("returnMethodStatus") Integer returnMethodStatus, @Param("orderId") String orderId);

    /**
     * 判断该订单是否是虚拟订单或者是门店自提订单
     */
    Map<String, Object> queryisVirtualOrder(@Param("orderId") String orderId, @Param("isNum") Integer isNum);

    /**
     * 为虚拟订单添加兑换码
     */
    Integer addRedeemCode(@Param("param") Map<String, Object> param);

    /**
     * 查询下单所需参数
     */
    OrderToInformParaVO queryBaseParaByInt(@Param("payNumber") String payNumber);

    /**
     * 插入系统消息
     */
    boolean insertSysMessage(@Param("orderId") String orderId, @Param("sellerId") String sellerId, @Param("sellerName") String sellerName,
                             @Param("messageType") Integer messageType, @Param("messageTitle") String messageTitle,
                             @Param("messageCreateTime") String messageCreateTime, @Param("messageContent") String messageContent, @Param("shopId") String shopId);

    /**
     * 查询下单所需参数
     */
    List<OrderToInformParaVO> queryBasePara(@Param("payNumber") String payNumber);

    /**
     * 获取支付单号
     */
    String queryUpayByOrderId(@Param("order_id") String order_id);

    /**
     * 更改状态
     */
    boolean updateOrderBaseByPaymentByPureDigital(@Param("orderStatus") Integer orderStatus, @Param("paymentId") Integer paymentId,
                                                  @Param("paymentName") String paymentName, @Param("paymentTime") String paymentTime,
                                                  @Param("paymentNumbers") String paymentNumbers, @Param("payCode") Integer payCode);

    /**
     * 更新OrderBase支付等字段
     */
    boolean updateOrderBaseByPayment(@Param("orderStatus") Integer orderStatus, @Param("payCode") Integer payCode, @Param("paymentId") Integer paymentId,
                                     @Param("paymentName") String paymentName, @Param("paymentTime") String paymentTime,
                                     @Param("paymentNumbers") String paymentNumbers);

    /**
     * 根据支付号查询实际支付单号
     */
    List<String> queryActualOrderId(@Param("paymentNumbers") String paymentNumbers);

    /**
     * 更新支付凭证
     *
     * @param param
     * @return
     */
    Integer updatePaymentVocher(QukVocherImagesDTO param);

    /**
     * 订单之线下订单 导出
     *
     * @param mapobject
     * @return
     */
    List<SellingOrderVO> PlatFormOfflineOrderEXcel(@Param("param") Map<Object, Object> mapobject);

    /**
     * 订单之卖出交易
     *
     * @param page
     * @param param
     * @return
     */
    IPage<SellingOrderVO> selectSellingOrderNew2(IPage<SellingOrderVO> page, @Param("param") SellingInputNewDTO param);

    /**
     * 订单之卖出交易 导出
     *
     * @param param
     */
    List<SellingOrderVO> selectSellingOrderNewEXcel(@Param("param") SellingInputNewDTO param);


    /**
     * 查询商品总价格
     *
     * @param param
     * @return
     */
    String querySellingOrderMoneynNew2(@Param("param") SellingInputNewDTO param);

    /**
     * 查询卖家中心订单发票列表
     *
     * @param param
     * @return
     */
    IPage<SellerOrderInvoiceVO> querySellerOrderInvoiceInfo(IPage<SellerOrderInvoiceVO> page, @Param("param") Map<String, Object> param);

    /**
     * 查询卖家中心订单发票列表总数
     *
     * @param param
     * @return
     */
    Integer querySellerOrderInvoiceInfoCount(@Param("param") Map<Object, Object> param);

    /**
     * 查询商品信息
     *
     * @param param
     * @return
     */
    List<OrderGoodsDetailVO> queryOrderGoodsInfo2(@Param("param") Map<String, Object> param);

    /**
     * 查询发票类型
     *
     * @param param
     * @return
     */
    Integer getInvoiceType(@Param("param") Map<String, Object> param);


    /**
     * 查询基础订单
     *
     * @param page
     * @param param
     * @return
     */
    IPage<OrderDetailsVO> queryOrderBase(IPage<OrderDetailsVO> page, @Param("param") Map<String, Object> param);


    /**
     * 查询所有订单基础信息
     * <p>
     * 本函数旨在实现查询所有订单的基础信息，支持通过分页方式获取数据。
     * 使用IPage作为返回类型，实现了分页查询的功能，可以有效地减少内存占用，提高查询效率。
     * 参数map用于传递查询条件，通过@Param注解指定参数名，提高了查询的灵活性和扩展性。
     *
     * @param page 分页对象，包含当前页码和每页的记录数等信息，用于进行分页查询。
     * @param map  查询参数的映射表，可以通过键值对的方式传递多种查询条件，提高了函数的灵活性。
     * @return 返回分页后的订单基础信息，包含了当前页的订单列表和分页信息。
     */
    IPage<OrderDetailsVO> queryAllOrderBase(IPage<OrderDetailsVO> page, @Param("param") Map<String, Object> map);

    /**
     * 商家查询订单
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsVO> queryOrders(@Param("orderId") String orderId);

    /**
     * 伪删除
     *
     * @param orderId
     * @return
     */
    boolean pseudoDelete(@Param("orderId") String orderId);


    /**
     * 查询供应商订单信息
     *
     * @param page
     * @param baseMap
     * @return
     */
    IPage<OrderBaseVO> listSupplyBaseInfo(IPage<OrderBaseVO> page, @Param("param") Map<String, Object> baseMap);


    /**
     * 查询经销商订单信息
     *
     * @param page
     * @param baseMap
     * @return
     */
    IPage<OrderBaseVO> listDealerBaseInfo(IPage<OrderBaseVO> page, @Param("baseMap") Map<String, Object> baseMap);


    /**
     * 查询订单信息
     *
     * @param page
     * @param baseMap
     * @return
     */
    IPage<OrderBaseVO> listBaseInfo(IPage<OrderBaseVO> page, @Param("baseMap") Map<String, Object> baseMap);

    /**
     * 查询当日交易额
     *
     * @return
     */
    BigDecimal queryTodaySales();

    /**
     * 查询全部交易额
     *
     * @return
     */
    BigDecimal queryAllSales();

    /**
     * 查询本周交易额
     *
     * @return
     */
    BigDecimal queryWeekSales();

    /**
     * 查询本月交易额
     *
     * @return
     */
    BigDecimal queryMonthSales();

    /**
     * 查询年度交易额
     *
     * @return
     */
    BigDecimal queryYearSales();

    /**
     * 查询店铺全部排行
     *
     * @return
     */
    List<StoreParaVO> queryAllStore();

    /**
     * 查询店铺本日排行
     *
     * @return
     */
    List<StoreParaVO> queryTodayStore();

    /**
     * 查询店铺本周排行
     *
     * @return
     */
    List<StoreParaVO> queryWeekStore();

    /**
     * 查询店铺本月排行
     *
     * @return
     */
    List<StoreParaVO> queryMonthStore();

    /**
     * 查询店铺本年排行
     *
     * @return
     */
    List<StoreParaVO> queryYearStore();


    /**
     * 获取订单详情
     *
     * @param orderNum
     * @return
     */
    MerchantsOrdersDetailsVO queryByOrderDetails(@Param("orderNum") String orderNum);

    /**
     * 查询商家后台订单详情下方订单详情
     *
     * @param orderNum
     * @param shopId
     * @return
     */
    List<AddOrderDetailsVO> queryNewOrderDetails(@Param("orderNum") String orderNum, @Param("shopId") String shopId);

    /**
     * 查询分佣比例
     *
     * @param goodsId
     * @return
     */
    BigDecimal queryCatChouYong(@Param("goodsId") Integer goodsId);

    /**
     * 查询退货退款
     *
     * @param orderNum
     * @return
     */
    List<ReturnOrderDetailsVO> queryReturnOrderDetails(@Param("orderNum") String orderNum);

    /**
     * 更新基础
     *
     * @param mapDigital
     * @return
     */
    boolean updateBaseByPureDigital(Map<String, Object> mapDigital);

    /**
     * 订单基准更新为付款
     *
     * @param mapDigital
     * @return
     */
    boolean fiveUpdateOrderBaseToPaymentByPureDigital(Map<String, Object> mapDigital);

    /**
     * 订单商品
     *
     * @param orderStatus
     * @param paymentNumber
     * @return
     */
    boolean fiveUpdateOrderGoodsToGoodsStatusByPureDigital(@Param("orderStatus") Integer orderStatus,
                                                           @Param("paymentNumber") String paymentNumber);

    /**
     * 更新基础付款
     *
     * @param mapDigital
     * @return
     */
    boolean updateBaseToPayment(Map<String, Object> mapDigital);

    /**
     * 更新订单基础付款
     *
     * @param mapDigital
     * @return
     */
    boolean fiveUpdateOrderBaseToPayment(Map<String, Object> mapDigital);

    /**
     * 查询订单
     *
     * @param paymentNumber
     * @return
     */
    List<String> queryOrderIdForPayNumber(@Param("paymentNumber") String paymentNumber);

    /**
     * 订单商品和商品状态
     *
     * @param orderStatus
     * @param orderNumber
     * @return
     */
    boolean fiveUpdateOrderGoodsToGoodsStatus(@Param("orderStatus") Integer orderStatus,
                                              @Param("orderNumber") String orderNumber);

    /**
     * 供应商 创建订单
     *
     * @param orderId
     * @param shopId
     * @param orderGateShopId
     * @param shopName
     * @param userId
     * @param userName
     * @param sellerId
     * @param sellerUserName
     * @param orderDate
     * @param orderGoodsTime
     * @param orderReceiverName
     * @param orderReceiverAddress
     * @param orderReceiverContact
     * @param orderReceiverProvinceId
     * @param orderReceiverCityId
     * @param orderReceiverAreaId
     * @param paymentNumber
     * @param invoiceName
     * @param invoiceId
     * @param shopPrice
     * @param orderBasePaymentAmount
     * @param orderDiscountFee
     * @param shippingFee
     * @param orderBuyerEvaluationStatusInteger
     * @param buyerMessage
     * @param orderStatus
     * @param orderDiscountType
     * @param couponPrice
     * @param voucherId
     * @param orderFromNum
     * @param redpacketId
     * @param redPacketPrice
     * @param redPacketSumMoney
     * @param platformVoucherCode
     * @param platformVoucherPrice
     * @param platformVoucherShopPrice
     * @param districtId
     * @param treasureOrderShopId
     * @param orderPromotionId
     * @param groupbuyStatus
     * @param orderBuyId
     * @param smallOrder
     * @param liveId
     * @param accountId
     * @param virtualGoods
     * @param periodOfValidity
     * @param pastDue
     * @param shopDistributorId
     * @param orderType
     * @param deliverShopId
     * @param deliverShopName
     * @param orderSupplyPrice
     * @param shareUserId
     * @param integralGoods
     * @param totalIntegral
     * @param orderIsShopTake
     * @param dropShipping
     * @param dealerCommission
     * @param supplyCommission
     * @param orderBaseCommission
     * @param baseMemberDiscount
     */
    void insertSupplierByOrderBase(@Param("orderId") String orderId, @Param("shopId") Integer shopId, @Param("orderGateShopId") Integer orderGateShopId,
                                   @Param("shopName") String shopName, @Param("userId") String userId, @Param("userName") String userName,
                                   @Param("sellerId") String sellerId, @Param("sellerUserName") String sellerUserName,
                                   @Param("orderDate") String orderDate, @Param("orderGoodsTime") String orderGoodsTime,
                                   @Param("orderReceiverName") String orderReceiverName,
                                   @Param("orderReceiverAddress") String orderReceiverAddress,
                                   @Param("orderReceiverContact") String orderReceiverContact,
                                   @Param("orderReceiverProvinceId") Integer orderReceiverProvinceId,
                                   @Param("orderReceiverCityId") Integer orderReceiverCityId,
                                   @Param("orderReceiverAreaId") Integer orderReceiverAreaId, @Param("paymentNumber") String paymentNumber,
                                   @Param("invoiceName") String invoiceName, @Param("invoiceId") Integer invoiceId,
                                   @Param("shopPrice") BigDecimal shopPrice,
                                   @Param("orderBasePaymentAmount") BigDecimal orderBasePaymentAmount,
                                   @Param("orderDiscountFee") BigDecimal orderDiscountFee, @Param("shippingFee") BigDecimal shippingFee,
                                   @Param("orderBuyerEvaluationStatusInteger") Integer orderBuyerEvaluationStatusInteger,
                                   @Param("buyerMessage") String buyerMessage, @Param("orderStatus") Integer orderStatus,
                                   @Param("orderDiscountType") Integer orderDiscountType, @Param("couponPrice") BigDecimal couponPrice,
                                   @Param("voucherId") String voucherId, @Param("orderFromNum") Integer orderFromNum,
                                   @Param("redpacketId") String redpacketId, @Param("redPacketPrice") BigDecimal redPacketPrice,
                                   @Param("redPacketSumMoney") BigDecimal redPacketSumMoney, @Param("platformVoucherCode") String platformVoucherCode, @Param("platformVoucherPrice") BigDecimal platformVoucherPrice, @Param("platformVoucherShopPrice") BigDecimal platformVoucherShopPrice, @Param("districtId") Integer districtId,
                                   @Param("treasureOrderShopId") Integer treasureOrderShopId, @Param("orderPromotionId") Integer orderPromotionId, @Param("groupbuyStatus") Integer groupbuyStatus, @Param("orderBuyId") String orderBuyId, @Param("smallOrder") Integer smallOrder, @Param("liveId") String liveId, @Param("accountId") String accountId,
                                   @Param("virtualGoods") Integer virtualGoods, @Param("periodOfValidity") String periodOfValidity,
                                   @Param("pastDue") Integer pastDue, @Param("shopDistributorId") Integer shopDistributorId, @Param("orderType") Integer orderType, @Param("deliverShopId") Integer deliverShopId, @Param("deliverShopName") String deliverShopName,
                                   @Param("orderSupplyPrice") BigDecimal orderSupplyPrice, @Param("shareUserId") Integer shareUserId, @Param("integralGoods") Integer integralGoods, @Param("totalIntegral") Integer totalIntegral, @Param("orderIsShopTake") Integer orderIsShopTake, @Param("dropShipping") Integer dropShipping, @Param("dealerCommission") BigDecimal dealerCommission,
                                   @Param("supplyCommission") BigDecimal supplyCommission, @Param("orderBaseCommission") BigDecimal orderBaseCommission, @Param("baseMemberDiscount") BigDecimal baseMemberDiscount, @Param("distributionDiscount") BigDecimal distributionDiscount,
                                   @Param("purchaseNumber") String purchaseNumber,@Param("originalOrder") String originalOrder,@Param("orderSource") Integer orderSource,@Param("paymentCode") Integer paymentCode);

    /**
     * 查询店铺id
     *
     * @param isNotshopId
     * @return
     */
    List<String> queryShopBudistrictAndshopIdNo(@Param("isNotshopId") String isNotshopId);

    /**
     * 查询订单交易信息
     *
     * @param page
     * @param param
     * @return
     */
    IPage<PlatFormMemberSelfQueryVO> queryPlatFormMemberSelfPard(IPage<PlatFormMemberSelfQueryVO> page, @Param("param") PlatFormMemberSelfQueryDTO param);


    /**
     * 查询订单商品总价
     *
     * @param param
     * @return
     */
    String queryMemberSelfMoney(PlatFormMemberSelfQueryDTO param);


    /**
     * 更新基础订单
     *
     * @param mapGoods
     * @return
     */
    boolean updateByOrderBase(Map<String, Object> mapGoods);

    /**
     * 物流消息
     *
     * @param page
     * @param map
     * @return
     */
    IPage<OrderListVO> selByOrderBase(IPage<OrderListVO> page, @Param("map") Map<String, Object> map);

    /**
     * 查询订单商品信息
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsListVO> queryGoodsList1(@Param("orderId") String orderId);


    /**
     * 分页查询月销售报告
     *
     * @param page
     * @param param
     * @return
     */
    IPage<MonthReportSalesVO> queryMonthsaleReport(IPage<MonthReportSalesVO> page, @Param("param") MonthReportDTO param);

    /**
     * 查询月销售报告导出
     *
     * @param param
     * @return
     */
    List<MonthReportSalesVO> queryMonthsaleReportExcel(@Param("param") MonthReportDTO param);


    /**
     * 查询商品详情
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsVO> queryGoodsDetail(@Param("orderId") String orderId);


    /**
     * 查询真实订单
     *
     * @param param
     * @return
     */
    List<PlatFormMemberSelfQueryVO> queryRealOrderSelfPardExccel(@Param("param") RealOrderSelfDTO param);

    /**
     * 平台财务订单 列表查询
     *
     * @param param
     * @return
     */
    IPage<PlatFormMemberSelfQueryVO> queryRealOrderSelfPard(IPage<PlatFormMemberSelfQueryVO> page, @Param("param") RealOrderSelfDTO param);

    /**
     * 更新基础订单
     *
     * @param map
     * @return
     */
    Integer updateOrderBaseByMap(@Param("param") Map<String, Object> map);

    /**
     * 未下架数量
     *
     * @param shopId
     * @return
     */
    Integer selectGoodsStatus(@Param("shopId") Integer shopId);


    /**
     * 未发货/未结算
     *
     * @param shopId
     * @return
     */
    Map<String, Object> setNoSendGoods(@Param("shopId") Integer shopId);

    /**
     * 查询Exchange列表
     *
     * @param page
     * @param param
     * @return
     */
    IPage<OrderBaseEntity> queryExchangeList(IPage<OrderBaseEntity> page, @Param("param") Map<String, Object> param);

    /**
     * 查询门店 自提订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<OrderListVO> queryGateOrdersList(IPage<OrderListVO> page, @Param("param") Map<String, Object> map);

    /**
     * 查询门店 配送订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<OrderListVO> queryGateDeliveryOrders(IPage<OrderListVO> page, @Param("param") Map<String, Object> map);

    /**
     * 查询订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<OrderListVO> queryOrderListByPhone(IPage<OrderListVO> page, @Param("param") Map<String, Object> map);

    /**
     * 分页查询虚拟订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<OrderDetailsVO> queryVirtualOrderBase(Page<OrderDetailsVO> page, @Param("param") Map<String, Object> map);
    /**
     * 查询虚拟订单 导出
     *
     * @param map
     * @return
     */
    List<OrderExportVO> queryVirtualOrderBaseExp( @Param("param") Map<String, Object> map);

    /**
     * 分页查询成员用户订单详细信息
     *
     * @param page
     * @param map
     * @return
     */
    IPage<UserInfoOrderDetailVO> queryMemberUserOrderDetail(IPage<UserInfoOrderDetailVO> page, @Param("param") Map<String, Object> map);

    /**
     * 分页查询店铺订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<GateShopOrdersVO> queryGateShopOrders(IPage<GateShopOrdersVO> page, @Param("param") Map<String, Object> map);

    /**
     * 根据 核销码查询 门店订单详情
     *
     * @param redeemCode
     * @param orderGateShopId
     * @return
     */
    Map<String, Object> queryGateShopCancelOrderDetails(@Param("redeemCode") String redeemCode, @Param("orderGateShopId") String orderGateShopId);

    /**
     * 根据核销码查看门店订单详情
     *
     * @param redeemCode
     * @param shopId
     * @return
     */
    OrderDetailsVO queryGateShopOrderDetails(@Param("redeemCode") String redeemCode, @Param("shopId") String shopId);

    /**
     * 根据核销码核销门店订单
     *
     * @param redeemCode
     * @param updateTime
     * @return
     */
    Integer updateGateShopOrderCancel(@Param("redeemCode") String redeemCode, @Param("updateTime") String updateTime,
                                      @Param("orderId") String orderId, @Param("shopId") String shopId);

    /**
     * 分页查询真实店铺订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<VirtualGateShopOrderVO> queryVirtualGateShopOrders(IPage<VirtualGateShopOrderVO> page, @Param("param") Map<String, Object> map);

    /**
     * 供应商  待发货订单
     *
     * @param page
     * @param paramMap
     * @return
     */
    IPage<ShipmentsByOrderBaseVO> queryShipmentsOrderBase(IPage<ShipmentsByOrderBaseVO> page, @Param("param") Map<String, Object> paramMap);

    /**
     * 供应商 已售经销，已售自销订单
     *
     * @param page
     * @param paramMap
     * @return
     */
    IPage<OrderDetailsVO> querySupplyOrderBase(IPage<OrderDetailsVO> page, @Param("param") Map<String, Object> paramMap);

    /**
     * 查询商家订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<OrderDetailsVO> queryGateShopTakeOrderBase(Page<OrderDetailsVO> page, @Param("param") Map<String, Object> map);
    /**
     * 查询商家订单 导出
     *
     * @param map
     * @return
     */
    List<OrderExportVO> queryGateShopTakeOrderBaseExp(@Param("param")Map<String, Object> map);

    /**
     * 根据卖家id 和核销码查询 订单详情
     *
     * @param redeemCode
     * @param userId
     * @return
     */
    Map<String, Object> queryCancelOrderDetails(@Param("redeemCode") String redeemCode, @Param("userId") String userId, @Param("shopId") String shopId);

    /**
     * 根据核销码查看虚拟订单详情
     *
     * @param redeemCode
     * @return
     */
    OrderDetailsVO queryOrderDetails(@Param("redeemCode") String redeemCode, @Param("shopId") String shopId);

    /**
     * 根据核销码核销订单
     *
     * @param redeemCode
     * @param updateTime
     * @return
     */
    Integer updateOrderCancel(@Param("redeemCode") String redeemCode, @Param("updateTime") String updateTime,
                              @Param("orderId") String orderId, @Param("shopId") String shopId);

    /**
     * 更改物流编号
     *
     * @param orderId
     * @param orderShippingMessage
     * @param orderShippingCode
     * @return
     */
    boolean updateChangeShipmentsByBase(@Param("orderId") String orderId, @Param("orderShippingCode") String orderShippingCode, @Param("orderShippingMessage") String orderShippingMessage);

    /**
     * 更改物流编号
     *
     * @param orderId
     * @param orderShippingCode
     * @param orderShippingMessage
     * @return
     */
    boolean updateChangeShipmentsByGoods(@Param("orderId") String orderId, @Param("orderShippingCode") String orderShippingCode, @Param("orderShippingMessage") String orderShippingMessage);

    /**
     * 查询收货人联系方式
     *
     * @param orderId
     * @return
     */
    String queryReceiverPhone(@Param("orderId") String orderId);

    /**
     * 更改物流编号
     *
     * @param orderId
     * @param orderShippingMessage
     * @param orderShippingCode
     * @return
     */
    boolean updateChangeAllShipmentsByBase(@Param("orderId") String orderId, @Param("orderShippingExpressId") Integer orderShippingExpressId, @Param("orderShippingCode") String orderShippingCode, @Param("orderShippingMessage") String orderShippingMessage);

    /**
     * 更改物流编号
     *
     * @param orderId
     * @param orderShippingCode
     * @param orderShippingMessage
     * @return
     */
    boolean updateChangeAllShipmentsByGoods(@Param("orderId") String orderId, @Param("orderShippingExpressId") Integer orderShippingExpressId, @Param("orderShippingCode") String orderShippingCode, @Param("orderShippingMessage") String orderShippingMessage);


    /**
     * 查询买家订单记录
     *
     * @param page
     * @param param
     * @return
     */
    IPage<UserInfoOrderDetailVO> queryUserorderInfoList(IPage<UserInfoOrderDetailVO> page, @Param("param") Map<String, Object> param);

    /**
     * 更新订单库分隔发货
     *
     * @param mapBase
     * @return
     */
    boolean updateSeparateShipmentsByOrderBase(Map<String, Object> mapBase);

    /**
     * 更新店铺订单系统消息
     *
     * @param shopId
     * @param orderId
     * @return
     */
    boolean updateOrderMessageProcessingStatus(@Param("shopId") String shopId,
                                               @Param("orderId") String orderId);

    /**
     * 变更地址信息
     *
     * @param mapInfo
     * @return
     */
    boolean updateAddressInformation(Map<String, Object> mapInfo);

    /**
     * 批量发货订单导出
     *
     * @param map
     * @return
     */
    List<ShipmentsOrderDetailDTO> batchShipmentsOrderExport(Map<String, Object> map);

    /**
     * 分页查询商店ID参数选择订单
     *
     * @param page
     * @param param
     * @return
     */
    IPage<ManageOrderbaseListVO> selectOrderbaseByShopIdParam(Page<ManageOrderbaseListVO> page,
                                                              @Param("param") RetrunQueryPrintDTO param);


    /**
     * 查询发货导出参数
     *
     * @param mapExport
     * @return
     */
    List<SoldOrderExportDTO> queryShippingExportParams(Map<String, Object> mapExport);

    /**
     * 回收站界面(商品详情)
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsVO> queryRecycleBin(@Param("orderId") String orderId);

    /**
     * 查询物流公司信息
     *
     * @param orderId
     * @return
     */
    List<LogisticsMessageVO> queryLogisticsMessage(@Param("orderId") String orderId);

    /**
     * 查询商品名与商品图
     *
     * @param goodsIds
     * @param orderId
     * @param shopId
     * @return
     */
    List<ShippingGoodsMessageVO> queryGoodsMessage(@Param("goodsIds") List<Integer> goodsIds, @Param("orderId") String orderId, @Param("shopId") Integer shopId);

    /**
     * 查询物流名称
     *
     * @param expressId
     * @return
     */
    String queryExpressNameById(@Param("expressId") Integer expressId);

    /**
     * 查询用户电话
     *
     * @param orderId
     * @return
     */
    Map<String, String> queryUserPhone2(@Param("orderId") String orderId);


    /**
     * 更新商店隐藏
     *
     * @param isShopHidden
     * @param orderId
     * @return
     */
    boolean updateShopHidden(@Param("isShopHidden") Integer isShopHidden, @Param("orderId") String orderId);

    /**
     * 分页查询基础订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<OrderDetailsVO> queryRecycleBinByOrderBase(IPage<OrderDetailsVO> page, @Param("map") Map<String, Object> map);


    /**
     * 查询订单快递信息
     *
     * @param virtualGoods
     * @param shopId
     * @param allOrderStatus
     * @param otherOrderStatus
     * @param userName
     * @param orderNum
     * @param goodsName
     * @param orderStartTime
     * @param orderEndTime
     * @return
     */
    List<OrderDetailsVO> queryRecycleBinByOrderBaseExcel(@Param("virtualGoods") Integer virtualGoods, @Param("shopId") Integer shopId,
                                                         @Param("allOrderStatus") String allOrderStatus, @Param("otherOrderStatus") Integer otherOrderStatus,
                                                         @Param("userName") String userName, @Param("orderNum") String orderNum,
                                                         @Param("goodsName") String goodsName, @Param("orderStartTime") String orderStartTime,
                                                         @Param("orderEndTime") String orderEndTime);

    /**
     * 查询订单价格
     *
     * @param orderId
     * @return
     */
    BigDecimal calculateTotalPrice(@Param("orderId") String orderId);

    /**
     * 查询goods表实付
     *
     * @param orderId
     * @return
     */
    BigDecimal queryGoodsPaymentPrice(@Param("orderId") String orderId, @Param("goodsId") Integer goodsId);

    /**
     * 查询改价价格
     *
     * @param orderId
     * @return
     */
    BigDecimal queryBeforeAdjustPrice(@Param("orderId") String orderId, @Param("goodsId") Integer goodsId);

    /**
     * 查询改价
     *
     * @param orderId
     * @return
     */
    BigDecimal queryOrderBaseBeforeAdjustPrice(String orderId);

    /**
     * 更改goods表实付金额
     *
     * @param orderId
     * @param price
     * @param goodsId
     * @return
     */
    boolean updateOrderGoodsPaymentAmountAdd(@Param("orderId") String orderId, @Param("price") BigDecimal price,
                                             @Param("adjustPrice") String adjustPrice, @Param("goodsId") Integer goodsId,
                                             @Param("isChangePrice") Integer isChangePrice, @Param("orderGoodsCommission") BigDecimal orderGoodsCommission);

    /**
     * 更改goods表实付金额
     *
     * @param orderId
     * @param price
     * @param goodsId
     * @return
     */
    boolean updateOrderGoodsPaymentAmountSubtract(@Param("orderId") String orderId, @Param("price") BigDecimal price,
                                                  @Param("adjustPrice") String adjustPrice, @Param("goodsId") Integer goodsId,
                                                  @Param("isChangePrice") Integer isChangePrice, @Param("orderGoodsCommission") BigDecimal orderGoodsCommission);

    /**
     * 更改base表实付金额
     *
     * @param orderId
     * @param calculateAfterMoney
     * @return
     */
    boolean updateOrderBasePaymentAmount(@Param("orderId") String orderId, @Param("calculateAfterMoney") BigDecimal calculateAfterMoney,
                                         @Param("adjustPrice") String adjustPrice, @Param("orderBaseCommission") BigDecimal orderBaseCommission);

    /**
     * 取消订单Record
     *
     * @param orderId
     * @return
     */
    boolean updateOrderPaymentAmountByRecord(@Param("orderId") String orderId, @Param("calculateAfterMoney") BigDecimal calculateAfterMoney);

    /**
     * 取消订单Trade
     *
     * @param orderId
     * @return
     */
    boolean updateOrderPaymentAmountByTrade(@Param("orderId") String orderId, @Param("calculateAfterMoney") BigDecimal calculateAfterMoney);


    /**
     * webm门店核销记录
     *
     * @param page
     * @param param
     * @return
     */
    IPage<QueryCancelRecordResultVO> queryCancelShopGateRecord2(IPage<QueryCancelRecordResultVO> page, @Param("param") PageUserTokenBeanDTO param);

    /**
     * webm门店核销记录
     *
     * @param page
     * @param param
     * @return
     */
    IPage<QueryCancelRecordResultVO> queryCancelShopGateRecord(IPage<QueryCancelRecordResultVO> page, @Param("param") PageUserTokenBeanDTO param);


    /**
     * 查询虚拟商品用户
     *
     * @param page
     * @return
     */
    List<String> queryVirtualGoodsUser2(@Param("page") PageUserTokenBeanDTO page);

    /**
     * 查询虚拟商品用户数量
     *
     * @param page
     * @return
     */
    Integer queryVirtualGoodsUserCount2(@Param("page") PageUserTokenBeanDTO page);

    /**
     * 查询虚拟商品用户
     *
     * @param page
     * @return
     */
    List<String> queryVirtualGoodsUser(@Param("page") PageUserTokenBeanDTO page);

    /**
     * 查询虚拟商品用户数量
     *
     * @param page
     * @return
     */
    Integer queryVirtualGoodsUserCount(@Param("page") PageUserTokenBeanDTO page);

    /**
     * 查询用户信息
     *
     * @param userId
     * @return
     */
    Map<String, Object> selectMembershipUserById(@Param("userId") String userId);


    /**
     * 分页查询记录
     *
     * @param page
     * @param param
     * @return
     */
    IPage<QueryCancelRecordResultVO> queryCancelRecord2(Page<QueryCancelRecordResultVO> page, @Param("param") Map<String, Object> param);

    /**
     * 分页查询记录
     *
     * @param page
     * @param param
     * @return
     */
    IPage<QueryCancelRecordResultVO> queryCancelRecord(Page<QueryCancelRecordResultVO> page, @Param("param") Map<String, Object> param);

    /**
     * 分页查询订单集合
     *
     * @param page
     * @param mapOrder
     * @return
     */
    IPage<OrderListVO> queryOrderList(IPage<OrderListVO> page, @Param("param") Map<String, Object> mapOrder);

    /**
     * 根据快递名查询店铺批量打印快递鸟信息
     *
     * @param expressName
     * @return
     */
    KdniaoExpressVO querykdniaoExpressByparam(@Param("expressName") String expressName);


    /**
     * 查询发货地址
     *
     * @param shippingAddressId
     * @return
     */
    ShopShippingAddressVO selectShippingAddress(@Param("addressId") Integer shippingAddressId);


    /**
     * 查询地址信息
     *
     * @param districtId
     * @return
     */
    BaseDistrictVO selectOrderArea(@Param("districtId") Integer districtId);
    /**
     * 更新交易订单
     *
     * @param orderId
     * @return
     */
    int updateConsumeTradeState(@Param("orderId") String orderId);

    /**
     * 更新交易明细
     *
     * @param orderId
     * @return
     */
    int updateConsunmRecordState(@Param("orderId") String orderId);


    /**
     * 查询基于店铺列表的订单
     *
     * @param mapShop
     * @return
     */
    List<SoldOrderExportDTO> queryOrderBaseOfShopList(@Param("param") Map<String, Object> mapShop);

    /**
     * 查订单总额和运费
     *
     * @param orderId
     * @return
     */
    Map queryorderpaymentamount(@Param("orderId") String orderId);


    /**
     * 查询红包和代金卷
     *
     * @param orderId
     * @return
     */
    WapGoodsSaleSumUpdateVO queryRedByEnvelope(String orderId);

    /**
     * 查看会员是否过期
     *
     * @param userId
     * @return
     */
    PlusMemberEntityVO selectplusmember(@Param("userId") String userId);

    /**
     * 退还plus会员折扣
     *
     * @param userId
     * @param usedAmount
     */
    void updateplusmember(@Param("userId") String userId, @Param("usedAmount") BigDecimal usedAmount);

    /**
     * 发送站内信通知
     *
     * @param platformMessage
     */
    void insertMessage(PlatformMessageVO platformMessage);


    /**
     * 查用户退款订单信息
     *
     * @param orderId
     * @param shopId
     * @return
     */
    OrderStatusVO selectorderid(@Param("orderId") String orderId, @Param("shopId") Integer shopId);

    /**
     * 查询用户投诉信息
     *
     * @param complaintsId
     * @param shopId
     * @return
     */
    OrderStatusVO selectcomplaintsid(@Param("complaintsId") String complaintsId, @Param("shopId") Integer shopId);


    /**
     * 查询未退款订单数量
     *
     * @param orderid
     * @return
     */
    Integer queryisVirtualOrderByTime(String orderid);

    /**
     * 定时任务fegin-确认收货超过14天默认默认好评
     *
     * @return
     */
    List<AcquiescenceVO> queryNotEvaluatedOrder();


    /**
     * 统计日周月销售额
     *
     * @param shopId
     * @return
     */
    List<SaleRoomVO> querySaleByShopId(@Param("shopGateId") Integer shopGateId,@Param("shopId") Integer shopId, @Param("shopType") Integer shopType);


    /**
     * 根据goodsId查询 积分商品的价格 所需积分 库存
     *
     * @param goodsId
     * @return
     */
    Map<String, Object> queryBygoodsId(Integer goodsId);

    /**
     * 查询店铺发货时长
     *
     * @param shopId
     * @param goodId
     * @return
     */
    BigDecimal queryShippingTimeByGoods(@Param("shopId") Integer shopId, @Param("goodsId") Integer goodId);

    /**
     * 查询是否是供应商订单
     *
     * @param orderId
     * @return
     */
    OrderPayInfoVO queryOrderInfo(@Param("orderId") String orderId);

    /**
     * 查询供应商订单信息
     *
     * @param paymentNum
     * @return
     */
    List<DealerOrderGoodsVO> querySupplyOrder(String paymentNum);


    /**
     * 查询当前售卖区域模板下 的城市Id
     *
     * @param transportAreaId
     * @return
     */
    String selectByTransportAreaIds(@Param("transportAreaId") Integer transportAreaId);


    /**
     * 查询当前商品是否是虚拟商品
     *
     * @param goodsId
     * @return
     */
    Integer queryVirtualGoods(Integer goodsId);

    /**
     * 查询商品是否有限制
     * 通过商品ID查询该商品是否有限制。返回值为null表示没有限制，非null值表示有限制。
     *
     * @param goodsId 商品ID，用于查询商品限制信息。
     * @return Integer 返回商品的限制信息，null表示无限制。
     */
    Integer getIntegralGoodsIsLimit(Integer goodsId);

    /**
     * 查询订单中的商品数量
     * 通过订单ID查询该订单中商品的数量。此方法用于统计订单中的商品总数，以便于后续的业务处理。
     *
     * @param orderId 订单ID，用于查询订单中的商品数量。
     * @return Integer 返回订单中的商品数量。
     */
    Integer selectOrderGoodsNum(@Param("orderId") String orderId);

    /**
     * 查询当前订单已发货的商品数量
     *
     * @param orderId
     * @param goodsIds
     * @return
     */
    Integer selectOrderFahuoGoodsNum(@Param("orderId") String orderId, @Param("goodsIds") List<Integer> goodsIds);

    /**
     * 查询骑手信息。
     * <p>
     * 通过传入分销商ID，查询关联的骑手信息。此方法旨在为系统提供一种检索骑手数据的途径，
     * 以便于根据分销商ID快速定位到对应的骑手信息，支持进一步的业务操作或数据统计。
     *
     * @param distributorId 分销商ID，用于指定查询的骑手所属的分销商。
     * @return 返回一个Map对象，包含骑手的相关信息。其中，键值对的键代表信息的类型，
     * 值为对应的信息内容。具体的键值对内容由实际的数据库结构和业务需求决定。
     */
    Map<String, Object> queryRiderInfo(@Param("distributorId") String distributorId);


    /**
     * 门店中心  查询门店配送单
     *
     * @param param
     * @return
     */
    IPage<GateQueryDeliveryOrdersVO> gateShopQueryDeliveryOrders(Page<QueryCancelRecordResultVO> pageQuery, @Param("param") GateQueryDeliveryOrdersDTO param);

    /**
     * 根据订单号查询门市配送订单详情。
     *
     * @param orderId 订单号，用于查询订单的详细信息。
     * @return 返回门市配送订单的详细信息，包括订单状态、配送信息等。
     */
    GateShopDeliveryOrdersDetailVO gateShopQueryDeliveryOrdersDetail(@Param("orderId") String orderId);

    /**
     * 商家确认订单。
     *
     * @param orderId 订单号，商家通过此订单号进行确认操作。
     */
    void shopAffirm(@Param("orderId") String orderId);

    /**
     * 更新订单的骑手评价状态。
     *
     * @param orderId 订单号，用于更新订单中骑手的评价状态。
     */
    void updateOrderRiberEvaluated(@Param("orderId") String orderId);

    /**
     * 查询门市配送订单数量。
     *
     * @param userId     用户ID，用于查询特定用户下的订单数量。
     * @param orderStart 订单状态码，用于筛选特定状态的订单。
     * @param pickup     提货方式，用于筛选特定提货方式的订单。
     * @return 返回满足条件的门市配送订单数量。
     */
    Integer queryGateDeliveryOrdersNum(@Param("userId") String userId, @Param("orderStatusNum") Integer orderStart, @Param("pickup") Integer pickup);

    /**
     * 查询用户 消费总额
     *
     * @param shopUserDTO
     * @return
     */
    BigDecimal queryUserTotalExpenditure(@Param("param") ShopUserDTO shopUserDTO);

    /**
     * 查询用户 最近消费时间
     *
     * @param shopUserDTO
     * @return
     */
    String queryUserLatestConsumptionTime(@Param("param") ShopUserDTO shopUserDTO);

    /**
     * 查询店铺会员历史订单
     *
     * @param page
     * @param shopUserDTO
     * @return
     */
    IPage<ShopUserHistoricalOrdersVO> queryShopUserHistoricalOrders(IPage page, @Param("param") ShopUserDTO shopUserDTO);

    /**
     * 查询采购单信息
     *
     * @param param 查询参数
     * @return 进货单信息
     */
    PurchaseOrderVO queryPurchaseOrderDetails(@Param("param") StorageInventoryFlowGoodsDTO param);

    /**
     * 查询用户消费信息
     * <p>
     * 该方法通过接收一个参数映射，查询并返回用户的消费信息
     * 它允许通过不同的参数组合来筛选查询结果，提高了查询的灵活性和效率
     *
     * @param map 包含查询参数的映射，参数通过键值对形式指定，可能包括用户ID、时间范围等
     * @return 返回一个映射，包含查询到的用户消费信息，具体结构取决于查询结果
     */
    Map<String, Object> queryUserConsumption(@Param("param") Map<String, String> map);

    /**
     * 查询用户订单信息
     * <p>
     * 该方法主要用于查询用户在即时通讯（IM）系统中的订单信息，支持分页查询和各种查询条件
     * 通常被上层业务逻辑调用，以获取特定用户或订单的相关信息
     *
     * @param page 分页对象，用于指定查询的当前页码、每页大小等分页信息，同时也用于接收查询结果的封装
     * @param map  包含查询参数的映射，如用户ID、订单状态、时间范围等，用于精确查询条件
     * @return 返回一个IPage对象，其中包含符合查询条件的用户订单信息的分页结果
     */
    IPage<ImQueryUserOrderInfoDTO> imQueryUserOrderInfo(IPage<ImQueryUserOrderInfoDTO> page, @Param("param") Map<String, Object> map);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    String selectOrderSource(@Param("orderId") String orderId);

    /**
     * 根据订单号查询一件代发商品
     *
     * @param orderId
     * @return
     */
    List<DealerGoodsFlagVO> queryDealerOrderList(@Param("orderId") String orderId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    String queryCommonIdAndShopId(@Param("spuId") String spuId,@Param("shopId") Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer queryGoodsSource(@Param("goodsId") Integer goodsId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    String querySkuId(@Param("goodsId") Integer goodsId,@Param("shopId") Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    String querySpuId(@Param("goodsId") Integer goodsId,@Param("shopId") Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<String> selectCommonIdsByOrderId(@Param("orderId") String orderId);

    /**
     * TODO 补充方法说明
     * @param orderId TODO
     * @return TODO
     */
    Integer checkOrderIsRefund(String orderId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer selectOrderShipmentQuantity(@Param("orderId") String orderId);

    /**
     * 查询经销子单是否已全部发货
     * @param orderId
     * @return
     */
    List<OrderBaseEntity>  querySonOrderShipment(@Param("orderId")String orderId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<Integer>  selectGoodsIdBySkuIdList(@Param("skuIds") List<String> skuIds,  @Param("shopId")Integer shopId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    String queryOrderShippingExpressId(@Param("expressName")String expressName);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer insertBaseExpress(@Param("param") BaseExpressDTO baseExpress);

    /**
     * TODO 补充方法说明
     * @param page TODO
     * @return TODO
     */
    IPage<YiYaTongOrderVO> queryYiYaTongOrderByPage(IPage<Map<String, Object>> page,@Param("map") Map<String, Object> data);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<YiYaTongOrderVO> queryYiYaTongOrderByPage(@Param("map") Map<String, Object> data);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer queryDealserShopId(@Param("originalOrder")String originalOrder);

    /**
     * 更新经销基础订单
     * @param dealerOrdeId
     * @param mapGoods
     * @return
     */
    boolean updateByDealerOrderBase(@Param("dealerOrdeId")String dealerOrdeId,@Param("param") Map<String, Object> mapGoods);

    /**
     * 供应商 查询基础订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<OrderDetailsVO> queryOrderBaseOfSupplierShop(IPage<OrderDetailsVO> page, @Param("map") Map<String, Object> map);
    /**
     * 供应商 查询基础订单 导出
     *
     * @param map
     * @return
     */
    List<OrderExportVO> soldSupplierOrdersExp(@Param("map") Map<String, Object> map);

    /**
     * 查询经销店铺名称
     * @param originalOrder
     * @return
     */
    String queryDealerShopName(@Param("originalOrder")String originalOrder);

    /**
     * 查询供应商发货订单库
     *
     * @param page
     * @param mapBase
     * @return
     */
    IPage<ShipmentsByOrderBaseVO> queryShipmentsSupplierOrderBase(IPage<ShipmentsByOrderBaseVO> page, @Param("param") Map<String, Object> mapBase);

    /**
     * 查询进货订单
     * @param page
     * @param param
     * @return
     */
    IPage<queryInvoiceOrderVO> queInvoiceOrder(IPage<queryInvoiceOrderVO> page , @Param("param") Map<String, Object> param);

    /**
     * 根据进货订单号查询商品详情
     * @param orderId
     * @return
     */
    List<queryPurchaseGoodsDetailsVO> queryGoodsDetailsList(@Param("orderId") String orderId);

    /**
     * 查询进货订单
     * @param page
     * @param param
     * @return
     */
    IPage<queryPurchaseOrderVO> queryPurchaseOrder(IPage<queryPurchaseOrderVO> page , @Param("param") Map<String, Object> param);

    /**
     * 查询子订单id
     * @param paymentNumbers
     * @return
     */
    List<String> querySubOrderId(@Param("paymentNumbers")String paymentNumbers);

    /**
     * 更新子订单基础
     *
     * @param mapPayment
     * @return
     */
    boolean updateOrderBaseByPaymentBySub(@Param("param") Map<String, Object> mapPayment);

    /**
     * 查询店铺是否是自营
     *
     * @param shopId
     * @return
     */
    Boolean queryShopSelfSupport(@Param("shopId") Integer shopId);

    /**
     * 查询店铺经营类目
     *
     * @param shopId
     * @return
     */
    List<Integer> queryShopGoodsCatIds(@Param("shopId") Integer shopId);

    /**
     * 查询商品所属分类
     *
     * @param goodsId
     * @return
     */
    Integer queryGoodsCatByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * TODO 补充方法说明
     * @param goodsId TODO
     * @return TODO
     */
    Map<String, Object> queryBySupplierGoodsId(Integer goodsId);

    /**
     * 更新
     *
     * @param mapInvoice
     * @return
     */
    boolean updateInvoice(Map<String, Object> mapInvoice);

    /**
     * 查询分类抽佣比例
     *
     * @param catId
     * @return
     */
    BigDecimal queryCatCommission(@Param("catId") Integer catId);

    /**
     * TODO 补充方法说明
     * @param mapGoods TODO
     * @return TODO
     */
    boolean insertByOrderGoods(Map<String, Object> mapGoods);

    /**
     * 修改经销折扣信息
     *
     * @param orderId
     * @param goodsId
     * @param originalOrder
     */
    void updateOrderDealerGoods(@Param("orderId") String orderId, @Param("goodsId") Integer goodsId,
                                @Param("originalOrder") String originalOrder);

    /**
     * 查询供应商商品信息
     *
     * @param goodsId
     * @return
     */
    OrderDealerGoodsVO queryOrderSupperGoods(@Param("goodsId") Integer goodsId);

    /**
     * 查询经销折扣信息
     *
     * @param orderDealerGoodsVO
     */
    void insertByOrderDealerGoods(@Param("param") OrderDealerGoodsVO orderDealerGoodsVO);

    /**
     * 根据shopid查询
     *
     * @param shopId
     * @return
     */
    Map<String, Object> selectSupplierUserId(@Param("shopId") Integer shopId);

    /**
     * 根据顺序插入基础映射
     * 此方法用于按照特定顺序将映射中的键值对插入到某个数据结构或数据库中
     * 确保插入操作遵循映射中键的顺序
     *
     * @param mapBase 包含要插入的键值对的映射
     * @return 插入操作是否成功
     */
    boolean insertByOrderBaseMap(Map<String, Object> mapBase);

    /**
     * 下单成功，删除进货车商品
     *
     * @param userId
     * @param goodsIdList
     * @return
     */
    boolean batchSupplierDeleteGoods(@Param("userId") String userId, @Param("goodsIdList") List<Integer> goodsIdList);

    /**
     * 根据订单ID查询商品信息
     * 此方法用于同步订单相关商品信息，通过订单ID作为参数进行查询
     *
     * @param orderId 订单ID，用于查询关联的商品信息
     * @return 返回一个SynchronousGoodsDTO对象列表，包含与订单ID关联的商品信息
     */
    List<SynchronousGoodsDTO> queryGoodsByOrderId(@Param("orderId") String orderId);

    /**
     * 查询供应商商品信息
     *
     * @param goodsId
     * @return
     */
    OrderDealerGoodsVO queryOrderSupplierGoods(@Param("goodsId") Integer goodsId);

    /**
     * 查询供应商商品含税金额
     *
     * @param supplierGoodsId
     * @return
     */
    BigDecimal querySupplierTaxInclusiveAmount(@Param("supplierGoodsId") Integer supplierGoodsId);

    /**
     * 查询经销折扣
     *
     * @param shopId
     * @param supplierShopId
     * @return
     */
    BigDecimal queryDealerDiscount(@Param("shopId") Integer shopId, @Param("supplierShopId") Integer supplierShopId);

    /**
     * 查询子单订单号
     * @param orderId
     * @return
     */
    List<OrderGoodsVO> querySonOrderList(@Param("orderId") String orderId);

    /**
     * 查询子单退单号
     * @param sonOrderId
     * @return
     */
    String querySonReturnCode(@Param("orderId") String sonOrderId);

    /**
     * 查询子单商品信息
     * @param orderId
     * @return
     */
    List<OrderGoodsVO> querySonOrderGoodsList(@Param("orderId") String orderId);

    /**
     * 更新退货订单的佣金费用
     * 该方法用于在数据库中更新特定订单的退回佣金费用，通常在处理退货时调用
     *
     * @param orderId 订单ID，用于标识需要更新的订单
     * @param returnSonOrderCommissionFee 退回的子订单佣金费用，表示该订单退回时涉及的佣金金额
     */
    void updateReturnOrderCommissionFe(@Param("orderId")String orderId,
                                       @Param("returnSonOrderCommissionFee") BigDecimal returnSonOrderCommissionFee);

    /**
     * 查询经销商品信息
     *
     * @param goodsId
     * @return
     */
    OrderDealerGoodsVO queryOrderDealerGoods(@Param("goodsId") Integer goodsId);

    /**
     * 查询商品是否是 代发商品
     *
     * @param goodsId
     * @return
     */
    Integer queryDealerGoodsFlagNum(@Param("goodsId") Integer goodsId);

    /**
     * 将经销折扣价设置为商品单价
     *
     * @return
     */
    Integer getPriceBySupplierShopAndGoodsShopId(@Param("shopId") Integer shopId, @Param("dalerShopId") Integer dalerShopId);

    /**
     * 根据供应商店铺ID、商品信息和用户ID获取价格
     * 此方法旨在通过特定的店铺、商品和用户信息来查询商品的价格
     *
     * @param shopId 供应商店铺ID，用于标识特定的供应商店铺
     * @param userId 用户ID，用于标识请求价格信息的用户
     * @return 返回与供应商店铺、商品和用户关联的价格信息，如果没有找到相关价格信息，则返回null
     */
    Integer getPriceBySupplierShopAndGoodsAndUserId(@Param("shopId") Integer shopId,@Param("userId")  String userId);

    /**
     * 查询子单
     * @param page
     * @param map
     * @return
     */
    IPage<OrderDetailsVO> querySonSupplierOrders(IPage<OrderDetailsVO> page, @Param("map") Map<String, Object> map);

    /**
     * 根据用户ID查询经销商折扣
     *
     * @param shopId 用户ID，用于标识特定的用户
     * @param supplierShopId 供应商店铺ID，用于确定特定的供应商
     * @return 返回用户在特定供应商处的经销商折扣，使用BigDecimal表示以确保精度
     */
    BigDecimal queryDealerDiscountByUserId(String shopId, Integer supplierShopId);

    /**
     *  主单 经销商品 佣金
     *
     * @param
     * @return
     */
    ShopSettleAccountInfoDTO getSellOnAccountShop(@Param("param")Map<String, Object> mapAccount);

    /**
     * 主单 经销商品金额
     * @param mapAccount
     * @return
     */
    BigDecimal getSellOnAccountTotal(@Param("param")Map<String, Object> mapAccount);

    /**
     *  子单 佣金之和
     * @param mapAccount
     * @return
     */
    Double querySonOrderCommissionTotal(Map<String, Object> mapAccount);

    /**
     * 根据订单ID获取赊账订单的结算信息
     *
     * @param orderId 订单ID
     * @return ShopSettleAccountInfoDTO 结算信息对象，包含订单的结算详情
     */
    ShopSettleAccountInfoDTO getSellOnAccountOrder(String orderId);

    /**
     * 根据订单ID获取赊账金额
     *
     * @param orderId 订单ID
     * @return Double 赊账金额
     */
    BigDecimal getSellOnAccount(String orderId);

    /**
     * 查询供应商订单的佣金总额
     *
     * @param orderId 订单ID
     * @return Double 佣金总额
     */
    Double querySupppllierOrderCommossionTotal(String orderId);

    /**
     * 根据订单ID获取自营销售的佣金
     *
     * @param orderId 订单ID
     * @return ShopSettleAccountInfoDTO 自营销售佣金的结算信息
     */
    ShopSettleAccountInfoDTO getSelfSellingCommission(String orderId);

    /**
     * 查询商品名与商品图
     *
     * @param goodsIds
     * @param orderId
     * @return
     */
    List<ShippingGoodsMessageVO> querySupplierGoodsMessage(@Param("goodsIds") List<Integer> goodsIds, @Param("orderId") String orderId);



    /**
     * 更新订单的团购状态
     *
     * @param orderIdlist 一个包含多个订单ID的列表，用于指定需要更新团购状态的订单
     */
    void updateOrderGroupbuyStatus(@Param("orderIdlist") List<String> orderIdlist);

    /**
     * 查询店铺订单数量
     * 根据店铺ID查询该店铺的订单数量
     *
     * @param shopId 店铺ID，用于标识需要查询的店铺
     * @return 店铺的订单数量如果查询结果为空，则返回null
     */
    Integer selectShopOrder(@Param("shopId") Integer shopId);

    /**
     * 获取未完成开票的订单数量
     *
     * @param param
     * @return
     */
    Integer getUndealedOrderCount(@Param("param") Map<String, Object> param);

    /**
     * 根据日期范围获取支付金额
     * @param startTimeStr
     * @param endTimeStr
     * @param shopId
     * @return
     */
    BigDecimal queryPayMentByDate(@Param("startTimeStr") String startTimeStr, @Param("endTimeStr") String endTimeStr, @Param("shopId") String shopId);

    /**
     *  查询支付金额数量
     * @param startTimeStr
     * @param endTimeStr
     * @param shopId
     * @return
     */
    Integer queryPayCountByDate(@Param("startTimeStr") String startTimeStr, @Param("endTimeStr") String endTimeStr, @Param("shopId") String shopId);


    /**
     * 根据店铺ID和日期范围查询店铺的支付金额
     * @param shopId
     * @param today
     * @return
     */
    List<AppShopBaseSalesInfoVO> queryShopBasePayPriceByApp(String shopId, String today);


    /**
     * 查询订单信息
     *
     * @param orderId
     * @return
     */
    Map<String, Object> queryOrderInfoFeign(@Param("orderId") String orderId);


    /**
     * 查询用户信息
     */
    Map<String,Object> queryBuyUserId(@Param("orderId") String orderId);

    /**
     * 更新退款会计期间。
     *
     * @param param 退款会计期间参数DTO
     * @return 更新操作的影响行数
     */
    int updateRefundAccountPeriod(@Param("param") RefundAccountParamDTO param);

    /**
     * 更新账单状态。
     *
     * @param accountingPeriodId 会计期间ID
     * @param accountStatus 账单状态
     */
    void editAccountStatus(@Param("accountingPeriodId") String accountingPeriodId, @Param("accountStatus") Integer accountStatus);

    /**
     * 查询订单信息
     * @param orderId
     * @return
     */
    OrderBaseVO queryOrderInfoFegin(String orderId);

    /**
     * 查询会计期间ID。
     *
     * @param orderId 订单号
     * @return 会计期间ID
     */
    String queryAccountingPeriodId(@Param("orderId") String orderId);


    /**
     * 根据会计期间ID查询账单详情。
     *
     * @param accountingPeriodId 会计期间ID
     * @return 账单详情VO
     */
    AccountShopBillDetailsVO selectDetailsByPeriodId(@Param("accountingPeriodId") String accountingPeriodId);


    /**
     * 查询店铺
     *
     * @param orderId
     * @return
     */
    String queryTreasureShopIdByInt(@Param("orderId") String orderId);

    /**
     * 根据订单号查询订单信息。
     *
     * @param orderId 订单号，是查询订单的关键标识。
     * @return 返回订单号对应的订单信息。如果找不到匹配的订单，可能返回空字符串。
     */
    String queryOrderId(@Param("orderId") String orderId);


    /**
     * 根据订单号查询订单信息。
     * @param orderId
     * @return
     */
    List<Map<String, Object>> queryOrderIdAndPaymentNumber(@Param("orderId") String orderId);

    /**
     * 新增怡亚通订单记录
     * @param submitOrderListVO
     * @return
     */
    Integer insertYiYaTongOrder(@Param("param") SubmitOrderVO submitOrderListVO);

    /**
     * 判断纯数字订单
     *
     * @param orderId
     * @param paymentNumber
     * @return
     */
    List<AssetTmpInfoDTO> queryAssetInfo(@Param("orderId") String orderId, @Param("paymentNumber") String paymentNumber);

    /**
     * 查询买方用户ID
     *
     * @param orderId
     * @return
     */
    String queryBuyerUserIdByOrderBase(@Param("orderId") String orderId);


    /**
     * 查找订单商品id
     * @param orderId
     * @return
     */
    List<WapGoodsSaleSumDTO> queryGoodsByOrderIdFeign(@Param("orderId") String orderId);

    /**
     * 查询订单编号是否已存在
     *
     * @param orderId 订单编号，用于查询是否存在重复的订单
     * @return 如果订单编号已存在，则返回非空的Integer值，表示存在的数量；如果不存在，则返回0或null
     */
    Integer queryOrderNumExist(@Param("orderId")String orderId);
    /**
     * 将货币符号和汇率存入订单表
     * @param orderId
     * @param currencyCode
     * @param exchangeRate
     * @return
     */
    Integer updateCurrencyAndExchangeRate(@Param("orderId")String orderId, @Param("currencyCode")String currencyCode,
                                          @Param("exchangeRate")BigDecimal exchangeRate,@Param("currencySymbol")String currencySymbol);

    /**
     * 查询订单货币信息
     * @param orderId
     * @return
     */
    BaseCurrencySymbolVO queryOrderCurrencyInfo(@Param("orderId") String orderId);

    /**
     * 查询退单参数
     * @param orderId
     * @return
     */
    RefundParamDTO queryRefundParam(@Param("orderId")String orderId);

    /**
     * 付款凭证审核改为 审核驳回
     * @param orderId
     */
    void cancelOrderUpdatePaymentVocher(@Param("orderId")String orderId);

    /**
     * 报错京东订单号
     * @param orderId
     * @param jingDongOrderId
     */
    void updateOrderJingDongOrderId(@Param("orderId")String orderId, @Param("jingDongOrderId")String jingDongOrderId);

    /**
     * 查询京东订单
     * @param page
     * @param data
     * @return
     */
    IPage<YiYaTongOrderVO> queryJingdDngOrderByPage(IPage<Map<String, Object>> page,@Param("map") Map<String, Object> data);
    /**
     * 查询京东订单
     * @param data
     * @return
     */
    List<YiYaTongOrderVO> queryJingDongOrderByPageExel(@Param("map") Map<String, Object> data);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    OrderBaseEntity selectMallPaymentOrder(@Param("orderId") String orderId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int markMallOrderPaid(@Param("orderId") String orderId,
                          @Param("expectedStatus") Integer expectedStatus,
                          @Param("paidStatus") Integer paidStatus,
                          @Param("cashierNo") String cashierNo,
                          @Param("channelOrderNo") String channelOrderNo,
                          @Param("paidTime") java.time.LocalDateTime paidTime);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    OrderBaseEntity selectMallOrderByCashierNo(@Param("cashierNo") String cashierNo);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    int bindMallCashier(@Param("orderId") String orderId,
                        @Param("expectedStatus") Integer expectedStatus,
                        @Param("cashierNo") String cashierNo);
}
