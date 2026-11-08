package com.houjiang.payment.feign;

import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.payment.AssetTmpInfoDTO;
import com.houjiang.commons.dto.payment.RefundAccountParamDTO;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.BaseCurrencySymbolVO;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.order.PointsPayReqVO;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.yiyatong.base.SubmitOrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@FeignClient("order")
public interface OrderServiceClient {

    /**
     * 查询是否多订单
     *
     * @param map
     * @return
     */
    @PostMapping("/order/base/queryShopIdByOrderIdNum")
    ResponseResult queryShopIdByOrderIdNum(@RequestBody Map<String, Object> map);

    /**
     * 查询店铺信息
     *
     * @param map
     * @return
     */
    @PostMapping("/order/base/queryShopIdByOrderInfo")
    ResponseResult queryShopIdByOrderInfo(@RequestBody Map<String, Object> map);

    /**
     * 查询商品数量 fegin
     *
     * @param map
     * @return
     */
    @PostMapping("/order/base/queryShopIdByOrderIdCount")
    Integer queryGoodNumByOrderId(@RequestBody Map<String, Object> map);

    /**
     * 判断该订单是否是虚拟订单或者是门店自提订单
     */
    @PostMapping("/order/base/isVirtualOrder")
    ResponseResult isVirtualOrder(@RequestBody Map<String, String> orderIdIsNum);

    /**
     * 为虚拟订单添加兑换码
     */
    @PostMapping("/order/base/addRedeemCode")
    ResponseResult addRedeemCode(@RequestBody Map<String, Object> param);

    /**
     * 下单通知
     */
    @RequestMapping("/order/base/orderToInform")
    ResponseResult orderToInform(@RequestBody Map<String, Object> map);

    /**
     * 获取非数字订单
     */
    @PostMapping(value = "/order/base/queryUpayByOrderId")
    String queryUpayByOrderId(@RequestBody String order_id);

    /**
     * 更新orderBase中支付方式等字段
     */
    @RequestMapping("/order/base/updateByPaymentType")
    public ResponseResult UpdateOrderBaseByPayment(@RequestBody Map<String, Object> map);


    /**
     * 根据订单id查订单信息
     *
     * @param OrderId
     * @return
     */
    @GetMapping("/order/base/selectOrderInfoById")
    ResponseResult selectOrderInfoById(@RequestParam("OrderId") String OrderId);

    /**
     * 更新orderBase中支付方式等字段
     *
     * @param map
     * @return
     */
    @PostMapping("/order/base/updateByPaymentTypeFegin")
    boolean HFiveUpdateOrderBaseByPayment(@RequestBody Map<Object, Object> map);

    /**
     * 修改余额充值订单
     *
     * @param map
     * @return
     */
    @PostMapping("/orderBalanceRecharge/updateBalanceRechargeOrder")
    ResponseResult updateBalanceRechargeOrder(@RequestBody Map<Object, Object> map);

    /**
     * 根据充值单号查询充值单详情
     *
     * @param rechargeOrderId
     * @return
     */
    @PostMapping("/orderBalanceRecharge/queryRechargeOrderById")
    OrderBalanceRechargeVO queryRechargeOrderById(@RequestParam("rechargeOrderId") String rechargeOrderId);


    /**
     * 查询订单信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/order/base/feign/queryOrderInfo")
    Map<String, Object> queryOrderInfo(@RequestParam("orderId") String orderId);


    /**
     * 查询用户信息
     */
    @GetMapping("/order/base/feign/queryBuyUserId")
    Map<String, Object> queryBuyUserId(@RequestParam("orderId") String orderId);

    /**
     * 根据订单号查询退款订单号。
     *
     * @param orderId 原始订单号，用于查询对应的退款订单号。
     * @return 返回退款订单号。
     */
    @GetMapping("/order/return/feign/queryRefundOrderNum")
    String queryRefundOrderNum(@RequestParam("orderId") String orderId);

    /**
     * 更新退款会计期间。
     *
     * @param param 退款会计期间参数DTO
     * @return 更新操作的影响行数
     */
    @PostMapping("/order/base/feign/updateRefundAccountPeriod")
    int updateRefundAccountPeriod(@RequestBody RefundAccountParamDTO param);


    /**
     * 更新账单状态。
     *
     * @param accountingPeriodId 会计期间ID
     * @param accountStatus      账单状态
     */
    @GetMapping("/order/base/feign/editAccountStatus")
    void editAccountStatus(@RequestParam("accountingPeriodId") String accountingPeriodId, @RequestParam("accountStatus") Integer accountStatus);

    /**
     * 查询订单信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/order/base/feign/queryOrderInfoFegin")
    OrderBaseVO queryOrderInfoFegin(@RequestParam("orderId") String orderId);

    /**
     * 查询会计期间ID。
     *
     * @param orderId 订单号
     * @return 会计期间ID
     */
    @GetMapping("/order/base/feign/queryAccountingPeriodId")
    String queryAccountingPeriodId(@RequestParam("orderId") String orderId);


    /**
     * 根据会计期间ID查询账单详情。
     *
     * @param accountingPeriodId 会计期间ID
     * @return 账单详情VO
     */
    @GetMapping("/order/base/feign/selectDetailsByPeriodId")
    AccountShopBillDetailsVO selectDetailsByPeriodId(@RequestParam("accountingPeriodId") String accountingPeriodId);


    /**
     * 查询店铺的退货退款现金金额。
     *
     * @param pojo 销售统计DTO
     * @return 退货退款现金金额
     */
    @PostMapping("/order/return/feign/querShopReturnCash")
    BigDecimal querShopReturnCash(@RequestBody ShopSalesStatisticsDTO pojo);


    /**
     * 查询店铺
     *
     * @param orderId
     * @return
     */
    @GetMapping("/order/base/feign/queryTreasureShopIdByInt")
    String queryTreasureShopIdByInt(@RequestParam("orderId") String orderId);

    /**
     * 根据订单号查询订单信息。
     *
     * @param orderId 订单号，是查询订单的关键标识。
     * @return 返回订单号对应的订单信息。如果找不到匹配的订单，可能返回空字符串。
     */
    @GetMapping("/order/base/feign/queryOrderId")
    String queryOrderId(@RequestParam("orderId") String orderId);

    /**
     * 查询订单号和支付单号
     *
     * @param orderId
     * @return
     */
    @GetMapping("/order/base/feign/queryOrderIdAndPaymentNumber")
    List<Map<String, Object>> queryOrderIdAndPaymentNumber(@RequestParam("orderId") String orderId);

    /**
     * 更新订单结算状态
     *
     * @param list
     * @return
     */
    @PostMapping("/order/settlement/feign/updateOrderSettlementStatus")
    boolean updateOrderSettlementStatus(@RequestBody List<String> list);


    /**
     * 更新结算状态
     *
     * @param list
     * @return
     */
    @PostMapping("/order/settlement/feign/updateSettlementStatus")
    boolean updateSettlementStatus(@RequestBody List<String> list);


    /**
     * 支票结算金额
     *
     * @param shopId
     * @return
     */
    @GetMapping("/order/settlement/feign/checkSettlementAmount")
    boolean checkSettlementAmount(@RequestParam("shopId") String shopId);

    /**
     * 新增怡亚通订单记录
     *
     * @param submitOrderListVO
     * @return
     */
    @PostMapping("/order/base/feign/insertYiYaTongOrder")
    Integer insertYiYaTongOrder(@RequestBody SubmitOrderVO submitOrderListVO);


    /**
     * 判断纯数字订单
     *
     * @param orderId
     * @param paymentNumber
     * @return
     */
    @GetMapping("/order/base/feign/queryAssetInfo")
    List<AssetTmpInfoDTO> queryAssetInfo(@RequestParam("orderId") String orderId, @RequestParam("paymentNumber") String paymentNumber);

    /**
     * 查询买方用户ID
     *
     * @param orderId
     * @return
     */
    @GetMapping("/order/base/feign/queryBuyerUserIdByOrderBase")
    String queryBuyerUserIdByOrderBase(@RequestParam("orderId") String orderId);


    /**
     * 查找订单商品id
     *
     * @param orderId
     * @return
     */
    @GetMapping("/order/base/feign/queryGoodsByOrderId")
    List<WapGoodsSaleSumDTO> queryGoodsByOrderId(@RequestParam("orderId") String orderId);


    /**
     * @return 处理结果
     */
    @PostMapping("/order/base/queryOrderNumExist")
    Integer queryOrderNumExist(@RequestParam("orderId") String orderId);

    /**
     * 将货币符号和汇率存入订单表
     *
     * @param orderId
     * @param currencyCode
     * @param exchangeRate
     * @return
     */
    @PostMapping("/order/base/updateCurrencyAndExchangeRate")
    ResponseResult updateCurrencyAndExchangeRate(@RequestParam("orderId") String orderId, @RequestParam("currencyCode") String currencyCode,
                                                 @RequestParam("exchangeRate") BigDecimal exchangeRate, @RequestParam("currencySymbol") String currencySymbol);

    /**
     * 根据订单查询货币信息
     *
     * @param orderId
     * @return
     */
    @PostMapping("/order/base/queryOrderCurrencyInfo")
    BaseCurrencySymbolVO queryOrderCurrencyInfo(@RequestParam("orderId") String orderId);

    /**
     * 判断京东余额支付足够支付
     *
     * @param orderId
     * @param orderAmount
     * @return
     */
    @GetMapping("/order/base/queryJDBalanceIsEnough")
    ResponseResult queryJDBalanceIsEnough(@RequestParam("orderId") String orderId,
                                          @RequestParam("orderAmount") BigDecimal orderAmount);

    /**
     * @return 处理结果
     */
    @GetMapping("/order/base/getOrderBase")
    ResponseResult<OrderBaseVO> getOrderBase(@RequestParam("orderId") String orderId,
                                             @RequestParam("paymentNumber") String paymentNumber);

    /**
     * @param req 请求
     */
    @PostMapping("/order/base/points/updateOrderStatus")
    void updateOrderStatus(@RequestBody PointsPayReqVO req);
}
