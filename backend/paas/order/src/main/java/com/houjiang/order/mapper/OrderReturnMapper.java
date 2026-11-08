package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.dto.base.UpMemberClassDTO;
import com.houjiang.commons.dto.job.ShopSettleAccountInfoDTO;
import com.houjiang.commons.dto.order.RefundParamDTO;
import com.houjiang.commons.dto.payment.AccountPeriodParamDTO;
import com.houjiang.commons.dto.payment.GroupBookingTradeDTO;
import com.houjiang.commons.dto.payment.RefundAccountParamDTO;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.dto.userms.ShopYgUserInfoDTO;
import com.houjiang.commons.vo.goods.GoodsDetailsListVO;
import com.houjiang.commons.vo.goods.WapGoodsSaleSumUpdateVO;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.shop.AccountPeriodVO;
import com.houjiang.commons.vo.shop.ManageShopAddressQueryVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.OrderReturnEntity;
import com.houjiang.order.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.houjiang.order.entity.OrderReturn
 * @author Amos
 */
@Mapper
public interface OrderReturnMapper extends BaseMapper<OrderReturnEntity> {

    /**
     * 更新返回订单
     *
     * @param orderId
     */
    void updateOdbOrderReturn(String orderId);

    /**
     * 查询返回订单数据
     *
     * @param orderId
     * @return
     */
    OrderReturnStatusDTO queryOrderReturnData(String orderId);


    /**
     * 更新返回订单
     *
     * @param orderReturnMap
     * @return
     */
    boolean updateOrderReturn(Map<String, Object> orderReturnMap);


    /**
     * 新增返回订单
     *
     * @param ordereReturnMap
     * @return
     */
    Integer insertOrderReturn(@Param("param") Map<String, Object> ordereReturnMap);


    /**
     * 更新订单商品
     *
     * @param orderGoodsMap1
     * @return
     */
    Integer updateOrderGoodsById(@Param("param") Map<String, Object> orderGoodsMap1);

    /**
     * 插入退款信息
     *
     * @param orderReturnMap
     * @return
     */
    Integer returnMoney(@Param("param") Map<String, Object> orderReturnMap);

    /**
     * 更新订单商品
     *
     * @param orderGoodsMap
     * @return
     */
    Integer updateOrderGoodsById1(@Param("param") Map<String, Object> orderGoodsMap);

    /**
     * 查询所有退货商品是否全退
     * @param orderId
     * @return
     */
    Integer queryOrderGoodsReturnStatus(@Param("orderId") String orderId);

    /**
     * 查询员工列表
     *
     * @param businessId
     * @return
     */
    List<String> queryEmployeesList(Integer businessId);


    /**
     * 分页查询博客类型
     *
     * @param page
     * @param param
     * @return
     */
    IPage<ReturnOrderVO> getBlogByType(IPage<ReturnOrderVO> page, @Param("param") Map<String, Object> param);


    /**
     * 查看退货详情
     *
     * @param returnCode
     * @param shopId
     * @return
     */
    SellerOrderDetailVO getSellerOrderDetailById(@Param("returnCode") String returnCode,@Param("shopId") String shopId);


    /**
     * 查询经销折扣
     *
     * @param returnCode
     * @return
     */
    SellerOrderDetailVO queryDistributionDiscount(String returnCode);

    /**
     * 查询返回订单详情
     *
     * @param orderId
     * @return
     */
    ReturnOrderDetailVO queryOrderReturnDetail(String orderId);

    /**
     * 更新返回订单
     *
     * @param orderGoodsMap
     * @return
     */
    Integer updateOrderReturnById(Map<String, Object> orderGoodsMap);


    /**
     * 更新返回订单状态
     *
     * @param orderId
     * @param expirationTime
     * @return
     */
    Integer updateOrderReturnState(@Param("orderId") String orderId, @Param("exp") Date expirationTime);

    /**
     * 获取退款订单列表
     */
    IPage<ReturnOrderDetailVO> listRefundOrder(IPage<ReturnOrderDetailVO> page, @Param("param") Map<String, Object> refundMap);

    /**
     * 获取退款订单详情
     */
    ReturnOrderDetailVO refundDetail(Map<String, Object> refundMap);

    /**
     * 查询订单商品信息
     */
    List<GoodsDetailsVO> listReturnGoods(@Param("param") Map<String, String> paraMa);

    /**
     * 导出退款列表
     */
    List<ReturnOrderDetailVO> listrefundorderExp(@Param("param") Map<String, String> paraMap);

    /**
     * 查看提醒发货列表
     */
    IPage<OrderRemindDeliveryVO> queryOrderRemindDeliveryPort(IPage<OrderRemindDeliveryVO> page, @Param("param") OrderRemindDeliveryDTO param);

    /**
     * 提醒发货列表导出
     */
    List<OrderRemindDeliveryVO> OrderRemindDeliveryPortExp(@Param("param") OrderRemindDeliveryDTO param);

    /**
     * 获取退款订单
     */
    IPage<ReturnOrderDetailVO> listReturnRefund(IPage<ReturnOrderDetailVO> page, @Param("refundMap") Map<String, Object> refundMap);

    /**
     * 获取退货订单报表导出
     */
    List<ReturnOrderDetailVO> listReturnRefundExcel(@Param("refundMap") Map<String, String> paraMap);


    /**
     * @param page
     * @param param
     * @return
     */
    IPage<ReturnReportQueryseleVO> queryReturnsaleReport(IPage<ReturnReportQueryseleVO> page, @Param("param") ReturnReportQueryDTO param);


    /**
     * 查询订单商品信息
     * @param orderId
     * @return
     */
    List<GoodsDetailsVO> queryGoodsDetail(@Param("orderId") String orderId);


    /**
     * 查询退货销售报告Excel
     *
     * @param param
     * @return
     */
    List<ReturnsaleReportVO> queryReturnsaleReportExcel(@Param("param") ReturnReportQueryDTO param);

    /**
     * 分页查询退款订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<SellerOrderDetailVO> listRefund(Page<SellerOrderDetailVO> page, @Param("param") Map<String, Object> map);

    /**
     * 查询返回商品集合
     *
     * @param map
     * @return
     */
    List<GoodsDetailsListVO> listReturnGoods1(@Param("param") Map<String, Object> map);

    /**
     * 分页查询返回订单
     *
     * @param page
     * @param map
     * @return
     */
    IPage<ReturnOrderVO> listReturnOrders(Page<ReturnOrderVO> page, @Param("param") Map<String, Object> map);

    /**
     * 查的退款订单id
     * @param returnCode
     * @return
     */
    String getOrderIdByReturnCode(@Param("returnCode") String returnCode);

    /**
     * 查询用户售后商品
     * @param orderId
     * @param shopId
     * @return
     */
    List<Integer> queryReturnGoods(@Param("orderId") String orderId,@Param("shopId") String shopId);

    /**
     * 更新订单退款信息
     * @param orderBaseMap
     * @return
     */
    Integer updateOrderBaseById(@Param("param") Map<String, Object> orderBaseMap);

    /**
     * 查询退款金额
     * @param orderId
     * @return
     */
    BigDecimal queryReturnCash(@Param("orderId") String orderId);

    /**
     * 查的订单编号
     * @param orderId
     * @return
     */
    String queryPaymentNumber(@Param("orderId") String orderId);

    /**
     * 根据订单号查询订单状态
     * @param uOrderId
     * @return
     */
    List<Integer> queryOrderRecordStatus(@Param("uOrderId") String uOrderId);

    /**
     * 查询订单退款信息
     * @param orderId
     * @return
     */
    RefundParamDTO queryRefundPara(@Param("orderId") String orderId);

    /**
     * 查询支付配置信息
     * @param paymentMode
     * @return
     */
    Map selectYfPayConfig(String paymentMode);

    /**
     * 查询余额支付总额
     * @param uOrderId
     * @return
     */
    BigDecimal queryAllTradePaymentRechargeCard(@Param("uOrderId") String uOrderId);

    /**
     * 查询订单交易明细
     * @param orderId
     * @return
     */
    GroupBookingTradeDTO queryRefundTrade(@Param("orderId") String orderId);


    /**
     * 查询账期id
     * @param orderId
     * @return
     */
    String queryAccountingPeriodId(@Param("orderId") String orderId);

    /**
     * 更新账单
     * @param param
     * @return
     */
    int updateRefundAccountPeriod(@Param("param") RefundAccountParamDTO param);

    /**
     * 查找账单信息
     * @param accountingPeriodId
     * @return
     */
    AccountShopBillDetailsVO selectDetailsByPeriodId(@Param("accountingPeriodId") String accountingPeriodId);

    /**
     * 全部还款
     * @param accountingPeriodId
     * @param accountStatus
     */
    void editAccountStatus(@Param("accountingPeriodId") String accountingPeriodId, @Param("accountStatus") Integer accountStatus);

    /**
     * 查找账期会员的账期信息
     * @param shopId
     * @param userId
     * @return
     */
    AccountPeriodVO getAccountPeriodByIds(@Param("shopId") Integer shopId, @Param("userId") String userId);

    /**
     * 更新账期信息
     * @param param
     */
    void updateAccountPeriod(@Param("param") AccountPeriodParamDTO param);

    /**
     * 员工用户退款金额更新
     * @param record
     * @return
     */
    int updateUserShareReturn(@Param("param") ShopYgUserInfoDTO record);

    /**
     * 减去分享商品销售数量
     * @param fxGoodsInfo
     * @return
     */
    int updateGoodsSaleNum(@Param("param") FxGoodsInfoDTO fxGoodsInfo);

    /**
     * 更新微客用户佣金业绩
     * @param record
     * @return
     */
    int updateVkCommissionReturn(@Param("param") VKUserDTO record);

    /**
     * 平台退货佣金计算  会员折扣计算
     * @param orderId
     * @return
     */
    List<OrderPingtaiCommissionDTO> queryOrderPingTaiCommission(@Param("orderId") String orderId);

    /**
     * 查询佣金
     * @param orderId
     * @param shopId
     * @return
     */
    BigDecimal querySupperGoodsPrice(@Param("orderId") String orderId,@Param("shopId") Integer shopId);

    /**
     *更新佣金
/**
     * @param orderId
     * @param returnCommission
     */
    void updateOrderRetuenSupperCommission(@Param("orderId") String orderId, @Param("returnCommission") BigDecimal returnCommission);

    /**
     * 根据订单号查询支付单号
     * @param orderId
     * @return
     */
    String queryOrderPaymentNumber(@Param("orderId") String orderId);

    /**
     * 当前订单号 和 支付单号 查询是否有其他订单没有全退货
     * @param orderId
     * @param paymentNumber
     * @return
     */
    Integer queryNotRetuenOrder(@Param("orderId") String orderId, @Param("paymentNumber") String paymentNumber);

    /**
     * 修改红包状态
     * @param orderId
     */
    void updateHongbaoStart(@Param("orderId") String orderId);

    /**
     * 有个优惠劵状态
     * @param orderId
     */
    void updateYouHuiquanStart(@Param("orderId") String orderId);

    /**
     * 更新交易佣金退款
     * @param orderId
     * @param countPingTaiCommission
     */
    void updateOrderBasePingTaiCommission(@Param("orderId") String orderId, @Param("countPingTaiCommission") BigDecimal countPingTaiCommission);

    /**
     * 更新退款表 平台退还佣金 退还会员折扣
     * @param orderId
     * @param countPingTaiCommission
     * @param huiYuanZhekou
     */
    void updateOrderReturnPingTaiCommission(@Param("orderId") String orderId, @Param("countPingTaiCommission") BigDecimal countPingTaiCommission, @Param("huiYuanZhekou") BigDecimal huiYuanZhekou);

    /**
     * 查询订单佣金
     * @param orderId
     * @return
     */
    List<OrderSupplyCommissionDTO> queryOrderSupplyCommission(@Param("orderId") String orderId);

    /**
     * 更新商品订单退还佣金
     * @param orderGoodsCommissionList
     * @return
     */
    int updateOrderGoodsCommissionBatch(@Param("orderGoodsCommissionList") List<OrderSupplyCommissionDTO> orderGoodsCommissionList);

    /**
     * 更新订单退还佣金
     * @param orderId
     * @param dealerReturnCommission
     * @param supplyReturnCommission
     * @return
     */
    int updateOrderSupplyCommission(@Param("orderId") String orderId, @Param("dealerReturnCommission") BigDecimal dealerReturnCommission, @Param("supplyReturnCommission") BigDecimal supplyReturnCommission);

    /**
     * 查询订单商品信息
     * @param orderId
     * @return
     */
    List<Map<String, Object>> queryBuyNumByorderId(@Param("orderId") String orderId);

    /**
     * 根据orderid 查询应该回退的积分和userid
     * @param orderId
     * @return
     */
    Map<String, Object> queryAddpointsLogByOrderId(@Param("orderId") String orderId);

    /**
     * 订单实付金额
     * @param orderId
     * @return
     */
    BigDecimal queryOrderAmount(@Param("orderId") String orderId);

    /**
     * 订单商品总价（不含运费）
     * @param orderId
     * @return
     */
    BigDecimal queryOrderGoodsAmount(@Param("orderId") String orderId);

    /**
     * 查询退货数量和商品价格
     * @param orderId
     * @return
     */
    List<OrderReturnPointsDTO> queryOrderReturnInfo(@Param("orderId") String orderId);

    /**
     * 查询交易，明细
     * @param orderId
     * @return
     */
    AssetDirectionAndPayTypeInfoDTO getAssetDirectionAndPayTypeInfo(@Param("orderId") String orderId);

    /**
     * 新增交易明细
     * @param map
     */
    void insertDetail(@Param("param") Map<String, Object> map);

    /**
     * 添加积分修改记录
     * @param param
     */
    void feginAddpointsLog(@Param("param") UserPointLogParamDTO param);

    /**
     *更新付款状态
/**
     * @param map
     * @return
     */
    Integer updateRefuse(@Param("param") Map<String, String> map);

    /**
     * 查询当前商品的销量
     * @param commonId
     * @return
     */
    Integer queryCommonNameSalenum(@Param("commonId") Integer commonId);

    /**
     * 更新商品销量
     * @param commonId
     * @param orderGoodsReturnNum
     * @return
     */
    Integer updateGoodsSalenum(@Param("commonId") Integer commonId, @Param("orderGoodsReturnNum") Integer orderGoodsReturnNum);

    /**
     * 获取当前用户的积分
     * @param userId
     * @return
     */
    Integer queryMemberMessageListIntegral(@Param("userId") String userId);
    /**
     * 修改当前用户的积分
     * @param param
     * @return
     */
    void updateMemberMessageListIntegral(@Param("param") Map<String, Object> param);

    /**
     * 查询会员积分规则
     * @param gradeRule
     * @return
     */
    MembershipGradeDTO queryMembershipGradeByGradeRule(@Param("gradeRule") Integer gradeRule);

    /**
     * 修改会员等级
     * @param upMemberClassDto
     * @return
     */
    Integer updateMemeberClass(@Param("param") UpMemberClassDTO upMemberClassDto);

    /**
     * 查询订单状态
     * @param orderId
     * @return
     */
    Integer queryOrderStatus(@Param("orderId") String orderId);

    /**
     * 判断是否是虚拟订单 且没有被核销
     * @param orderid
     * @return
     */
    Map<String, Object> selectisVirtualOrderByTime(@Param("orderId") String orderid);

    /**
     * 该虚拟订单已过期 且过期不退款直接返回false
     * @param orderid
     * @return
     */
    Integer queryisVirtualOrderByTime(@Param("orderId") String orderid);

    /**
     * 取goodsid commonid 购买数量
     * @param orderId
     * @return
     */
    List<WapGoodsSaleSumUpdateVO> queryGoodsXinXi(@Param("orderId") String orderId);

    /**
     * 更新商品库存
     * @param param
     * @return
     */
    Integer updateCommonStockByGoodsId(@Param("param") WapGoodsSaleSumUpdateVO param);
    /**
     * 更新商品库存
     * @param param
     * @return
     */
    Integer updateGoodsStockByGoodsId(@Param("param") WapGoodsSaleSumUpdateVO param);

    /**
     * 更新销量
     * @param goods
     */
    void lessgoodsBySales(@Param("param") WapGoodsSaleSumUpdateVO goods);

    /**
     * 查询红包和代金卷
     * @param orderId
     * @return
     */
    WapGoodsSaleSumUpdateVO queryRedByEnvelope(@Param("orderId") String orderId);

    /**
     * 退还代金卷
     * @param voucherCode
     */
    void updateCouponStuate(@Param("voucherCode") String voucherCode);

    /**
     * 退还红包
     * @param redpacketCode
     */
    void updateRedPageStuate(@Param("redpacketCode") String redpacketCode);

    /**
     * 未核销且在有效期内
     * @param orderId
     * @return
     */
    Integer queryisVirtualOrderByTime1(@Param("orderId") String orderId);

    /**
     * 过期且可过期退款
     * @param orderid
     * @return
     */
    Integer queryisVirtualOrderByTime2(@Param("orderId") String orderid);

    /**
     * 获取会员折扣和交易佣金
     * @param orderId
     * @return
     */
    Map<String, Object> queryOrderReturnInfoById(@Param("orderId") String orderId);

    /**
     * 更新订单会员折扣和交易佣金
     * @param map
     */
    void updateOrderReturnInfoById(@Param("param") Map<String, Object> map);

    /**
     * 查询地址信息
     * @param shippingAddressId
     * @return
     */
    ManageShopAddressQueryVO selectShopAddressDetail(@Param("shippingAddressId") Integer shippingAddressId);

    /**
     * 查询城市id
     * @param districtIds
     * @return
     */
    List<String> selectByIdList(@Param("districtIds") List<Integer> districtIds);

    /**
     * 更新退款状态
     * @param orderBaseMap
     * @return
     */
    Integer updateOrderBaseById1(@Param("param") Map<String, Object> orderBaseMap);

    /**
     * 更新退货状态
     * @param platform
     * @return
     */
    Integer updateOrderReturnShop(@Param("param") OrderReturnPlatformVO platform);

    /**
     * 更新订单商品退货退款状态
     * @param orderGoodsMap
     * @return
     */
    Integer updateOrderGoodsById2(@Param("param") Map<String, Object> orderGoodsMap);

    /**
     * 更新交易明细退货退款状态
     * @param map
     * @return
     */
    Integer updateRecordRefuse(@Param("param") Map<String, String> map);

    /**
     * 查询商品分类
     *
     * @param goodsId
     * @return
     */
    List<Map> selectgoodscat(@Param("param") List goodsId);

    /**
     * 获取商家质保金余额
     *
     * @param shopId
     * @return
     */
    Integer selectshop(Integer shopId);

    /**
     * 查询是否有其他订单没有全退货
     *
     * @param orderId
     * @param paymentNumber
     * @return
     */
    List queryOrderId(@Param("orderId") String orderId, @Param("paymentNumber") String paymentNumber);

    /**
     * 查询其他订单是否全额退款
     *
     * @param orderIds
     * @return
     */
    List<Map> queryOrderreturn(@Param("orderIds") List orderIds);

    /**
     * 平台超时未处理
     *
     * @return
     */
    List<ReturnOrderDetailVO> updateplatform1();

    /**
     * 更新失效时间
     * @param orderId
     * @param expriationTime
     */
    void updateplatform(@Param("orderId") String orderId, @Param("expriationTime") Date expriationTime);

    /**
     * 查询订单发货地址
     *
     * @param orderId
     * @return
     */
    String queryOrderAddress(@Param("orderId") String orderId);

    /**
     * 查询订单卖家名
     * @param orderId
     * @return
     */
    String queryOrderAddrName(@Param("orderId") String orderId);

    /**
     * 商家超时未处理退款售后
     *
     * @return
     */
    List<OrderReturnEntity> queryTimeOutReturn();


    /**
     * 查询店铺类型
     * @param shopId
     * @return
     */
    Integer selectshoptype(@Param("shopId") Integer shopId);


    /**
     * 商家超时未同意退货订单
     *
     * @return
     */
    List<ReturnOrderDetailsVO> queryTimeOutReturnGoods();


    /**
     * 售后查询商家超时未确认收货申请
     *
     * @return
     */
    List<ReturnOrderDetailsVO> queryTimeOutReturnGoods1();



    /**
     *查询买家超时未处理退款申请
/**
     * @return
     */
    List<ReturnOrderDetailVO> updateTimeOutBuyReturn();


    /**
     * 查询买家超时未处理 退货申请
     * @return
     */
    List<ReturnOrderDetailVO> updatebuyTimeOutReturn();


    /**
     * 查询买家超时填写物流信息 退货申请
     * @return
     */
    List<ReturnOrderDetailVO> updatebuyTimeOutReturnGoods();

    /**
     * 更新秒杀库存
     * @param wapGoodsSaleSumUpdate
     */
    void updateSeckillStockByGoodsId(@Param("param") WapGoodsSaleSumUpdateVO wapGoodsSaleSumUpdate);

    /**
     * 获取退款状态
     *
     * @param returnCode
     * @return
     */
    Integer getReturnStateByReturnCode(String returnCode);

    /**
     * 获取退款状态
     *
     * @param orderId
     * @return
     */
    Integer getReturnStateByOrderId(String orderId);

    /**
     * 得到第一个退款商品
     *
     * @param orderId
     * @return
     */
    String queryReturnGoodsNameByOrderId(@Param("orderId") String orderId);;

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    Integer queryStorageId(@Param("orderId")  String orderId);

    /**
     * TODO 补充方法说明
     */
    void updateReturnOrderCommissionFe(@Param("orderId") String orderId,
                                       @Param("returnSonOrderCommissionFee") BigDecimal returnSonOrderCommissionFee);

    /**
     * TODO 补充方法说明
     * @param mapAccount TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSellOnReturnCashByShop(Map<String, Object> mapAccount);

    /**
     * TODO 补充方法说明
     * @param orderId TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSelfSellingReturnCash(String orderId);

    /**
     * TODO 补充方法说明
     * @param orderId TODO
     * @return TODO
     */
    ShopSettleAccountInfoDTO getSellOnReturnCash(String orderId);


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
    BigDecimal querShopReturnCash(@Param("pojo") ShopSalesStatisticsDTO pojo);

    /**
     * 根据订单id获取退还订单的平台退款支付凭证
     */
    String selectReturnPaymentVoucher(String orderId);

    /**
     * 查询返回订单
     *
     * @param returnCode
     * @return
     */
    String queryreturnCodeByOrderId(String returnCode);

    /**
     * 根据京东订单号查询 商城订单信息
     * @param jdOrderId
     * @return
     */
    ReturnOrderVO queryReturnOrderByJDOrderId(@Param("jdOrderId") String jdOrderId);

    /**
     * 根据京东退单申请单号查询 商城订单号
     * @param jdReturenOrderId
     * @return
     */
    String queryOrderIdByJDReturnOrderId(@Param("param") String jdReturenOrderId);
}




