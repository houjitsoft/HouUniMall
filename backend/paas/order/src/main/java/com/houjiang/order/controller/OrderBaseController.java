package com.houjiang.order.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.houjiang.commons.annnotation.RepeatClick;
import com.houjiang.commons.annnotation.SensitiveCheck;
import com.houjiang.commons.annnotation.ShopTokenLimit;
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
import com.houjiang.commons.vo.order.PointsPayReqVO;
import com.houjiang.commons.vo.payment.AccountShopBillDetailsVO;
import com.houjiang.commons.vo.shop.AppShopBaseSalesInfoVO;
import com.houjiang.commons.vo.shop.ShopUserVO;
import com.houjiang.commons.vo.yiyatong.base.SubmitOrderVO;
import com.houjiang.order.dto.*;
import com.houjiang.order.entity.OrderBaseEntity;
import com.houjiang.order.entity.OrderGoodsEntity;
import com.houjiang.order.utils.interfacerepeatedcalls.AccessLimit;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderBaseController
 * @Description 订单详细信息
 * @Author Amos
 * @Date 2023-06-06 15:00:48
 * @Version 1.0
 **/
@RestController
@RequestMapping("order/base")
public class OrderBaseController extends BaseController {
    /**
     * 支付时查询商品实付金额
     *
     * @param map
     * @return
     */
    @PostMapping("/queryAmountPaid")
    public ResponseResult queryAmountPaid(@RequestBody Map<String, String> map) {
        ResponseResult result = orderBaseService.queryAmountPaid(map);
        return result;
    }

    /**
     * 查询用户已兑换的用户积分数量
     */
    @PostMapping("/queryIntegraOrdersCount")
    public ResponseResult queryIntegraOrdersCount(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryintegraOrdersCount(map);
        return result;
    }

    /**
     * 查询是否多订单 fegin
     *
     * @param map
     * @return
     */
    @PostMapping("/queryShopIdByOrderIdNum")
    public Integer queryShopIdByOrderIdNum(@RequestBody Map<String, Object> map) {
        Integer integer = orderBaseService.queryShopIdByOrderIdNum(map);
        return integer;
    }

    /**
     * 查询店铺信息 fegin
     *
     * @param map
     * @return
     */
    @PostMapping("/queryShopIdByOrderInfo")
    public ResponseResult queryShopIdByOrderInfo(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryShopIdByOrderInfo(map);
        return result;
    }

    /**
     * 查询商品数量 fegin
     *
     * @param map
     * @return
     */
    @PostMapping("/queryShopIdByOrderIdCount")
    public Integer queryGoodNumByOrderId(@RequestBody Map<String, Object> map) {
        Integer integer = orderBaseService.queryGoodNumByOrderId(map);
        return integer;
    }

    /**
     * 订单消息
     *
     * @param map
     * @return
     */
    @PostMapping("/messageToRemind")
    public ResponseResult messageToRemind(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.messageToRemind(map);
        return result;
    }

    /**
     * 判断订单状态已辨用作删除
     *
     * @param map
     * @return
     */
    @PostMapping("/deleteOrder")
    public ResponseResult deleteOrder(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.deleteOrder(map);
        return result;
    }

    /**
     * 取消订单
     *
     * @param map
     * @return
     */
    @PostMapping("/cancelTheOrder")
    public ResponseResult cancelTheOrder(@RequestBody Map<Object, Object> map) throws TransactionException {
        ResponseResult result = orderBaseService.cancelTheOrder(map);
        return result;
    }

    /**
     * 确认收货
     *
     * @param map
     * @return
     */
    @RepeatClick
    @PostMapping("/confirmTheGoods")
    public ResponseResult confirmTheGoods(@RequestBody Map<Object, Object> map) throws TransactionException {
        ResponseResult result = orderBaseService.confirmTheGoods(map);
        return result;
    }

    /**
     * 延迟收货
     *
     * @param map
     * @return
     */
    @PostMapping("/delayToReceiveGoods")
    public ResponseResult delayToReceiveGoods(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.delayToReceiveGoods(map);
        return result;
    }

    /**
     * 查询已售订单信息(订单详情)
     *
     * @param map
     * @return
     */
    @PostMapping("/enquireSoldOrders")
    public ResponseResult enquireSoldOrders(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.enquireSoldOrders(map);
        return result;
    }

    /**
     * 添加评论
     *
     * @param para
     * @return
     */
    @RepeatClick(expireSeconds = 10)
    @SensitiveCheck(field = {"goodsList.feedback"})
    @PostMapping("/addComments")
    public ResponseResult addComments(@RequestBody CommentsListDTO para) throws TransactionException {
        ResponseResult result = orderBaseService.addComments(para);
        return result;
    }

    /**
     * 首页订单总数
     *
     * @param map
     * @return
     */
    @PostMapping("/ordersTotal")
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult ordersTotal(@RequestBody Map<Object, Object> map) {
        ResponseResult rr = orderBaseService.ordersTotal(map);
        return rr;
    }

    /**
     * 商家app首页订单总数
     *
     * @param map
     * @return
     */
    @ShopTokenLimit
    @PostMapping("/shopApp/ordersTotal")
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult shopAppOrdersTotal(@RequestBody Map<Object, Object> map) {
        ResponseResult rr = orderBaseService.shopAppOrdersTotal(map);
        return rr;
    }

    /**
     * 销量排名
     *
     * @param map
     * @return
     */
    @PostMapping("/salesNo")
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult salesNo(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.salesNo(map);
        return result;
    }

    /**
     * 计算销售额
     *
     * @param map
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @PostMapping("/calculatedSales")
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult calculatedSales(@RequestBody Map<Object, Object> map)
            throws JsonParseException, JsonMappingException, IOException {
        ResponseResult rr = orderBaseService.calculatedSales(map);
        return rr;
    }

    /**
     * 查询虚拟订单未评价订单
     *
     * @param map
     * @return
     */
    @PostMapping("/queryVirtualOrderByEvaluation")
    public ResponseResult queryVirtualOrderByEvaluation(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryVirtualOrderByEvaluation(map);
        return result;
    }

    /**
     * 订单信息查询(兑换记录列表)
     *
     * @param map
     * @return
     */
    @PostMapping("/queryIntegralOrders")
    public ResponseResult queryIntegralOrders(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryIntegralOrders(map);
        return result;
    }

    /**
     * 查询积分未评价订单
     *
     * @param map
     * @return
     */
    @PostMapping("/queryIntegralOrderByEvaluation")
    public ResponseResult queryIntegralOrderByEvaluation(@RequestBody Map<String, Object> map) {
        ResponseResult rr = orderBaseService.queryIntegralOrderByEvaluation(map);
        return rr;
    }

    /**
     * 查询买家订单发票列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/query/list")
    public ResponseResult queryOrderInvoiceList(@RequestBody Map<String, Object> param) {
        ResponseResult result = orderBaseService.queryOrderInvoiceList(param);
        return result;
    }

    /**
     * 订单之卖出交易(真实订单)
     *
     * @param
     * @return
     * @throws ParseException
     */
    @PostMapping("/platform/orderDetails")
    public ResponseResult orderSellingTransaction(@RequestBody SellingInputDTO sellingPara) {
        ResponseResult result = orderBaseService.orderSellingTransaction(sellingPara);
        return result;
    }

    /**
     * 订单之卖出交易(真实订单)导出
     *
     * @param
     * @return
     * @throws ParseException
     */
    @PostMapping("/platform/orderDetailsExcel")
    public ResponseResult orderSellingTransactionExcel(@RequestBody SellingInputDTO sellingPara) {
        ResponseResult result = orderBaseService.orderSellingTransactionExcel(sellingPara);
        return result;
    }

    /**
     * 订单之订单详情
     *
     * @param
     * @return
     */
    @PostMapping("/platForm/backGroundByOrderDetails")
    public ResponseResult PlatFormBackGroundByOrderDetails(@RequestBody Map<String, Object> buyingPara) {
        ResponseResult result = orderBaseService.PlatFormBackGroundByOrderDetails(buyingPara);
        return result;
    }

    /**
     * 订单之线下订单
     *
     * @param sellingPara
     * @return
     * @throws ParseException
     */
    @PostMapping("/platForm/backGroundBySelling")
    public ResponseResult PlatFormBackGroundBySelling(@RequestBody SellingInputDTO sellingPara) {
        ResponseResult result = orderBaseService.PlatFormBackGroundBySelling(sellingPara);
        return result;
    }

    /**
     * 订单之线下订单 excel导出
     *
     * @param para
     * @return
     */
    @PostMapping("/platForm/backGroundBySellingExp")
    public ResponseResult platFormBackGroundBySellingExp(@RequestBody SellingInputDTO para) {
        ResponseResult rr = orderBaseService.PlatFormBackGroundBySellingExp(para);
        return rr;
    }

    /**
     * 根据orderId 获取已经上传的支付凭证信息
     *
     * @return
     */
    @PostMapping("/platform/queryPaymentDetail")
    public ResponseResult querkVocherImages(@RequestBody QukVocherImagesDTO param) {
        ResponseResult result = orderBaseService.querkVocherImages(param);
        return result;
    }

    /**
     * 审核 线下订单 (上传凭证)
     *
     * @return
     */
    @PostMapping("/platform/updatePaymentDetail")
    public ResponseResult checkPaymentVocher(@RequestBody QukVocherImagesDTO param) {
        ResponseResult result = orderBaseService.checkPaymentVocher(param);
        return result;
    }

    /**
     * 判断该订单是否是虚拟订单或者是门店自提订单
     */
    @PostMapping("/isVirtualOrder")
    public ResponseResult isVirtualOrder(@RequestBody Map<String, String> orderIdIsNum) {
        ResponseResult virtualOrder = orderBaseService.isVirtualOrder(orderIdIsNum);
        return virtualOrder;
    }

    /**
     * 为虚拟订单添加兑换码
     */
    @PostMapping("/addRedeemCode")
    public ResponseResult addRedeemCode(@RequestBody Map<String, Object> param) {
        ResponseResult result = orderBaseService.addRedeemCode(param);
        return result;
    }

    /**
     * 下单通知
     *
     * @param map
     * @return
     */
    @PostMapping("/orderToInform")
    public ResponseResult orderToInform(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.orderToInform(map);
        return result;
    }

    /**
     * 获取非数字订单
     */
    @PostMapping("/queryUpayByOrderId")
    public ResponseResult queryUpayByOrderId(@RequestBody String order_id) {
        ResponseResult result = orderBaseService.queryUpayByOrderId(order_id);
        return result;
    }

    /**
     * 更新orderBase中支付方式等字段
     *
     * @param map
     * @return
     */
    @PostMapping("/updateByPaymentType")
    public ResponseResult UpdateOrderBaseByPayment(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.UpdateOrderBaseByPayment(map);
        return result;
    }

    /**
     * 更新orderBase中支付方式等字段 feign 支付宝
     *
     * @param map
     * @return
     */
    @PostMapping("/updateByPaymentTypeFegin")
    public boolean fiveUpdateOrderBaseByPayment(@RequestBody Map<Object, Object> map) {
        boolean b = orderBaseService.fiveUpdateOrderBaseByPayment(map);
        return b;
    }

    /**
     * 超出时间申请售后拒绝
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/timeOut")
    public ResponseResult overTimeReturnOrder(@RequestBody Map<String, String> paraMap) throws ParseException {
        ResponseResult result = orderBaseService.overTimeReturnOrder(paraMap);
        return result;
    }

    /**
     * 多单号查询物流信息
     *
     * @param map
     * @return
     */
    @PostMapping("/queryLogistics2")
    public ResponseResult queryLogistics2(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.queryLogistics2(map);
        return result;
    }

    /**
     * 订单之线下订单 导出
     *
     * @param sellingInputDTO
     * @return
     * @throws ParseException
     */
    @PostMapping("/platform/offlineOrderEXcel")
    public ResponseResult PlatFormOfflineOrderEXcel(@RequestBody SellingInputDTO sellingInputDTO) throws ParseException {
        ResponseResult result = orderBaseService.PlatFormOfflineOrderEXcel(sellingInputDTO);
        return result;
    }

    /**
     * 订单之卖出交易
     *
     * @param sellingPara
     * @return
     * @throws ParseException
     */
    @PostMapping("/platForm/selling")
    public ResponseResult PlatFormBackGroundBySellingNew(@RequestBody SellingInputNewDTO sellingPara) throws ParseException, IllegalAccessException {
        ResponseResult result = orderBaseService.PlatFormBackGroundBySellingNew(sellingPara);
        return result;
    }

    /**
     * 订单之卖出交易 导出
     *
     * @param sellingPara
     * @return
     * @throws ParseException
     */
    @PostMapping("/platForm/sellEXcel")
    public ResponseResult platFormOnlineOrdersEXcel(@RequestBody SellingInputNewDTO sellingPara) throws ParseException {
        ResponseResult result = orderBaseService.platFormOnlineOrdersEXcel(sellingPara);
        return result;
    }

    /**
     * 根据订单id查订单信息
     *
     * @param OrderId
     * @return
     */
    @GetMapping("/selectOrderInfoById")
    public ResponseResult selectOrderInfoById(@RequestParam("OrderId") String OrderId) {
        ResponseResult result = orderBaseService.selectOrderInfoById(OrderId);
        return result;
    }

    /**
     * 查询卖家中心订单发票列表
     *
     * @param param Map<Object,Object>
     * @return ResultResp
     */
    @PostMapping("/seller/query/list")
    public ResponseResult querySellerOrderInvoiceList(@RequestBody Map<String, Object> param) {
        ResponseResult result = orderBaseService.querySellerOrderInvoiceList(param);
        return result;
    }

    /**
     * 已售订单
     *
     * @param map
     * @return
     */
    @PostMapping("/soldOrders")
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult soldOrders(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.soldOrders(map);
        return result;
    }

    /**
     * 商家查看全部订单
     *
     * @param map
     * @return
     */
    @ShopTokenLimit
    @PostMapping("/allOrders")
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult allOrders(@RequestBody Map<String, Object> map) throws ParseException {
        ResponseResult result = orderBaseService.allOrders(map);
        return result;
    }

    /**
     * 伪删除
     *
     * @param map
     * @return
     */
    @PostMapping("/pseudoDelete")
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult pseudoDelete(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.pseudoDelete(map);
        return result;
    }

    /**
     * 平台每日交易额(可按时间查询)
     *
     * @param map
     * @return
     */
    @PostMapping("/platform/tradeByDaily")
    public ResponseResult dailyTurnoverOfThePlatform(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.dailyTurnoverOfThePlatform(map);
        return result;
    }

    /**
     * 平台总交易额(可按时间查询)
     *
     * @param map
     * @return
     */
    @PostMapping("/platform/tradeByTotal")
    public ResponseResult totalTurnoverOfThePlatform(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.totalTurnoverOfThePlatform(map);
        return result;
    }

    /**
     * 店铺排行
     *
     * @param map
     * @return
     */
    @PostMapping("/storeRanking")
    public ResponseResult storeRanking(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.storeRanking(map);
        return result;
    }

    /**
     * 订单详情
     *
     * @param map
     * @return
     */
    @PostMapping("/business/orderDetails")
    /** 防止恶意请求 */
    @AccessLimit()
    public ResponseResult businessManageByOrderDetails(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.businessManageByOrderDetails(map);
        return result;
    }

    /**
     * 查询打印的 订单详情
     *
     * @param map
     * @return
     */
    @PostMapping("/query/printOrderDetails")
    public ResponseResult queryPrintOrderDetails(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryPrintOrderDetails(map);
        return result;
    }

    /**
     * 根据红包编码查询订单详情
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/redpacket/orderDetails")
    public ResponseResult getRedPacketOrder(@RequestBody Map<String, String> paraMap) {
        ResponseResult result = orderBaseService.getRedPacketOrder(paraMap);
        return result;

    }

    /**
     * 订单之自身会员交易
     *
     * @param param
     * @return
     */
    @PostMapping("/query/memberOrderByPage")
    public ResponseResult queryPlatFormMemberSelfPard(@RequestBody PlatFormMemberSelfQueryDTO param) throws IllegalAccessException {
        ResponseResult result = orderBaseService.queryPlatFormMemberSelfPard(param);
        return result;
    }

    /**
     * 商品发货
     *
     * @param map
     * @return
     */
    @GlobalTransactional
    @PostMapping("/shipments")
    public ResponseResult shipments(@RequestBody Map<Object, Object> map) throws TransactionException {
        ResponseResult result = orderBaseService.shipments(map);
        return result;
    }

    /**
     * 平台积分商城 兑换列表 删除
     *
     * @param map
     * @return
     */
    @PostMapping("/deleteIntegralOrder")
    public ResponseResult deleteIntegralOrder(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.deleteIntegralOrder(map);
        return result;
    }

    /**
     * 物流消息
     *
     * @return
     */
    @PostMapping("/logisticsInformation")
    public ResponseResult logisticsInformation(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.logisticsInformation(map);
        return result;
    }

    /**
     * 平台查询 交易明细
     *
     * @param param
     * @return
     */
    @PostMapping("/queryMonthsaleReport")
    public ResponseResult queryMonthsaleReport(@RequestBody MonthReportDTO param) {
        ResponseResult result = orderBaseService.queryMonthsaleReport(param);
        return result;
    }

    /**
     * 平台查询 交易明细 导出
     *
     * @param param
     * @return
     */
    @PostMapping("/queryMonthsaleReportExcel")
    public ResponseResult queryMonthsaleReportExcel(@RequestBody MonthReportDTO param) {
        ResponseResult result = orderBaseService.queryMonthsaleReportExcel(param);
        return result;
    }

    /**
     * 平台财务订单 列表导出
     *
     * @param param
     * @return
     */
    @PostMapping("/queryRealOrderSelfPardExcel")
    public ResponseResult queryRealOrderSelfPardExcel(@RequestBody RealOrderSelfDTO param) {
        ResponseResult result = orderBaseService.queryRealOrderSelfPardExcel(param);
        return result;
    }

    /**
     * 平台财务订单 列表查询
     *
     * @param param
     * @return
     */
    @PostMapping("/queryRealOrderSelfPard")
    public ResponseResult queryRealOrderSelfPard(@RequestBody RealOrderSelfDTO param) throws IllegalAccessException {
        ResponseResult result = orderBaseService.queryRealOrderSelfPard(param);
        return result;
    }

    /**
     * 修改订单信息(店铺名)
     *
     * @param map
     * @return
     */
    @PostMapping("/updateByMap")
    public ResponseResult updateByMap(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.updateByMap(map);
        return result;
    }

    /**
     * 查询退保店铺详情
     *
     * @param map
     * @return
     */
    @PostMapping("/query/returnBond/detail")
    public ResponseResult queryReturnBondDetail(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryReturnBondDetail(map);
        return result;
    }

    /**
     * 判断店铺下是否有未完成订单
     *
     * @param map
     * @return
     */
    @PostMapping("/updateShopByOrderInfo")
    public ResponseResult updateShopByOrderInfo(@RequestBody Map<String, Object> map) {
        ResponseResult resultResp = orderBaseService.updateShopByOrderInfo(map);
        return resultResp;
    }

    /**
     * 平台查询积分订单列表
     *
     * @param map
     * @return
     */
    @PostMapping("/queryExchangeList")
    public ResponseResult queryExchangeList(@RequestBody Map<String, Object> map) {
        ResponseResult resultResp = orderBaseService.queryExchangeList(map);
        return resultResp;
    }

    /**
     * 门店订单信息查询 查询门店自提单
     *
     * @param map
     * @return
     */
    @PostMapping("queryGateOrders")
    public ResponseResult queryGateOrders(@RequestBody Map<String, Object> map) {
        ResponseResult resultResp = orderBaseService.queryGateOrders(map);
        return resultResp;
    }

    /**
     * 门店订单信息查询 查询门店配送单
     *
     * @param map
     * @return
     */
    @PostMapping("queryGateDeliveryOrders")
    public ResponseResult queryGateDeliveryOrders(@RequestBody Map<String, Object> map) {
        ResponseResult resultResp = orderBaseService.queryGateDeliveryOrders(map);
        return resultResp;
    }

    /**
     * 门店订单信息查询 查询门店配送单 各种状态订单数量
     *
     * @param map
     * @return
     */
    @PostMapping("queryGateDeliveryOrdersNum")
    public ResponseResult queryGateDeliveryOrdersNum(@RequestBody Map<String, Object> map) {
        ResponseResult resultResp = orderBaseService.queryGateDeliveryOrdersNum(map);
        return resultResp;
    }

    /**
     * 平台查询用户 购买记录
     *
     * @param map
     * @return
     */
    @PostMapping("/platform/purchaseRecord")
    public ResponseResult queryMemberUserOrderDetail(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryMemberUserOrderDetail(map);
        return result;
    }

    /**
     * 订单信息查询
     *
     * @param map
     * @return
     */
    @PostMapping("/queryOrders")
    public ResponseResult queryOrders(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryOrders(map);
        return result;
    }

    /**
     * 查询未评价订单
     *
     * @param map
     * @return
     */
    @PostMapping("/queryOrderByEvaluation")
    public ResponseResult queryOrderByEvaluation(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryOrderByEvaluation(map);
        return result;
    }

    /**
     * 查询所有虚拟订单
     *
     * @param map
     * @return
     */
    @PostMapping("/virtualOrders")
    public ResponseResult virtualOrders(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.virtualOrders(map);
        return result;
    }

    /**
     * 查询所有虚拟订单 导出
     *
     * @param map
     * @return
     */
    @PostMapping("/virtualOrdersExp")
    public ResponseResult virtualOrdersExp(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.virtualOrdersExp(map);
        return result;
    }

    /**
     * 门店管理中心自提订单列表
     *
     * @param map
     * @return
     */
    @PostMapping("/queryGateShopOrders")
    public ResponseResult queryGateShopOrders(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryGateShopOrders(map);
        return result;
    }

    /**
     * web门店 核销订单根据 兑换码 来 查询订单信息
     *
     * @param map
     * @return
     */
    @PostMapping("/query/cancelOrder")
    public ResponseResult webGateShopCancelOrder(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.webGateShopCancelOrder(map);
        return result;
    }

    /**
     * 核销门店订单
     *
     * @param map
     * @return
     */
    @PostMapping("/cancelGateShopOrder")
    public ResponseResult cancelGateShopOrder(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.cancelGateShopOrder(map);
        return result;
    }

    /**
     * @throws
     * @title virtualOrders
     * @description 门店管理中心自提虚拟订单列表
     * @author Amos
     * @updateTime 2021/7/22 11:14
     */
    @PostMapping("/virtualGateShopOrders")
    public ResponseResult queryVirtualGateShopOrders(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryVirtualGateShopOrders(map);
        return result;
    }

    /**
     * 查询门店虚拟订单
     *
     * @param map
     * @return
     */
    @PostMapping("/shopGateTakeOrders")
    public ResponseResult shopGateTakeOrders(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.shopGateTakeOrder(map);
        return result;
    }

    /**
     * 查询门店订单 导出
     *
     * @param map
     * @return
     */
    @PostMapping("/shopGateTakeOrdersExp")
    public ResponseResult shopGateTakeOrdersExp(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.shopGateTakeOrdersExp(map);
        return result;
    }

    /**
     * web核销订单根据兑换码来查询订单信息
     *
     * @param map
     * @return
     */
    @PostMapping("/webCancelOrder")
    public ResponseResult webCancelOrder(@RequestBody Map<String, String> map) {
        ResponseResult result = orderBaseService.queryCancelOrderDetails(map);
        return result;
    }

    /**
     * 核销兑换码
     *
     * @param map
     * @return
     */
    @PostMapping("/cancelOrder")
    public ResponseResult cancelOrder(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.cancelOrder(map);
        return result;
    }


    /**
     * 核销订单
     *
     * @param map
     * @return
     */

    @PostMapping("/business/cancelOrder")
    public ResponseResult businessCancelOrder(@RequestBody Map<String, Object> map) throws TransactionException {
        ResponseResult result = orderBaseService.businessCancelOrder(map);
        return result;
    }


    /**
     * 更改物流公司与物流编号
     *
     * @param map
     * @return
     */
    @PostMapping("/changeShipments")
    public ResponseResult changeShipments(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.changeShipments(map);
        return result;
    }

    /**
     * 品牌店铺查询会员订单
     *
     * @param param
     * @return
     */
    @PostMapping("/query/memberOrderDetail")
    public ResponseResult queryMemberOrderDetail(@RequestBody Map<String, Object> param) {
        ResponseResult result = orderBaseService.queryMemberOrderDetail(param);
        return result;
    }

    /**
     * 分开发货
     *
     * @param param
     * @return
     */
    @GlobalTransactional
    @PostMapping("/separateShipments")
    public ResponseResult separateShipments(@RequestBody ShipmentsDTO param) throws TransactionException {
        ResponseResult result = orderBaseService.separateShipments(param);
        return result;
    }

    /**
     * 更改收货人商品信息
     *
     * @param map
     * @return
     */
    @PostMapping("/update/addressInformation")
    public ResponseResult updateAddressInformation(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.updateAddressInformation(map);
        return result;
    }

    /**
     * 查询收货人地址信息
     *
     * @param map
     * @return
     */
    @PostMapping("/query/addressInformation")
    public ResponseResult queryAddressInformation(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.queryAddressInformation(map);
        return result;
    }

    /**
     * 导出批量发货
     *
     * @param file
     * @return
     */
    @GlobalTransactional
    @PostMapping("/importShipmentsOrderExport")
    public ResponseResult importShipmentsOrderExport(@RequestParam("file") MultipartFile file, @RequestParam("token") String token) throws TransactionException {
        ResponseResult result = orderBaseService.importShipmentsOrderExport(file, token);
        return result;
    }

    /**
     * 批量发货模板
     *
     * @param map
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("/batchShipmentsOrderExport")
    public ResponseResult batchShipmentsOrderExport(@RequestBody Map<String, Object> map, HttpServletResponse response) {
        ResponseResult result = orderBaseService.batchShipmentsOrderExport(map, response);
        return result;
    }

    /**
     * 商家仓储批量发货模板
     *
     * @param map
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("/storageBatchShipmentsOrderExport")
    public ResponseResult storageBatchShipmentsOrderExport(@RequestBody Map<String, Object> map, HttpServletResponse response) {
        ResponseResult result = orderBaseService.storageBatchShipmentsOrderExport(map, response);
        return result;
    }

    /**
     * 根据店铺id获取未发货订单信息，分页
     *
     * @param param
     * @return
     * @throws IOException
     */
    @PostMapping("/queryUnshippedOrder")
    public ResponseResult queryUnshippedOrder(@RequestBody RetrunQueryPrintDTO param) {
        ResponseResult result = orderBaseService.queryUnshippedOrder(param);
        return result;
    }

    /**
     * 发货订单导出
     *
     * @param map
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping("/shippingOrderExport")
    public ResponseResult shippingOrderExport(@RequestBody Map<String, Object> map, HttpServletResponse response) {
        ResponseResult result = orderBaseService.shippingOrderExport(map, response);
        return result;
    }

    /**
     * 订单回收站下拉
     *
     * @param map
     * @return
     */
    @PostMapping("/query/recycleBin")
    public ResponseResult queryRecycleBin(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryRecycleBin(map);
        return result;
    }

    /**
     * 查询物流
     *
     * @param map
     * @return
     */
    @PostMapping("/queryLogisticsMessage")
    public ResponseResult queryLogisticsMessage(@RequestBody Map<String, Object> map) throws Exception {
        ResponseResult result = orderBaseService.queryLogisticsMessage(map);
        return result;
    }

    /**
     * 商家提醒付款
     *
     * @param map
     * @return
     */
    @PostMapping("/remindPay")
    public ResponseResult remindPay(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.remindPay(map);
        return result;
    }

    /**
     * 待发货订单
     *
     * @param map
     * @return
     */
    @PostMapping("/shipmentsOrder")
    public ResponseResult shipmentsOrder(@RequestBody Map<String, Object> map) throws ParseException {
        ResponseResult result = orderBaseService.shipmentsOrder(map);
        return result;
    }

    /**
     * 还原回收站订单
     *
     * @param map
     * @return
     */
    @GlobalTransactional
    @PostMapping("/reduction")
    public ResponseResult reduction(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.reduction(map);
        return result;
    }

    /**
     * 回收站界面
     *
     * @param map
     * @return
     */
    @PostMapping("/query/recycleBinPageDetails")
    public ResponseResult queryRecycleBinPageDetails(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryRecycleBinPageDetails(map);
        return result;
    }

    /**
     * 导出回收站界面
     *
     * @param map
     * @return
     */
    @PostMapping("/recycleBinExcel")
    public ResponseResult recycleBinExcel(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.recycleBinExcel(map);
        return result;
    }

    /**
     * 查询用户收货信息与物流名称
     *
     * @param map
     * @return
     */
    @PostMapping("/queryLogisticsName")
    public ResponseResult queryLogisticsName(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.queryLogisticsName(map);
        return result;
    }

    /**
     * 待付款订单改价
     *
     * @param map
     * @return
     */
    @PostMapping("/changeShoppingPrice")
    public ResponseResult changeShoppingPrice(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.changeShoppingPrice(map);
        return result;
    }


    /**
     * webm门店核销记录
     *
     * @return
     */
    @PostMapping("/cancelShopGateRecord")
    public ResponseResult cancelShopGateRecord(@RequestBody PageUserTokenBeanDTO page) {
        ResponseResult result = orderBaseService.cancelShopGateRecord(page);
        return result;
    }

    /**
     * 查询某个店铺下 购买虚拟商品的用户
     *
     * @param page
     * @return
     */
    @PostMapping("/queryVirtualGoodsUser")
    public ResponseResult queryVirtualGoodsUser(@RequestBody PageUserTokenBeanDTO page) {
        ResponseResult result = orderBaseService.queryVirtualGoodsUser(page);
        return result;
    }


    /**
     * 根据shoid 查询已经核销的订单
     *
     * @param page
     * @return
     */
    @PostMapping("/cancelRecord")
    public ResponseResult cancelRecord(@RequestBody PageUserTokenBeanDTO page) {
        ResponseResult result = orderBaseService.cancelRecord(page);
        return result;
    }

    /**
     * 订单信息查询(PC)
     *
     * @param map
     * @return
     */
    @PostMapping("/queryOrdersByPC")
    public ResponseResult queryOrdersByPC(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.queryOrdersByPC(map);
        return result;
    }

    /**
     * 已售订单导出
     *
     * @param map
     * @return
     */
    @PostMapping("/soldOrdersExp")
    public ResponseResult soldOrdersExp(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.soldOrdersExp(map);
        return result;
    }

    /**
     * 校验单号
     */
    @PostMapping("/checkodd")
    public ResponseResult checkodd(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderBaseService.checkodd(map);
        return result;
    }

    /**
     * 定时任务fegin-确认收货超过14天默认默认好评
     *
     * @return
     */
    @PostMapping("/job/queryNotEvaluatedOrder")
    public ResponseResult queryNotEvaluatedOrder() {
        ResponseResult resultResp = orderBaseService.queryNotEvaluatedOrder();
        return resultResp;
    }

    /**
     * 门店中心  查询门店配送单
     *
     * @param param
     * @return
     */
    @PostMapping("/gateShop/queryDeliveryOrders")
    public ResponseResult gateShopQueryDeliveryOrders(@RequestBody GateQueryDeliveryOrdersDTO param) {
        ResponseResult resultResp = orderBaseService.gateShopQueryDeliveryOrders(param);
        return resultResp;
    }

    /**
     * 门店中心  查询门店配送单 详情
     *
     * @param param
     * @returnD
     */
    @PostMapping("/gateShop/queryDeliveryOrdersDetail")
    public ResponseResult gateShopQueryDeliveryOrdersDetail(@RequestBody GateQueryDeliveryOrdersDTO param) {
        ResponseResult resultResp = orderBaseService.gateShopQueryDeliveryOrdersDetail(param);
        return resultResp;
    }

    /**
     * 门店中心  商家接单确认
     */
    @PostMapping("/gateShop/shopAffirm/")
    public ResponseResult shopAffirm(@RequestBody GateQueryDeliveryOrdersDTO param) {
        ResponseResult resultResp = orderBaseService.shopAffirm(param);
        return resultResp;
    }

    /**
     * 根据用户id shopId  查询用户总交易额 和 最近消费时间 feign
     *
     * @param shopUserDTO
     * @return
     */
    @PostMapping("/queryShopUser/totalExpenditure")
    public ShopUserVO queryShopUserTotalExpenditure(@RequestBody ShopUserDTO shopUserDTO) {
        ShopUserVO shopUserVO = orderBaseService.queryShopUserTotalExpenditure(shopUserDTO);
        return shopUserVO;
    }

    /**
     * 查询店铺会员历史订单
     *
     * @param shopUserDTO
     * @return
     */
    @PostMapping("/queryShopUser/historicalOrders")
    public ResponseResult queryShopUserHistoricalOrders(@RequestBody ShopUserDTO shopUserDTO) {
        ResponseResult responseResult = orderBaseService.queryShopUserHistoricalOrders(shopUserDTO);
        return responseResult;
    }

    /**
     * 查询采购单商品信息
     *
     * @param param 查询参数
     * @return 进货单商品信息
     */
    @PostMapping("/queryPurchaseOrderGoods")
    public PurchaseOrderVO queryPurchaseOrderGoods(@RequestBody StorageInventoryFlowGoodsDTO param) {
        return orderBaseService.queryPurchaseOrderGoods(param);
    }

    /**
     * Im查询用户消费金额、下单数量 feign
     * <p>
     * 该方法通过POST请求接收用户相关的参数，以查询用户的消费金额和下单数量
     * 主要用于即时通讯(IM)系统中，为用户提供消费和订单的概览信息
     *
     * @param map 包含查询条件的键值对，如用户ID等信息，用于指定查询范围
     * @return 返回包含用户消费金额和下单数量的ResponseResult对象
     * 包含具体的查询结果和状态信息
     */
    @PostMapping("/queryUserConsumption")
    public Map<String, Object> queryUserConsumption(@RequestBody Map<String, String> map) {
        Map<String, Object> resultResp = orderBaseService.queryUserConsumption(map);
        return resultResp;
    }

    /**
     * 查询用户订单信息
     * <p>
     * 该方法通过POST请求接收一个JSON对象，其中包含查询用户订单信息所需的参数
     * 参数以键值对的形式存在，具体参数名称和含义取决于业务逻辑
     *
     * @param map 包含查询参数的键值对，参数可能包括用户ID、订单号、时间范围等
     * @return 返回一个ResponseResult对象，其中包含查询结果和相关的状态信息
     */
    @PostMapping("im/queryUserOrderInfo")
    public ResponseResult imQueryUserOrderInfo(@RequestBody Map<String, Object> map) {
        ResponseResult resultResp = orderBaseService.imQueryUserOrderInfo(map);
        return resultResp;
    }

    /**
     * 怡亚通发货
     *
     * @param shipmentsListDTO
     * @return
     */
    @GlobalTransactional
    @PostMapping("/yiyatong/shipments")
    public ResponseResult yiyatongShipments(@RequestBody ShipmentsListDTO shipmentsListDTO) throws TransactionException {
        return orderBaseService.yiyatongShipments(shipmentsListDTO);
    }

    /**
     * 怡亚通售后
     *
     * @param returnListDTO
     * @return
     */
    @GlobalTransactional
    @PostMapping("/yiyatong/return")
    public ResponseResult yiyatongReturn(@RequestBody ReturnListDTO returnListDTO) throws TransactionException {
        return orderBaseService.yiyatongReturn(returnListDTO);
    }

    /**
     * 供应商发货发货
     *
     * @param map
     * @return
     */
    @GlobalTransactional
    @PostMapping("/supplierShipments")
    public ResponseResult supplierShipments(@RequestBody Map<String, Object> map) throws TransactionException {
        return orderBaseService.supplierShipments(map);
    }

    /**
     * 怡亚通已售订单
     *
     * @param map
     * @return
     */
    @PostMapping("/yiyatong/soldOrders")
    public ResponseResult yiyatongSoldOrders(@RequestBody Map<String, Object> map) {
        ResponseResult rr = orderBaseService.yiyatongSoldOrders(map);
        return rr;
    }

    /**
     * 怡亚通已售订单导出
     *
     * @param map
     * @return
     */
    @PostMapping("/yiyatong/soldOrdersExp")
    public ResponseResult yiyatongSoldOrdersExp(@RequestBody Map<String, Object> map) {
        ResponseResult rr = orderBaseService.yiyatongSoldOrdersExp(map);
        return rr;
    }

    /**
     * 供应商 已售订单
     *
     * @param map
     * @return
     */
    @PostMapping("/soldSupplierOrders")
    public ResponseResult soldSupplierOrders(@RequestBody Map<String, Object> map) {
        ResponseResult rr = orderBaseService.soldSupplierOrders(map);
        return rr;
    }

    /**
     * 供应商 已售订单 导出
     *
     * @param map
     * @return
     */
    @PostMapping("/soldSupplierOrdersExp")
    public ResponseResult soldSupplierOrdersExp(@RequestBody Map<String, Object> map) {
        ResponseResult rr = orderBaseService.soldSupplierOrdersExp(map);
        return rr;
    }

    /**
     * 供应商待发货订单
     *
     * @param map
     * @return
     */
    @PostMapping("/shipmentsSupplierOrder")
    public ResponseResult shipmentsSupplierOrder(@RequestBody Map<Object, Object> map) {
        ResponseResult rr = orderBaseService.shipmentsSupplierOrder(map);
        return rr;
    }

    /**
     * 查询发票订单信息
     *
     * @param params
     * @return
     */
    @PostMapping("/queInvoiceOrder")
    public ResponseResult queInvoiceOrder(@RequestBody queryInvoiceOrderDTO params) throws IllegalAccessException {
        ResponseResult result = orderBaseService.queInvoiceOrder(params);
        return result;
    }

    /**
     * 查询进货订单
     *
     * @param params
     * @return
     */
    @PostMapping("/queryPurchaseOrder")
    public ResponseResult queryPurchaseOrder(@RequestBody queryPurchaseOrderDTO params) throws IllegalAccessException {
        ResponseResult result = orderBaseService.queryPurchaseOrder(params);
        return result;
    }

    /**
     * 取消订单
     *
     * @param map
     * @return
     */
    @PostMapping("/cancelSupplierTheOrder")
    public ResponseResult cancelSupplierTheOrder(@RequestBody Map<Object, Object> map) {
        ResponseResult result = orderBaseService.cancelSupplierTheOrder(map);
        return result;
    }

    /**
     * 查询商品信息带分页
     *
     * @Description: 查询商品信息带分页
     * @Param: [map]
     * @Return:
     */
    @PostMapping("/queryReturnGoodsPage")
    public ResponseResult queryReturnGoodsPage(@RequestBody Map<String, Object> map) {
        ResponseResult resultResp = orderBaseService.queryReturnGoodsPage(map);
        return resultResp;
    }

    /**
     * 商家查询子单
     *
     * @param map
     * @return
     */
    @PostMapping("/querySonOrders")
    public ResponseResult querySonOrders(@RequestBody Map<String, Object> map) {
        ResponseResult rr = orderBaseService.querySonOrders(map);
        return rr;
    }

    /**
     * 更新订单的团购状态
     *
     * @param orderIdlist 一个包含多个订单ID的列表，用于指定需要更新团购状态的订单
     */
    @GetMapping("/feign/updateOrderGroupbuyStatus")
    public void updateOrderGroupbuyStatus(@RequestParam("orderIdlist") List<String> orderIdlist) {
        orderBaseService.updateOrderGroupbuyStatus(orderIdlist);
    }

    /**
     * 查询店铺订单数量
     * 根据店铺ID查询该店铺的订单数量
     *
     * @param shopId 店铺ID，用于标识需要查询的店铺
     * @return 店铺的订单数量如果查询结果为空，则返回null
     */
    @GetMapping("/feign/selectShopOrder")
    public Integer selectShopOrder(@RequestParam("shopId") Integer shopId) {
        Integer result = orderBaseService.selectShopOrder(shopId);
        return result;
    }

    /**
     * 获取未完成开票的订单数量
     *
     * @param param
     * @return
     */
    @PostMapping("/feign/getUndealedOrderCount")
    public Integer getUndealedOrderCount(@RequestBody Map<String, Object> param) {
        Integer result = orderBaseService.getUndealedOrderCount(param);
        return result;
    }

    /**
     * 根据指定日期范围和店铺ID查询支付金额
     * 此方法用于从数据库中检索特定店铺在指定时间范围内的支付金额
     * 它主要用于生成报表或进行数据分析
     *
     * @param startTimeStr 开始时间字符串，表示查询范围的起始时间
     * @param endTimeStr   结束时间字符串，表示查询范围的结束时间
     * @param shopId       店铺ID，用于指定查询的店铺
     * @return 返回查询到的支付金额，使用BigDecimal类型以确保财务数据的精确性
     */
    @GetMapping("/feign/queryPayMentByDate")
    public BigDecimal queryPayMentByDate(@RequestParam("startTimeStr") String startTimeStr, @RequestParam("endTimeStr") String endTimeStr, @RequestParam("shopId") String shopId) {
        BigDecimal result = orderBaseService.queryPayMentByDate(startTimeStr, endTimeStr, shopId);
        return result;
    }

    /**
     * 查询支付金额数量
     *
     * @param startTimeStr
     * @param endTimeStr
     * @param shopId
     * @return
     */
    @GetMapping("/feign/queryPayCountByDate")
    public Integer queryPayCountByDate(@RequestParam("startTimeStr") String startTimeStr, @RequestParam("endTimeStr") String endTimeStr, @RequestParam("shopId") String shopId) {
        Integer result = orderBaseService.queryPayCountByDate(startTimeStr, endTimeStr, shopId);
        return result;
    }

    /**
     * 查询店铺的支付金额
     *
     * @param shopId
     * @param today
     * @return
     */
    @GetMapping("/feign/queryShopBasePayPriceByApp")
    public List<AppShopBaseSalesInfoVO> queryShopBasePayPriceByApp(@RequestParam("shopId") String shopId, @RequestParam("today") String today) {
        List<AppShopBaseSalesInfoVO> result = orderBaseService.queryShopBasePayPriceByApp(shopId, today);
        return result;

    }

    /**
     * 查询订单信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/feign/queryOrderInfo")
    public Map<String, Object> queryOrderInfo(@RequestParam("orderId") String orderId) {
        Map<String, Object> result = orderBaseService.queryOrderInfo(orderId);
        return result;
    }

    /**
     * 查询用户信息
     */
    @GetMapping("/feign/queryBuyUserId")
    public Map<String, Object> queryBuyUserId(@RequestParam("orderId") String orderId) {
        Map<String, Object> result = orderBaseService.queryBuyUserId(orderId);
        return result;
    }


    /**
     * 更新退款会计期间。
     *
     * @param param 退款会计期间参数DTO
     * @return 更新操作的影响行数
     */
    @PostMapping("/feign/updateRefundAccountPeriod")
    public int updateRefundAccountPeriod(@RequestBody RefundAccountParamDTO param) {
        int row = orderBaseService.updateRefundAccountPeriod(param);
        return row;
    }

    /**
     * 更新账单状态。
     *
     * @param accountingPeriodId 会计期间ID
     * @param accountStatus      账单状态
     */
    @GetMapping("/feign/editAccountStatus")
    public void editAccountStatus(@RequestParam("accountingPeriodId") String accountingPeriodId, @RequestParam("accountStatus") Integer accountStatus) {
        orderBaseService.editAccountStatus(accountingPeriodId, accountStatus);
    }

    /**
     * 查询订单信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/feign/queryOrderInfoFegin")
    public OrderBaseVO queryOrderInfoFegin(@RequestParam("orderId") String orderId) {
        OrderBaseVO result = orderBaseService.queryOrderInfoFegin(orderId);
        return result;
    }

    /**
     * 查询会计期间ID。
     *
     * @param orderId 订单号
     * @return 会计期间ID
     */
    @GetMapping("/feign/queryAccountingPeriodId")
    public String queryAccountingPeriodId(@RequestParam("orderId") String orderId) {
        String result = orderBaseService.queryAccountingPeriodId(orderId);
        return result;
    }

    /**
     * 根据会计期间ID查询账单详情。
     *
     * @param accountingPeriodId 会计期间ID
     * @return 账单详情VO
     */
    @GetMapping("/feign/selectDetailsByPeriodId")
    public AccountShopBillDetailsVO selectDetailsByPeriodId(@RequestParam("accountingPeriodId") String accountingPeriodId) {
        AccountShopBillDetailsVO result = orderBaseService.selectDetailsByPeriodId(accountingPeriodId);
        return result;
    }

    /**
     * 查询店铺
     *
     * @param orderId
     * @return
     */
    @GetMapping("/feign/queryTreasureShopIdByInt")
    public String queryTreasureShopIdByInt(@RequestParam("orderId") String orderId) {
        String shopid = orderBaseService.queryTreasureShopIdByInt(orderId);
        return shopid;
    }

    /**
     * 根据订单号查询订单信息。
     *
     * @param orderId 订单号，是查询订单的关键标识。
     * @return 返回订单号对应的订单信息。如果找不到匹配的订单，可能返回空字符串。
     */
    @GetMapping("/feign/queryOrderId")
    public String queryOrderId(@RequestParam("orderId") String orderId) {
        String result = orderBaseService.queryOrderId(orderId);
        return result;
    }

    /**
     * 查询订单号和支付单号
     *
     * @param orderId
     * @return
     */
    @GetMapping("/feign/queryOrderIdAndPaymentNumber")
    public List<Map<String, Object>> queryOrderIdAndPaymentNumber(@RequestParam("orderId") String orderId) {
        List<Map<String, Object>> list = orderBaseService.queryOrderIdAndPaymentNumber(orderId);
        return list;
    }

    /**
     * 新增怡亚通订单记录
     *
     * @param submitOrderListVO
     * @return
     */
    @PostMapping("/feign/insertYiYaTongOrder")
    public Integer insertYiYaTongOrder(@RequestBody SubmitOrderVO submitOrderListVO) {
        Integer result = orderBaseService.insertYiYaTongOrder(submitOrderListVO);
        return result;
    }

    /**
     * 判断纯数字订单
     *
     * @param orderId
     * @param paymentNumber
     * @return
     */
    @GetMapping("/feign/queryAssetInfo")
    public List<AssetTmpInfoDTO> queryAssetInfo(@RequestParam("orderId") String orderId, @RequestParam("paymentNumber") String paymentNumber) {
        List<AssetTmpInfoDTO> list = orderBaseService.queryAssetInfo(orderId, paymentNumber);
        return list;
    }

    /**
     * 查询买方用户ID
     *
     * @param orderId
     * @return
     */
    @GetMapping("/feign/queryBuyerUserIdByOrderBase")
    public String queryBuyerUserIdByOrderBase(@RequestParam("orderId") String orderId) {
        String result = orderBaseService.queryBuyerUserIdByOrderBase(orderId);
        return result;
    }

    /**
     * 查找订单商品id
     *
     * @param orderId
     * @return
     */
    @GetMapping("/feign/queryGoodsByOrderId")
    public List<WapGoodsSaleSumDTO> queryGoodsByOrderId(@RequestParam("orderId") String orderId) {
        List<WapGoodsSaleSumDTO> list = orderBaseService.queryGoodsByOrderId(orderId);
        return list;
    }

    /**
     * 检查订单编号是否存在的接口
     * 该方法通过POST请求接收一个订单ID参数，并返回该订单编号是否存在于系统中的信息
     * 主要用于在创建新订单前，检查订单编号的唯一性，以避免重复订单的问题
     *
     * @param orderId 订单编号，用于查询订单是否已存在
     * @return 返回一个整数，表示存在多少个与给定订单编号相同的订单
     */
    @PostMapping("queryOrderNumExist")
    public Integer queryOrderNumExist(@RequestParam("orderId") String orderId) {
        return orderBaseService.queryOrderNumExist(orderId);
    }

    /**
     * 将货币符号和汇率存入订单表
     *
     * @param orderId
     * @param currencyCode
     * @param exchangeRate
     * @return
     */
    @PostMapping("updateCurrencyAndExchangeRate")
    public ResponseResult updateCurrencyAndExchangeRate(@RequestParam("orderId") String orderId, @RequestParam("currencyCode") String currencyCode,
                                                        @RequestParam("exchangeRate") BigDecimal exchangeRate, @RequestParam("currencySymbol") String currencySymbol) {
        return orderBaseService.updateCurrencyAndExchangeRate(orderId, currencyCode, exchangeRate, currencySymbol);
    }

    /**
     * 根据订单查询货币信息
     *
     * @param orderId
     * @return
     */
    @PostMapping("queryOrderCurrencyInfo")
    public BaseCurrencySymbolVO queryOrderCurrencyInfo(@RequestParam("orderId") String orderId) {
        return orderBaseService.queryOrderCurrencyInfo(orderId);
    }

    /**
     * 查看拼团状态
     *
     * @param para
     * @return
     */
    @RequestMapping("checkGroupBookingStatus")
    public boolean checkGroupBookingStatus(@RequestBody ExpireMarkIdUpdateDTO para) {
        boolean flag = orderBaseService.checkGroupBookingStatus(para);
        return flag;
    }

    /**
     * 保存京东订单号
     *
     * @param orderId
     * @param jingDongOrderId
     * @return
     */
    @PostMapping("/updateOrderJingDongOrderId")
    public ResponseResult updateOrderJingDongOrderId(@RequestParam("orderId") String orderId, @RequestParam("jingDongOrderId") String jingDongOrderId) {
        return orderBaseService.updateOrderJingDongOrderId(orderId, jingDongOrderId);
    }

    /**
     * 根据京东订单号 修改我方订单状态为 已发货
     *
     * @param jdOrderIds
     * @return
     */
    @PostMapping("/updateOrderStatusByJDOrderId")
    ResponseResult updateOrderStatusByJDOrderId(@RequestParam("jdOrderIds") List<String> jdOrderIds) {
        return orderBaseService.updateOrderStatusByJDOrderId(jdOrderIds);
    }

    /**
     * 将京东子订单号保存入库中
     *
     * @param orderGoodsJdVopDTOS
     * @return
     */
    @PostMapping("/updateJongDongSonOrder")
    ResponseResult updateJongDongSonOrder(@RequestBody List<OrderGoodsJdVopDTO> orderGoodsJdVopDTOS) {
        return orderBaseService.updateJongDongSonOrder(orderGoodsJdVopDTOS);
    }

    /**
     * 京东已售订单
     *
     * @param map
     * @return
     */
    @PostMapping("/jingDong/soldOrders")
    public ResponseResult jingDongSoldOrders(@RequestBody Map<String, Object> map) {
        ResponseResult rr = orderBaseService.jingDongSoldOrders(map);
        return rr;
    }

    /**
     * 京东已售订单导出
     *
     * @param map
     * @return
     */
    @PostMapping("/jingDong/soldOrdersExp")
    public ResponseResult jingDongSoldOrdersExp(@RequestBody Map<String, Object> map) {
        ResponseResult rr = orderBaseService.jingDongSoldOrdersExp(map);
        return rr;
    }

    /**
     * 判断京东余额支付足够支付
     *
     * @param orderId
     * @param orderAmount
     * @return
     */
    @GetMapping("/queryJDBalanceIsEnough")
    public ResponseResult queryJDBalanceIsEnough(@RequestParam("orderId") String orderId,
                                                 @RequestParam("orderAmount") BigDecimal orderAmount) {

        ResponseResult rr = orderBaseService.queryJDBalanceIsEnough(orderId, orderAmount);
        return rr;
    }

    @GetMapping("/getOrderBase")
    public ResponseResult<OrderBaseVO> getOrderBase(@RequestParam(value = "orderId", required = false) String orderId,
                                                    @RequestParam(value = "paymentNumber", required = false) String paymentNumber) {
        OrderBaseEntity one = orderBaseService.getOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(StrUtil.isNotBlank(orderId), OrderBaseEntity::getOrderId, orderId)
                .eq(StrUtil.isNotBlank(paymentNumber), OrderBaseEntity::getPaymentNumber, paymentNumber));
        return ResponseResult.success(BeanUtil.copyProperties(one, OrderBaseVO.class));
    }

    /**
     * 获取非数字订单 feign 支付宝
     */
    @PostMapping("/points/updateOrderStatus")
    public void updateOrderStatus(@RequestBody PointsPayReqVO req) {
        orderBaseService.update(null, new LambdaUpdateWrapper<OrderBaseEntity>()
                .set(OrderBaseEntity::getOrderStatus, req.getOrderStatus())
                .set(OrderBaseEntity::getPayCode, req.getPayCode())
                .set(OrderBaseEntity::getPaymentName, req.getPaymentName())
                .set(OrderBaseEntity::getPaymentTime, new Date())
                .eq(OrderBaseEntity::getPaymentNumber, req.getOrderId()));


        OrderBaseEntity orderBase = orderBaseService.getOne(new LambdaQueryWrapper<OrderBaseEntity>()
                .eq(OrderBaseEntity::getPaymentNumber, req.getOrderId()));
        if (BeanUtil.isNotEmpty(orderBase)) {
            orderGoodsService.update(null, new LambdaUpdateWrapper<OrderGoodsEntity>()
                    .set(OrderGoodsEntity::getOrderGoodsStatus, req.getOrderStatus())
                    .eq(OrderGoodsEntity::getOrderId, orderBase.getOrderId()));
        }
    }
}
