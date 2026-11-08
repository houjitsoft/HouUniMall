package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.houjiang.commons.dto.goods.DiscountGoodsListVO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.vop.JingDongReturnGoodsDTO;
import com.houjiang.commons.dto.vop.JingDongReturnOrderDTO;
import com.houjiang.commons.dto.vop.OrderGoodsJdVopDTO;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import com.houjiang.commons.vo.goods.DiscountGoodsInfoVO;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.goods.SeckillGoodsInfoVO;
import com.houjiang.commons.vo.goods.StorageInventoryFlowGoodsVO;
import com.houjiang.order.dto.ApplyReturnGoodDTO;
import com.houjiang.order.dto.OrderGoodsDiscountDTO;
import com.houjiang.order.dto.ReturnOrderDTO;
import com.houjiang.order.entity.OrderGoodsEntity;
import com.houjiang.order.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderGoodsMapper
 * @Description 订单商品表
 * @Author Amos
 * @Date 2023-05-31 11:41:48
 * @Version 1.0
 * @author Amos
 */
@Mapper
public interface OrderGoodsMapper extends BaseMapper<OrderGoodsEntity> {


    /**
     * 查询商品数量
     *
     * @param param
     * @return
     */
    Integer queryOrderGoodsCount(@Param("param") Map<String, Object> param);

    /**
     * 查询门店商品数量
     *
     * @param commonId
     * @param shopId
     * @return
     */
    Integer queryShopTakeGoodsStart(@Param("commonId") Integer commonId, @Param("shopId") String shopId);

    /**
     * 查询分类抽佣比例
     *
     * @param catId
     * @return
     */
    BigDecimal queryCatCommission(@Param("catId") Integer catId);

    /**
     * 新增订单商品
     *
     * @param totalGoodsShippingFee
     * @param orderId
     * @param shopId
     * @param shopName
     * @param goodsId
     * @param commonId
     * @param userId
     * @param goodsName
     * @param catId
     * @param goodsSpec
     * @param goodsprice
     * @param goodsNumber
     * @param goodsImage
     * @param goodsShippingFee
     * @param orderGoodsAmount
     * @param orderGoodsPlatformBear
     * @param paymentRealPrice
     * @param orderGoodsStatus
     * @param orderGoodsEvaluationStatus
     * @param goodsRtpPrice
     * @param couponSumMoney
     * @param platformVoucherRptPrice
     * @param goodsReturnStatus
     * @param goodsRefundStatus
     * @param orderGoodsTime
     * @param orderGoodsIdDeliver
     * @param promotionId
     * @param smallOrder
     * @param integral
     * @param dealerCommission
     * @param supplyCommission
     * @param orderGoodsCommission
     * @param goodsMemberDiscount
     * @param orderDiscountFee
     * @param plusgoodsprice
     * @param goodsPlusDis
     * @return
     */
    boolean insertByOrderGoods(@Param("totalGoodsShippingFee") BigDecimal totalGoodsShippingFee, @Param("orderId") String orderId, @Param("shopId") Integer shopId, @Param("shopName") String shopName, @Param("deliverShopId") Integer deliverShopId, @Param("deliverShopName") String deliverShopName, @Param("dropShipping") Integer dropShipping, @Param("goodsId") Integer goodsId, @Param("commonId") Integer commonId, @Param("userId") String userId, @Param("goodsName") String goodsName, @Param("catId") Integer catId, @Param("goodsSpec") String goodsSpec, @Param("goodsprice") BigDecimal goodsprice, @Param("goodsNumber") Integer goodsNumber, @Param("goodsImage") String goodsImage, @Param("goodsShippingFee") BigDecimal goodsShippingFee, @Param("orderGoodsAmount") BigDecimal orderGoodsAmount, @Param("orderGoodsPlatformBear") BigDecimal orderGoodsPlatformBear, @Param("paymentRealPrice") BigDecimal paymentRealPrice, @Param("orderGoodsStatus") Integer orderGoodsStatus, @Param("orderGoodsEvaluationStatus") Integer orderGoodsEvaluationStatus, @Param("goodsRtpPrice") BigDecimal goodsRtpPrice, @Param("couponSumMoney") BigDecimal couponSumMoney, @Param("platformVoucherRptPrice") BigDecimal platformVoucherRptPrice, @Param("goodsReturnStatus") Integer goodsReturnStatus, @Param("goodsRefundStatus") Integer goodsRefundStatus, @Param("orderGoodsTime") String orderGoodsTime, @Param("orderGoodsIdDeliver") Integer orderGoodsIdDeliver, @Param("promotionId") Integer promotionId, @Param("smallOrder") Integer smallOrder, @Param("integral") Integer integral, @Param("dealerCommission") BigDecimal dealerCommission, @Param("supplyCommission") BigDecimal supplyCommission, @Param("orderGoodsCommission") BigDecimal orderGoodsCommission, @Param("goodsMemberDiscount") BigDecimal goodsMemberDiscount, @Param("orderDiscountFee") BigDecimal orderDiscountFee, @Param("plusgoodsprice") BigDecimal plusgoodsprice, @Param("goodsPlusDis") Integer goodsPlusDis, @Param("fullReductionDiscount") BigDecimal fullReductionDiscount, @Param("fullFoldingDiscount") BigDecimal fullFoldingDiscount, @Param("activityUnitPrice") BigDecimal activityUnitPrice, @Param("goodsPromotionId") Integer goodsPromotionId, @Param("voucherId") Integer voucherId, @Param("verId") Integer verId, @Param("pointsRate") Double pointsRate, @Param("cashPrice") BigDecimal cashPrice);

    /**
     * 获取信息
     *
     * @param orderId
     * @return
     */
    Map<Object, Object> getVitualInfo(@Param("orderId") String orderId);

    /**
     * 查询采购编号
     *
     * @param orderId
     * @param goodsId
     * @return
     */
    Integer queryBuyNumByorderId1(@Param("orderId") String orderId, @Param("goodsId") Integer goodsId);

    /**
     * 查询商品集合
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsListVO> queryGoodsList(@Param("orderId") String orderId);

    /**
     * 更改状态
     */
    boolean updateOrderGoodsByStatusByPureDigital(@Param("orderStatus") Integer orderStatus, @Param("paymentNumbers") String paymentNumbers);

    /**
     * 更新商品状态
     */
    boolean updateOrderGoodsByStatus(@Param("orderStatus") Integer orderStatus, @Param("orderNumber") String orderNumber);

    /**
     * 商品集合详情
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsVO> listGoodsDetails(@Param("orderId") String orderId);

    /**
     * 退还商品集合
     *
     * @param paraMap
     * @return
     */
    List<GoodsDetailsVO> listReturnGoods(@Param("paraMap") Map<String, String> paraMap);

    /**
     * 退款商品集合
     *
     * @param selectMap
     * @return
     */
    List<GoodsDetailsVO> listReturnGoods1(@Param("paraMap") Map<String, String> selectMap);


    /**
     * 获取发货人代码
     *
     * @param shipmentId
     * @return
     */
    Map<Object, Object> getShipperCode2(@Param("shipmentId") Integer shipmentId);


    /**
     * 查询订单商品信息
     *
     * @param page
     * @param paraMap
     * @return
     */
    IPage<GoodsDetailsListVO> queryGoodsList2(IPage<GoodsDetailsListVO> page, @Param("param") Map<String, Object> paraMap);

    /**
     * 查询商品集合
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsListVO> queryGoodsList1(String orderId);


    /**
     * 查询店铺订单商品列表
     *
     * @param page
     * @param param
     * @return
     */
    IPage<GoodsDetailsListVO> queryGoodsListByPage(IPage<GoodsDetailsListVO> page, @Param("param") Map<String, Object> param);


    /**
     * 查询订单采购数量
     *
     * @param orderId
     * @param orderGoodsId
     * @return
     */
    ApplyReturnGoodDTO queryOrderPurchaseQuantity(String orderId, String orderGoodsId);


    /**
     * 获取商品折扣
     *
     * @param getGoodsMap
     * @return
     */
    OrderGoodsDiscountDTO getGoodsDiscount(Map<String, String> getGoodsMap);


    /**
     * 查询商品全部排行
     *
     * @return
     */
    List<GoodsParaVO> queryAllGoods();

    /**
     * 查询商品本日排行
     *
     * @return
     */
    List<GoodsParaVO> queryTodayGoods();

    /**
     * 查询商品本周排行
     *
     * @return
     */
    List<GoodsParaVO> queryWeekGoods();

    /**
     * 查询商品本月排行
     *
     * @return
     */
    List<GoodsParaVO> queryMonthGoods();

    /**
     * 查询商品本年排行
     *
     * @return
     */
    List<GoodsParaVO> queryYearGoods();

    /**
     * 添加供应商订单
     *
     * @param orderId
     * @param shopId
     * @param shopName
     * @param goodsId
     * @param commonId
     * @param userId
     * @param goodsName
     * @param catId
     * @param goodsSpec
     * @param goodsprice
     * @param goodsNumber
     * @param goodsImage
     * @param goodsShippingFee
     * @param orderGoodsAmount
     * @param orderGoodsPlatformBear
     * @param paymentRealPrice
     * @param orderGoodsStatus
     * @param orderGoodsEvaluationStatus
     * @param goodsRtpPrice
     * @param couponSumMoney
     * @param platformVoucherRptPrice
     * @param goodsReturnStatus
     * @param goodsRefundStatus
     * @param orderGoodsTime
     * @param orderGoodsIdDeliver
     * @param promotionId
     * @param smallOrder
     * @param integral
     * @param dealerCommission
     * @param supplyCommission
     * @param orderGoodsCommission
     * @param goodsMemberDiscount
     */
    void insertSupplierByOrderGoods(@Param("orderId") String orderId, @Param("shopId") Integer shopId,
                                    @Param("shopName") String shopName, @Param("deliverShopId") Integer deliverShopId,
                                    @Param("deliverShopName") String deliverShopName, @Param("dropShipping") Integer dropShipping,
                                    @Param("goodsId") Integer goodsId, @Param("commonId") Integer commonId,
                                    @Param("userId") String userId, @Param("goodsName") String goodsName, @Param("catId") Integer catId,
                                    @Param("goodsSpec") String goodsSpec, @Param("goodsprice") BigDecimal goodsprice,
                                    @Param("goodsNumber") Integer goodsNumber, @Param("goodsImage") String goodsImage,
                                    @Param("goodsShippingFee") BigDecimal goodsShippingFee,
                                    @Param("orderGoodsAmount") BigDecimal orderGoodsAmount,
                                    @Param("orderGoodsPlatformBear") BigDecimal orderGoodsPlatformBear,
                                    @Param("paymentRealPrice") BigDecimal paymentRealPrice, @Param("orderGoodsStatus") Integer orderGoodsStatus,
                                    @Param("orderGoodsEvaluationStatus") Integer orderGoodsEvaluationStatus,
                                    @Param("goodsRtpPrice") BigDecimal goodsRtpPrice, @Param("couponSumMoney") BigDecimal couponSumMoney, @Param("platformVoucherRptPrice") BigDecimal platformVoucherRptPrice,
                                    @Param("goodsReturnStatus") Integer goodsReturnStatus,
                                    @Param("goodsRefundStatus") Integer goodsRefundStatus, @Param("orderGoodsTime") String orderGoodsTime,
                                    @Param("orderGoodsIdDeliver") Integer orderGoodsIdDeliver, @Param("promotionId") Integer promotionId, @Param("smallOrder") Integer smallOrder, @Param("integral") Integer integral,
                                    @Param("dealerCommission") BigDecimal dealerCommission, @Param("supplyCommission") BigDecimal supplyCommission, @Param("orderGoodsCommission") BigDecimal orderGoodsCommission,
                                    @Param("goodsMemberDiscount") BigDecimal goodsMemberDiscount, @Param("verId") Integer verId);


    /**
     * 批量发货
     *
     * @param mapGoods
     * @return
     */
    boolean piLiangupdateByOrderGoods(Map<String, Object> mapGoods);

    /**
     * 发货
     *
     * @param mapGoods
     * @return
     */
    boolean updateByOrderGoods(Map<String, Object> mapGoods);

    /**
     * 供应商  待发货订单
     *
     * @param orderId
     * @param shopId
     * @return
     */
    List<ShipmentsByOrderGoodsDetailsVO> ShipmentsByOrderGoods(@Param("orderId") String orderId, @Param("shopId") String shopId);

    /**
     * 供应商 已售经销，已售自销订单
     *
     * @param orderId
     * @param shopId
     * @return
     */
    List<GoodsDetailsVO> querySupplyOrders(@Param("orderId") String orderId, @Param("shopId") String shopId);


    /**
     * 分开发货
     *
     * @param mapGoods
     * @return
     */
    boolean updateSeparateShipmentsByOrderGoods(Map<String, Object> mapGoods);


    /**
     * 按订单货物发货
     *
     * @param orderId
     * @param shopId
     * @return
     */
    List<ShipmentsByOrderGoodsDetailsVO> shipmentsByOrderGoods(@Param("orderId") String orderId, @Param("shopId") Integer shopId);


    /**
     * 查询订单
     *
     * @param orderId
     * @param shopId
     * @return
     */
    List<GoodsDetailsVO> queryOrders(@Param("orderId") String orderId, @Param("shopId") Integer shopId);

    /**
     * 查询商品集合
     *
     * @param orderId
     * @return
     */
    List<GoodsDetailsListVO> queryGoodsListLimit(@Param("orderId") String orderId);

    /**
     * 查询订单商品信息
     *
     * @param map
     * @return
     */
    List<GoodsDetailsListVO> queryComplaintsByGoodsList(@Param("param") Map<String, Object> map);

    /**
     * 商品实付金额
     *
     * @param goodsId
     * @param orderId
     * @return
     */
    BigDecimal selectgoodsprice(@Param("goodsId") Integer goodsId, @Param("orderId") String orderId);

    /**
     * 如果订单包含赠品 查询
     *
     * @param orderId
     * @return
     */
    String queryOrderZengpinInfo(@Param("param") String orderId);

    /**
     * 查询当前订单是否改价
     *
     * @param map
     * @return
     */
    Integer queryOrderUpdatePrice(@Param("param") Map<String, Object> map);


    /**
     * 更新订单商品
     *
     * @param updateGoodsMap
     */
    void updateOrderGoodsById2(Map<String, Object> updateGoodsMap);

    /**
     * 根据商品ID查询购买数量。
     *
     * @param goodsId 商品ID，用于查询购买此商品的数量。
     * @param userId  用户ID，限定查询指定用户购买的数量。
     * @return 购买数量，以Integer形式返回。
     */
    Integer queryBuyCountByGoodsId(@Param("goodsId") Integer goodsId, @Param("userId") String userId);

    /**
     * 根据折扣信息查询订单中的商品数量。
     *
     * @param param 包含折扣信息的Map对象，用于查询符合条件的商品数量。
     * @return 商品数量，以Integer形式返回。
     */
    Integer queryOrderGoodsCountByDiscount(@Param("param") Map<String, Object> param);

    /**
     * 根据订单ID和退货行列表计算应支付的商品金额。
     *
     * @param orderId     订单ID，用于查询订单详情。
     * @param returnGoods 退货行列表，用于筛选参与支付计算的商品。
     * @return 应支付的商品金额，以BigDecimal形式返回。
     */
    BigDecimal selectPayGoods(@Param("orderId") String orderId, @Param("returnGoods") List<Integer> returnGoods);

    /**
     * 根据订单ID和退货行列表计算应收费用的商品金额。
     *
     * @param orderId     订单ID，用于查询订单详情。
     * @param returnGoods 退货行列表，用于筛选参与费用计算的商品。
     * @return 应收费用的商品金额，以BigDecimal形式返回。
     */
    BigDecimal selectFeeGoods(@Param("orderId") String orderId, @Param("returnGoods") List<Integer> returnGoods);

    /**
     * 更改物流编号
     *
     * @param orderGoodsId
     * @param orderShippingMessage
     * @param orderShippingCode
     * @return
     */
    boolean updateChangeShipmentsByGoodsId(Integer orderGoodsId, String orderShippingCode, String orderShippingMessage);

    /**
     * 更改物流编号
     *
     * @param orderShippingExpressId
     * @param orderGoodsId
     * @param orderShippingCode
     * @param orderShippingMessage
     * @return
     */
    boolean updateChangeAllShipmentsByGoodsId(Integer orderGoodsId, Integer orderShippingExpressId, String orderShippingCode, String orderShippingMessage);

    /**
     * 根据商品ID查询猫的佣金。
     * <p>
     * 此方法用于查询特定商品ID对应的猫的佣金金额。
     *
     * @param goodsId 商品的唯一标识ID。
     * @return 返回表示佣金的BigDecimal对象，精确到小数点后若干位。
     */
    BigDecimal queryCatCommissionByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 根据订单ID和商品列表查询订单佣金。
     * <p>
     * 此方法用于综合考虑订单ID和订单中的商品列表来计算并查询订单的总佣金。
     *
     * @param orderId   订单的唯一标识ID。
     * @param goodsList 包含商品详细信息的列表，每个商品是一个Map对象，包含商品相关的关键信息。
     * @return 返回表示订单佣金的BigDecimal对象，精确到小数点后若干位。
     */
    BigDecimal queryOrderCommission(@Param("orderId") String orderId, @Param("goodsList") List<Map<String, Object>> goodsList);

    /**
     * 根据订单ID获取订单商品项的详细信息。
     * <p>
     * 此方法用于查询特定订单ID下的所有商品项的详细信息，包括但不限于商品名称、价格、数量等。
     *
     * @param orderId 订单的唯一标识ID。
     * @return 返回一个包含订单商品项详细信息的列表，每个商品项是一个OrderDetailGoodsItemVO对象。
     */
    List<OrderDetailGoodsItemVO> getOrderGoodsItem(@Param("orderId") String orderId);

    /**
     * 根据订单ID和商品ID查询订单商品项的详细信息。
     *
     * @param orderId
     * @param goodsId
     * @return
     */
    OrderGoodsEntity selectReturnGoods(@Param("orderId") String orderId, @Param("goodsId") Integer goodsId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer selectInventoryFlowNum(@Param("orderId") String orderId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    List<StorageInventoryFlowGoodsVO> queryPurchaseOrderGoodsList(@Param("orderId") String orderId);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer queryDealerGoodsId(@Param("dealerShopId") Integer dealerShopId, @Param("goodsId") Integer goodsId);

    /**
     * 经销单发货
     *
     * @param mapGoods
     * @return
     */
    boolean updateByDealerOrderGoods(@Param("orderId") String orderId, @Param("goodsId") Integer goodsId, @Param("param") Map<String, Object> mapGoods);

    /**
     * 按订单货物发货
     *
     * @param orderId
     * @return
     */
    List<ShipmentsByOrderGoodsDetailsVO> shipmentsByOrderGoodsDetails(String orderId);

    /**
     * 查询子单是否已发货成功
     *
     * @param originalOrder
     * @param orderId
     * @return
     */
    List<ShipmentsByOrderGoodsDetailsVO> querySonOrderShipmentsStart(@Param("originalOrder") String originalOrder, @Param("orderId") String orderId);

    /**
     * 查询订单发票货物信息
     *
     * @param param
     * @return
     */
    List<OrderGoodsDetailVO> queryOrderInvoiceGoodsInfo(@Param("param") Map<String, Object> param);

    /**
     * 根据订单ID选择供应商订单详情
     *
     * @param orderId 订单ID，用于查询特定订单的详细信息
     * @return 返回一个包含商品详情的列表，这些商品属于指定供应商订单
     */
    List<GoodsDetailsVO> selectSupplierOrderDetails(String orderId);

    /**
     * 根据订单ID获取供应商订单货物的发货信息
     *
     * @param orderId 订单ID，用于查询与特定订单相关的发货信息
     * @return 返回一个包含发货信息的列表，这些发货信息与指定供应商订单的货物相关
     */
    List<ShipmentsByOrderGoodsDetailsVO> shipmentsBySupplierOrderGoods(String orderId);

    /**
     * 查询供应商订单的货物出库详情
     *
     * @param orderId 订单ID，用于查询特定订单的货物出库详细信息
     * @return 返回一个包含货物出库详情的列表，这些详情与指定供应商订单相关
     */
    List<GoodsOrderDetailsVO> querySupplierOrderShipmentGoods(String orderId);

    /**
     * 查询供应商订单中的新类型商品销售总额
     *
     * @param orderId 订单ID，用于查询特定订单中新类型商品的销售总额
     * @return 返回一个包含新类型商品销售总额信息的列表，这些信息与指定供应商订单相关
     */
    List<WapGoodsSaleSumDTO> queryGoodsNewType(String orderId);

    /**
     * 查询折扣商品折扣
     *
     * @param goodsId
     */
    void lessGoodsBySales(@Param("goodsId") Integer goodsId, @Param("salenum") Integer salenum);


    /**
     * 查询折扣商品折扣
     *
     * @param goods
     */
    Integer selectGoodsSalenum(@Param("param") WapGoodsSaleSumDTO goods);

    /**
     * 根据订单ID查询购买数量
     *
     * 该方法通过特定的订单ID查询与该订单相关的所有购买数量信息
     * 查询结果以列表形式返回，其中每个元素都是一个映射，映射中包含了购买数量的相关信息
     *
     * @param orderId 订单ID，用于标识特定订单的唯一字符串
     * @return 包含购买数量信息的列表，每个元素都是一个映射，映射中的键是字段名，值是对应的字段值
     */
    List<Map<String, Object>> queryBuyNumByorderId(String orderId);

    /**
     * 分页查询按退货列出的货物
     *
     * @param page
     * @param orderId
     * @return
     */
    IPage<GoodsDetailsListVO> queryGoodsListByReturn(IPage<GoodsDetailsListVO> page, @Param("orderId") String orderId);


    /**
     * 查询商品数量
     * @param param
     * @return
     */
    Integer queryOrderGoodsCountFeign(@Param("param")Map<String, Object> param);

    /**
     * 查询折扣商品信息，返回单个商品的详细信息
     * 该方法专注于查询单个商品的折扣信息，通过商品列表结果对象作为参数
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件和商品相关信息
     * @return 返回单个商品的折扣信息详细视图对象
     */
    DiscountGoodsInfoVO queryDiscountGoodsInfo2(@Param("param") DiscountGoodsListVO discountGoodsListResult);


    /**
     * 查询符合条件的买家用户列表
     * 该方法用于筛选出符合条件的买家用户，条件通过商品列表结果对象传递
     *
     * @param discountGoodsListResult 折扣商品列表结果对象，包含查询条件
     * @return 返回符合条件的买家用户ID列表
     */
    List<String> queryBuyerUsers(@Param("param") DiscountGoodsListVO discountGoodsListResult);


    /**
     * 查询满足特定条件的记录数量
     * 该方法用于统计满足特定条件的记录数，这些条件通过一个参数映射传递
     *
     * @param paramMap 参数映射，包含统计查询所需的条件
     * @return 返回满足条件的记录数量
     */
    Integer queryCountForOlder(@Param("param")  Map<String,Object> paramMap);


    /**
     * 查询用户的订单数量
     * 该方法用于统计用户的订单数量，统计条件通过参数映射传递
     *
     * @param paramMap 参数映射，包含统计查询所需的条件
     * @return 返回用户的订单数量
     */
    Integer queryUserOrderCount(@Param("param") Map<String, Object> paramMap);

    /**
     *查询商品信息
/**
     * @param appSeckillGoodsResult
     * @return
     */
    SeckillGoodsInfoVO querySeckillGoodsInfo2(@Param("param") AppSeckillGoodsQueryResultVO appSeckillGoodsResult);

    /**
     * TODO 补充方法说明
     * @param orderId TODO
     * @return TODO
     */
    List<OrderGoodsVO> queryOrderGoodsByOrderId(String orderId);

    /**
     * 查找售后商品
     */
    List<WapGoodsSaleSumDTO> queryReturnGoodsToSale(@Param("param") ReturnOrderDTO dto);

    /**
     * TODO 补充方法说明
     * @param orderId TODO
     * @return TODO
     */
    Integer queryOrderReturnGoodsCount(String orderId);

    /**
     * 查询返回商品
     *
     * @param orderId
     * @return
     */
    List<Integer> queryReturnGoods(@Param("orderId") String orderId);

    /**
     * 返回商品集合
     *
     * @param selectMap
     * @return
     */
    List<GoodsOrderDetailsVO> returnGoodsList(Map<String, String> selectMap);

    /**
     * 保存京东订单号
     * @param orderId
     * @param jingDongOrderId
     */
    void updateOrderJingDongOrderId(@Param("orderId")String orderId, @Param("jingDongOrderId")String jingDongOrderId);

    /**
     * 将京东子订单号保存入库中
     * @param orderGoodsJdVopDTO
     */
    void updateJongDongSonOrder(@Param("param") OrderGoodsJdVopDTO orderGoodsJdVopDTO);

    /**
     * 根据商城 商品id和订单号 查询京东订单号
     * @param orderId
     * @param orderGoodsId
     * @return
     */
    String queryJdOrderId(@Param("orderId")String orderId, @Param("orderGoodsId")String orderGoodsId);

    /**
     * 批量查询订单下商品售后权益
     * @param jingDongReturnOrderDTO
     * @return
     */
    List<JingDongReturnGoodsDTO> queryJdOrderGoodsInfo(@Param("param") JingDongReturnOrderDTO jingDongReturnOrderDTO);

    /**
     * 查询j京东的订单号 退单号
     * @return
     */
    List<JingDongReturnOrderDTO> queryJdReturnOrderInfo(@Param("orderId") String orderId);

    /**
     * 根据订单号查询京东售后单信息
     * @param orderId
     * @return
     */
    List<JingDongReturnGoodsDTO> queryJdReturnOrderGoodsInfo(@Param("orderId") String orderId);
}
