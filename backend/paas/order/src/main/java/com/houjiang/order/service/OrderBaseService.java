package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.dto.goods.StorageInventoryFlowGoodsDTO;
import com.houjiang.commons.dto.job.ExpireMarkIdUpdateDTO;
import com.houjiang.commons.dto.order.PlatFormMemberSelfQueryDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.dto.payment.AssetTmpInfoDTO;
import com.houjiang.commons.dto.payment.RefundAccountParamDTO;
import com.houjiang.commons.dto.shop.ShopUserDTO;
import com.houjiang.commons.dto.vop.OrderGoodsJdVopDTO;
import com.houjiang.commons.dto.yiyatong.ReturnListDTO;
import com.houjiang.commons.dto.yiyatong.ShipmentsListDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.base.BaseCurrencySymbolVO;
import com.houjiang.commons.vo.goods.PurchaseOrderVO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.shop.AppShopBaseSalesInfoVO;
import com.houjiang.commons.vo.shop.ShopUserVO;
import com.houjiang.commons.vo.yiyatong.base.SubmitOrderVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.OrderBaseEntity;
import io.seata.core.exception.TransactionException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderBaseService
 * @Description 订单详细信息
 * @Author Amos
 * @Date 2023-06-06 15:00:49
 * @Version 1.0
 * @author Amos
 */
public interface OrderBaseService extends IService<OrderBaseEntity> {
    /**
     * 支付时查询商品实付金额
     *
     * @param map
     * @return
     */
    ResponseResult queryAmountPaid(Map<String, String> map);

    /**
     * 查询用户已兑换的用户积分数量
     *
     * @param map
     * @return
     */
    ResponseResult queryintegraOrdersCount(Map<String, Object> map);


    /**
     * 查询是否多订单
     *
     * @param map
     * @return
     */
    Integer queryShopIdByOrderIdNum(Map<String, Object> map);

    /**
     * 查询是否多订单
     *
     * @param map
     * @return
     */
    ResponseResult queryShopIdByOrderInfo(Map<String, Object> map);

    /**
     * 查询商品数量
     *
     * @param map
     * @return
     */
    Integer queryGoodNumByOrderId(Map<String, Object> map);

    /**
     * 订单消息
     *
     * @param map
     * @return
     */
    ResponseResult messageToRemind(Map<Object, Object> map);

    /**
     * 判断订单状态已辨用作删除
     *
     * @param map
     * @return
     */
    ResponseResult deleteOrder(Map<String, Object> map);

    /**
     * 取消订单
     *
     * @param map
     * @return
     */
    ResponseResult cancelTheOrder(Map<Object, Object> map) throws TransactionException;

    /**
     * 确认收货
     *
     * @param map
     * @return
     */
    ResponseResult confirmTheGoods(Map<Object, Object> map) throws TransactionException;


    /**
     * 延迟收货
     *
     * @param map
     * @return
     */
    ResponseResult delayToReceiveGoods(Map<Object, Object> map);

    /**
     * 查询已售订单信息(订单详情)
     *
     * @param map
     * @return
     */
    ResponseResult enquireSoldOrders(Map<String, Object> map);

    /**
     * 添加评论
     *
     * @param para
     * @return
     */
    ResponseResult addComments(CommentsListDTO para) throws TransactionException;

    /**
     * 首页订单总数
     *
     * @param map
     * @return
     */
    ResponseResult ordersTotal(Map<Object, Object> map);

    /**
     * 商家app首页订单总数
     *
     * @param map
     * @return
     */
    ResponseResult shopAppOrdersTotal(Map<Object, Object> map);

    /**
     * 销量排名
     *
     * @param map
     * @return
     */
    ResponseResult salesNo(Map<Object, Object> map);


    /**
     * 计算销售额
     *
     * @param map
     * @return
     */
    ResponseResult calculatedSales(Map<Object, Object> map);

    /**
     * 查询虚拟订单未评价订单
     *
     * @param map
     * @return
     */
    ResponseResult queryVirtualOrderByEvaluation(Map<String, Object> map);


    /**
     * 订单信息查询(兑换记录列表)
     *
     * @param map
     * @return
     */
    ResponseResult queryIntegralOrders(Map<String, Object> map);

    /**
     * 查询积分未评价订单
     *
     * @param map
     * @return
     */
    ResponseResult queryIntegralOrderByEvaluation(Map<String, Object> map);

    /**
     * 查询买家订单发票列表
     *
     * @param map
     * @return
     */
    ResponseResult queryOrderInvoiceList(Map<String, Object> map);

    /**
     * 真实订单之卖出交易
     *
     * @param
     * @return
     */
    ResponseResult orderSellingTransaction(SellingInputDTO sellingPara);

    /**
     * 真实订单之卖出交易 导出
     *
     * @param
     * @return
     */
    ResponseResult orderSellingTransactionExcel(SellingInputDTO sellingPara);

    /**
     * 订单之订单详情
     *
     * @param buyingPara
     * @return
     */
    ResponseResult PlatFormBackGroundByOrderDetails(Map<String, Object> buyingPara);

    /**
     * 订单之卖出交易
     *
     * @param
     * @return
     */
    ResponseResult PlatFormBackGroundBySelling(SellingInputDTO sellingPara);

    /**
     * 订单之卖出交易Exp
     *
     * @param
     * @return
     */
    ResponseResult PlatFormBackGroundBySellingExp(SellingInputDTO sellingPara);

    /**
     * 超出时间申请售后拒绝
     *
     * @param paraMap
     * @return
     */
    ResponseResult overTimeReturnOrder(Map<String, String> paraMap) throws ParseException;

    /**
     * 查询物流信息
     *
     * @param map
     * @return
     */
    ResponseResult queryLogistics2(Map<Object, Object> map);

    /**
     * 根据orderId 获取已经上传的支付凭证信息
     *
     * @return
     */
    ResponseResult querkVocherImages(QukVocherImagesDTO param);

    /**
     * 审核 线下订单 (上传凭证)
     *
     * @return
     */
    ResponseResult checkPaymentVocher(QukVocherImagesDTO param);

    /**
     * 判断该订单是否是虚拟订单或者是门店自提订单
     */
    ResponseResult isVirtualOrder(Map<String, String> orderIdIsNum);

    /**
     * 为虚拟订单添加兑换码
     */
    ResponseResult addRedeemCode(Map<String, Object> param);

    /**
     * 下单通知
     */
    ResponseResult orderToInform(Map<String, Object> map);

    /**
     * 获取非数字订单
     */
    ResponseResult queryUpayByOrderId(String order_id);

    /**
     * 更新支付方式、时间等字段
     */
    ResponseResult UpdateOrderBaseByPayment(Map<String, Object> map);

    /**
     * 更新orderBase中支付方式等字段 feign 支付宝
     *
     * @param map
     * @return
     */
    boolean fiveUpdateOrderBaseByPayment(Map<Object, Object> map);

    /**
     * 订单之线下订单 导出
     *
     * @param sellingInputDTO
     * @return
     * @throws ParseException
     */
    ResponseResult PlatFormOfflineOrderEXcel(SellingInputDTO sellingInputDTO) throws ParseException;


    /**
     * 订单之卖出交易
     *
     * @param sellingPara
     * @return
     * @throws ParseException
     */
    ResponseResult PlatFormBackGroundBySellingNew(SellingInputNewDTO sellingPara) throws ParseException, IllegalAccessException;

    /**
     * 订单之卖出交易 导出
     *
     * @param sellingPara
     */
    ResponseResult platFormOnlineOrdersEXcel(SellingInputNewDTO sellingPara);


    /**
     * 根据订单id查订单信息
     *
     * @param OrderId
     * @return
     */
    ResponseResult selectOrderInfoById(String OrderId);

    /**
     * 查询卖家中心订单发票列表
     *
     * @param param
     * @return
     */
    ResponseResult querySellerOrderInvoiceList(Map<String, Object> param);

    /**
     * 已售订单
     *
     * @param map
     * @return
     */
    ResponseResult soldOrders(Map<String, Object> map);

    /**
     * 商家查看全部订单
     *
     * @param map
     * @return
     */
    ResponseResult allOrders(Map<String, Object> map) throws ParseException;


    /**
     * @param map
     * @return
     */
    ResponseResult pseudoDelete(Map<String, Object> map);

    /**
     * 平台每日交易额(可按时间查询)
     *
     * @param map
     * @return
     */
    ResponseResult dailyTurnoverOfThePlatform(Map<String, Object> map);

    /**
     * 平台总交易额(可按时间查询)
     *
     * @param map
     * @return
     */
    ResponseResult totalTurnoverOfThePlatform(Map<String, Object> map);

    /**
     * 店铺排行
     *
     * @param map
     * @return
     */
    ResponseResult storeRanking(Map<String, Object> map);

    /**
     * 订单详情
     *
     * @param map
     * @return
     */
    ResponseResult businessManageByOrderDetails(Map<String, Object> map);

    /**
     *  查询打印的 订单详情
     * @param map
     * @return
     */
    ResponseResult queryPrintOrderDetails(Map<String, Object> map);


    /*
/**
     * 根据红包编码查询订单详情
     */
    ResponseResult getRedPacketOrder(Map<String, String> paraMap);


    /**
     * 订单之自身会员交易
     *
     * @param param
     * @return
     */
    ResponseResult queryPlatFormMemberSelfPard(PlatFormMemberSelfQueryDTO param) throws IllegalAccessException;


    /**
     * 发货
     *
     * @param map
     * @return
     */
    ResponseResult shipments(Map<Object, Object> map) throws TransactionException;


    /**
     * 平台积分商城 兑换列表 删除
     *
     * @param map
     * @return
     */
    ResponseResult deleteIntegralOrder(Map<Object, Object> map);

    /**
     * 物流消息
     *
     * @param map
     * @return
     */
    ResponseResult logisticsInformation(Map<String, Object> map);

    /**
     * 平台查询 交易明细
     *
     * @param param
     * @return
     */
    ResponseResult queryMonthsaleReport(MonthReportDTO param);

    /**
     * 平台查询 交易明细 导出
     *
     * @param param
     * @return
     */
    ResponseResult queryMonthsaleReportExcel(MonthReportDTO param);


    /**
     * 平台财务订单 列表导出
     *
     * @param param
     * @return
     */
    ResponseResult queryRealOrderSelfPardExcel(RealOrderSelfDTO param);


    /**
     * 平台财务订单 列表查询
     *
     * @param param
     * @return
     */
    ResponseResult queryRealOrderSelfPard(RealOrderSelfDTO param) throws IllegalAccessException;

    /**
     * 修改订单信息(店铺名)
     *
     * @param map
     * @return
     */
    ResponseResult updateByMap(Map<String, Object> map);


    /**
     * 查询退保店铺详情
     *
     * @param map
     * @return
     */
    ResponseResult queryReturnBondDetail(Map<String, Object> map);

    /**
     * 判断店铺下是否有未完成订单
     *
     * @param map
     * @return
     */
    ResponseResult updateShopByOrderInfo(Map<String, Object> map);

    /**
     * 平台查询积分订单列表
     *
     * @param param
     * @return
     */
    ResponseResult queryExchangeList(Map<String, Object> param);

    /**
     * 门店订单信息查询
     *
     * @param map
     * @return
     */
    ResponseResult queryGateOrders(Map<String, Object> map);

    /**
     * 门店订单信息查询 查询门配送单
     *
     * @param map
     * @return
     */
    ResponseResult queryGateDeliveryOrders(Map<String, Object> map);

    /**
     * 门店订单信息查询 查询门店配送单 各种状态订单数量
     *
     * @param map
     * @return
     */
    ResponseResult queryGateDeliveryOrdersNum(Map<String, Object> map);

    /**
     * 订单信息查询
     * queryOrders
     *
     * @param map
     * @return
     */
    ResponseResult queryOrders(Map<String, Object> map);

    /**
     * 查询未评价订单
     *
     * @param map
     * @return
     */
    ResponseResult queryOrderByEvaluation(Map<String, Object> map);

    /**
     * 平台查询用户 购买记录
     *
     * @param map
     * @return
     */
    ResponseResult queryMemberUserOrderDetail(Map<String, Object> map);

    /**
     * 查询所有虚拟订单
     *
     * @param map
     * @return
     */
    ResponseResult virtualOrders(Map<String, Object> map);

    /**
     * 查询所有虚拟订单 导出
     * @param map
     * @return
     */
    ResponseResult virtualOrdersExp(Map<String, Object> map);

    /**
     * 门店管理中心自提订单列表
     *
     * @param map
     * @return
     */
    ResponseResult queryGateShopOrders(Map<String, Object> map);

    /**
     * 门店 核销订单根据 兑换码 来 查询订单信息 验证核销码
     *
     * @param map
     * @return
     */
    ResponseResult webGateShopCancelOrder(Map<String, Object> map);

    /**
     * 核销订单
     *
     * @param map
     * @return
     */
    ResponseResult cancelGateShopOrder(Map<String, Object> map);

    /**
     * 门店管理中心自提虚拟订单列表
     *
     * @param map
     * @return
     */
    ResponseResult queryVirtualGateShopOrders(Map<String, Object> map);

    /**
     * 查询已售订单列表
     *
     * @param map
     * @return
     */
    ResponseResult shopGateTakeOrder(Map<String, Object> map);

    /**
     * 查询门店订单 导出
     * @param map
     * @return
     */
    ResponseResult shopGateTakeOrdersExp(Map<String, Object> map);

    /**
     * 根据核销码获取订单信息
     *
     * @param map
     * @return
     */
    ResponseResult queryCancelOrderDetails(Map<String, String> map);

    /**
     * 核销兑换码
     *
     * @param map
     * @return
     */
    ResponseResult cancelOrder(Map<String, Object> map);


    /**
     * 核销订单
     *
     * @param map
     * @return
     */
    ResponseResult businessCancelOrder(Map<String, Object> map) throws TransactionException;


    /**
     * 编辑物流单号
     *
     * @param map
     * @return
     */
    ResponseResult changeShipments(Map<Object, Object> map);

    /**
     * 品牌店铺查询会员订单
     *
     * @param param
     * @return
     */
    ResponseResult queryMemberOrderDetail(Map<String, Object> param);


    /**
     * 分开发货
     *
     * @param param
     * @return
     */
    ResponseResult separateShipments(ShipmentsDTO param) throws TransactionException;


    /**
     * 更改收货人商品信息
     *
     * @param map
     * @return
     */
    ResponseResult updateAddressInformation(Map<Object, Object> map);

    /**
     * 查询收货人地址信息
     *
     * @param map
     * @return
     */
    ResponseResult queryAddressInformation(Map<Object, Object> map);

    /**
     * 批量发货
     *
     * @param file
     * @return
     * @throws TransactionException
     */
    ResponseResult importShipmentsOrderExport(MultipartFile file,String token) throws TransactionException;


    /**
     * 批量发货模板
     *
     * @param map
     * @param response
     * @return
     */
    ResponseResult batchShipmentsOrderExport(Map<String, Object> map, HttpServletResponse response);
    /**
     * 商家仓储批量发货模板
     *
     * @param map
     * @param response
     * @return
     * @throws IOException
     */
    ResponseResult storageBatchShipmentsOrderExport(Map<String, Object> map, HttpServletResponse response);


    /**
     * 根据店铺id获取未发货订单信息，分页
     *
     * @param param
     * @return
     */
    ResponseResult queryUnshippedOrder(RetrunQueryPrintDTO param);


    /**
     * 发货订单导出
     *
     * @param map
     * @param response
     * @return
     */
    ResponseResult shippingOrderExport(Map<String, Object> map, HttpServletResponse response);

    /**
     * 查看回收站下的订单列表的商品详情
     *
     * @param map
     * @return
     */
    ResponseResult queryRecycleBin(Map<String, Object> map);

    /**
     * 检查是否是分开发货
     *
     * @param map
     * @return
     */
    ResponseResult queryLogisticsMessage(Map<String, Object> map) throws Exception;

    /**
     * 提醒用户付款
     *
     * @param map
     * @return
     */
    ResponseResult remindPay(Map<String, Object> map);


    /**
     * 待发货订单
     *
     * @param map
     * @return
     */
    ResponseResult shipmentsOrder(Map<String, Object> map) throws ParseException;

    /**
     * 还原回收站订单
     *
     * @param map
     * @return
     */
    ResponseResult reduction(Map<String, Object> map);


    /**
     * 回收站界面
     *
     * @param map
     * @return
     */
    ResponseResult queryRecycleBinPageDetails(Map<String, Object> map);


    /**
     * 导出回收站界面
     *
     * @param map
     * @return
     */
    ResponseResult recycleBinExcel(Map<String, Object> map);


    /**
     * 查询用户收货信息与物流名称
     *
     * @param map
     * @return
     */
    ResponseResult queryLogisticsName(Map<Object, Object> map);

    /**
     * 修改订单商品价格
     *
     * @param map
     * @return
     */
    ResponseResult changeShoppingPrice(Map<String, Object> map);


    /**
     * webm门店核销记录
     *
     * @return
     */
    ResponseResult cancelShopGateRecord(PageUserTokenBeanDTO page);


    /**
     * 查询某个店铺下 购买虚拟商品的用户
     *
     * @param page
     * @return
     */
    ResponseResult queryVirtualGoodsUser(PageUserTokenBeanDTO page);

    /**
     * 根据shoid 查询已经核销的订单
     *
     * @param page
     * @return
     */
    ResponseResult cancelRecord(PageUserTokenBeanDTO page);


    /**
     * 订单信息查询(PC)
     *
     * @param map
     * @return
     */
    ResponseResult queryOrdersByPC(Map<String, Object> map);
    /**
     * 已售订单导出
     *
     * @param map
     * @return
     */
    ResponseResult soldOrdersExp(Map<String, Object> map);


    /**
     * 校验单号
     */
    ResponseResult checkodd(Map<String, Object> map);


    /**
     * 定时任务fegin-确认收货超过14天默认默认好评
     *
     * @return
     */
    ResponseResult queryNotEvaluatedOrder();

    /**
     * 门店中心  查询门店配送单
     *
     * @param param
     * @return
     */
    ResponseResult gateShopQueryDeliveryOrders(GateQueryDeliveryOrdersDTO param);

    /**
     * 门店中心  查询门店配送单 详情
     *
     * @param param
     * @return
     */
    ResponseResult gateShopQueryDeliveryOrdersDetail(GateQueryDeliveryOrdersDTO param);

    /**
     * 门店中心  商家接单确认
     *
     * @param param
     * @return
     */
    ResponseResult shopAffirm(GateQueryDeliveryOrdersDTO param);

    /**
     * 根据用户id shopId  查询用户总交易额 和 最近消费时间 feign
     * @param shopUserDTO
     * @return
     */
    ShopUserVO queryShopUserTotalExpenditure(ShopUserDTO shopUserDTO);

    /**
     * 查询店铺会员历史订单
     * @param shopUserDTO
     * @return
     */
    ResponseResult queryShopUserHistoricalOrders(ShopUserDTO shopUserDTO);

    /**
     * 查询采购单商品信息
     * @param param 查询参数
     * @return 进货单商品信息
     */
    PurchaseOrderVO queryPurchaseOrderGoods(StorageInventoryFlowGoodsDTO param);

    /**
     * Im查询用户消费金额、下单数量 feign
     *
     * 该方法通过接收一个映射对象，其中包含查询所需的参数，例如用户ID、时间范围等，
     * 并返回一个包含用户消费信息的映射对象查询结果可以包括但不限于用户的总消费额、
     * 消费详情列表等信息具体返回内容取决于查询逻辑和需求
     *
     * @param map 包含查询参数的映射对象，例如用户ID、开始时间、结束时间等
     * @return 包含用户消费信息的映射对象，具体结构根据查询逻辑确定
     */
    Map<String, Object> queryUserConsumption(Map<String, String> map);

    /**
     * 查询用户订单信息
     *
     * 该方法根据提供的参数查询用户的订单信息，并返回查询结果
     * 主要用于处理查询请求，获取用户相关的订单详情
     *
     * @param map 包含查询条件的映射表，键值对为查询条件的字段名和对应的值
     * @return 返回查询结果，包含用户订单信息的响应结果
     */
    ResponseResult imQueryUserOrderInfo(Map<String, Object> map);

    /**
     * 发货
     *
     * @param shipmentsListDTO
     * @return
     */
    ResponseResult yiyatongShipments(ShipmentsListDTO shipmentsListDTO) throws TransactionException;

    /**
     * 怡亚通发货
     *
     * @param returnListDTO
     * @return
     */
    ResponseResult yiyatongReturn(ReturnListDTO returnListDTO) throws TransactionException;


    /**
     * 发货
     *
     * @param map
     * @return
     */
    ResponseResult supplierShipments(Map<String, Object> map) throws TransactionException;

    /**
     * 怡亚通已售订单
     *
     * @param map
     * @return
     */
    ResponseResult yiyatongSoldOrders(Map<String, Object> map);

    /**
     * 怡亚通已售订单导出
     *
     * @param map
     * @return
     */
    ResponseResult yiyatongSoldOrdersExp(Map<String, Object> map);
    /**
     * 供应商 已售订单
     *
     * @param map
     * @return
     */
    ResponseResult soldSupplierOrders(Map<String, Object> map);
    /**
     * 供应商 已售订单 导出
     *
     * @param map
     * @return
     */
    ResponseResult soldSupplierOrdersExp(Map<String, Object> map);

    /**
     * TODO 补充方法说明
     * @param map TODO
     * @return TODO
     */
    ResponseResult shipmentsSupplierOrder(Map<Object, Object> map);

    /**
     * 查询申请开票信息
     *
     * @param params
     * @return
     */
    ResponseResult queInvoiceOrder(queryInvoiceOrderDTO params) throws IllegalAccessException;

    /**
     * TODO 补充方法说明
     * @param params TODO
     * @return TODO
     */
    ResponseResult queryPurchaseOrder(queryPurchaseOrderDTO params) throws IllegalAccessException;

    /**
     * 取消订单
     *
     * @param map
     * @return
     */
    ResponseResult cancelSupplierTheOrder(Map<Object, Object> map);

    /**
     * 查询商品信息带分页
     *
     * @Description: 查询商品信息带分页
     * @Param: [map]
     * @Return:
     */
    ResponseResult queryReturnGoodsPage(Map<String, Object> map);

    /**
     * 商家查询子单
     *
     * @param map
     * @return
     */
    ResponseResult querySonOrders(Map<String, Object> map);


    /**
     * 更新订单的团购状态
     *
     * @param orderIdlist 一个包含多个订单ID的列表，用于指定需要更新团购状态的订单
     */
    void updateOrderGroupbuyStatus(List<String> orderIdlist);


    /**
     * 查询店铺订单数量
     * 根据店铺ID查询该店铺的订单数量
     *
     * @param shopId 店铺ID，用于标识需要查询的店铺
     * @return 店铺的订单数量如果查询结果为空，则返回null
     */
    Integer selectShopOrder(Integer shopId);


    /**
     * 获取未完成开票的订单数量
     *
     * @param param
     * @return
     */
    Integer getUndealedOrderCount( Map<String, Object> param);


    /**
     * 根据日期查询订单金额
     * @param startTimeStr
     * @param endTimeStr
     * @param shopId
     * @return
     */
    BigDecimal queryPayMentByDate( String startTimeStr,  String endTimeStr,  String shopId);


    /**
     * 根据日期查询
     * @param startTimeStr
     * @param endTimeStr
     * @param shopId
     * @return
     */
    Integer queryPayCountByDate( String startTimeStr, String endTimeStr, String shopId);


    /**
     * 根据日期查询订单金额
     * @param shopId
     * @param today
     * @return
     */

    /**
     * TODO 补充方法说明
     * @param shopId TODO
     * @param today TODO
     * @return TODO
     */
    List<AppShopBaseSalesInfoVO> queryShopBasePayPriceByApp(String shopId, String today);




    /**
     * 查询订单信息
     *
     * @param orderId
     * @return
     */
    Map<String, Object> queryOrderInfo(String orderId);


    /**
     * 查询用户信息
     */
    Map<String,Object> queryBuyUserId(String orderId);

    /**
     * 更新退款会计期间。
     *
     * @param param 退款会计期间参数DTO
     * @return 更新操作的影响行数
     */
    int updateRefundAccountPeriod(RefundAccountParamDTO param);


    /**
     * 更新账单状态。
     *
     * @param accountingPeriodId 会计期间ID
     * @param accountStatus 账单状态
     */
    void editAccountStatus(String accountingPeriodId, Integer accountStatus);

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
    String queryAccountingPeriodId(String orderId);


    /**
     * 根据会计期间ID查询账单详情。
     *
     * @param accountingPeriodId 会计期间ID
     * @return 账单详情VO
     */
    AccountShopBillDetailsVO selectDetailsByPeriodId(String accountingPeriodId);


    /**
     * 查询店铺
     *
     * @param orderId
     * @return
     */
    String queryTreasureShopIdByInt(String orderId);

    /**
     * 根据订单号查询订单信息。
     *
     * @param orderId 订单号，是查询订单的关键标识。
     * @return 返回订单号对应的订单信息。如果找不到匹配的订单，可能返回空字符串。
     */
    String queryOrderId(String orderId);

    /**
     *  查询订单号和支付单号
     * @param orderId
     * @return
     */
   List<Map<String, Object>> queryOrderIdAndPaymentNumber(String orderId);


    /**
     * 新增怡亚通订单记录
     * @param submitOrderListVO
     * @return
     */
    Integer insertYiYaTongOrder(SubmitOrderVO submitOrderListVO);


    /**
     * 判断纯数字订单
     *
     * @param orderId
     * @param paymentNumber
     * @return
     */
    List<AssetTmpInfoDTO> queryAssetInfo(String orderId,String paymentNumber);

    /**
     * 查询买方用户ID
     *
     * @param orderId
     * @return
     */
    String queryBuyerUserIdByOrderBase( String orderId);

    /**
     * 查找订单商品id
     * @param orderId
     * @return
     */
    List<WapGoodsSaleSumDTO> queryGoodsByOrderId(String orderId);



    /**
     * 查询订单编号是否已存在
     *
     * @param orderId 订单编号
     * @return 如果订单编号存在，则返回存在次数（大于0表示存在），否则返回null
     */
    Integer queryOrderNumExist(String orderId);
    /**
     * 将货币符号和汇率存入订单表
     * @param orderId
     * @param currencyCode
     * @param exchangeRate
     * @return
     */
    ResponseResult updateCurrencyAndExchangeRate(String orderId, String currencyCode, BigDecimal exchangeRate,String currencySymbol);

    /**
     * 根据订单查询货币信息
     * @param string
     * @return
     */
    BaseCurrencySymbolVO queryOrderCurrencyInfo(String string);

    /**
     * 查看拼团状态
     * @param para
     * @return
     */
    boolean checkGroupBookingStatus(ExpireMarkIdUpdateDTO para);

    /**
     * 保存京东订单号
     * @param orderId
     * @param jingDongOrderId
     * @return
     */
    ResponseResult updateOrderJingDongOrderId(String orderId, String jingDongOrderId);

    /**
     * 根据京东订单号 修改我方订单状态为 已发货
     * @param jdOrderIds
     * @return
     */
    ResponseResult updateOrderStatusByJDOrderId(List<String> jdOrderIds);

    /**
     * 将京东子订单号保存入库中
     * @param orderGoodsJdVopDTOS
     * @return
     */
    ResponseResult updateJongDongSonOrder(List<OrderGoodsJdVopDTO> orderGoodsJdVopDTOS);
    /**
     * 京东已售订单
     *
     * @param map
     * @return
     */
    ResponseResult jingDongSoldOrders(Map<String, Object> map);

    /**
     * 京东已售订单导出
     *
     * @param map
     * @return
     */
    ResponseResult jingDongSoldOrdersExp(Map<String, Object> map);

    /**
     * 判断京东余额支付足够支付
     * @param orderId
     * @param orderAmount
     * @return
     */
    ResponseResult queryJDBalanceIsEnough(String orderId, BigDecimal orderAmount);
}
