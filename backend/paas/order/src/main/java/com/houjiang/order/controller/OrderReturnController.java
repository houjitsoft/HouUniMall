package com.houjiang.order.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.houjiang.commons.annnotation.AdminTokenLimit;
import com.houjiang.commons.annnotation.RepeatClick;
import com.houjiang.commons.annnotation.ShopTokenLimit;
import com.houjiang.commons.dto.shop.ShopSalesStatisticsDTO;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.ApplyReturnOrderDTO;
import com.houjiang.order.dto.OrderRemindDeliveryDTO;
import com.houjiang.order.dto.ReturnReportQueryDTO;
import com.houjiang.order.dto.WaitPlatformRefundDTO;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OrderReturnController
 * @Description 退款
 * @Author Amos
 * @Date 2023/6/21 11:05
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/order/return")
public class OrderReturnController extends BaseController {

    /**
     * 未发货退款
     *
     * @param map
     * @return
     */
    @RepeatClick
    @PostMapping("/applyRefund")
    public ResponseResult applyRefund(@RequestBody Map<String, String> map) throws TransactionException {
        ResponseResult result = orderReturnService.applyRefund(map);
        return result;

    }

    /**
     * 申请退货
     *
     * @param map
     * @return
     */
    @PostMapping("/applyReturn")
    public ResponseResult applyReturn(@RequestBody Map<String, String> map) throws TransactionException {
        ResponseResult result = orderReturnService.applyReturn(map);
        return result;
    }

    /**
     * 申请退多件货物（退货退款）
     *
     * @param applyReturnOrders
     * @return
     */
    @RepeatClick
    @PostMapping("/applyReturn/more")
    public ResponseResult applyReturnMore(@RequestBody ApplyReturnOrderDTO applyReturnOrders) throws TransactionException {
        ResponseResult result = orderReturnService.applyReturnMore(applyReturnOrders);
        return result;

    }

    /**
     * 仅退款
     *
     * @param map
     * @return
     */
    @RepeatClick
    @PostMapping("/onlyReturnMoney")
    public ResponseResult onlyReturnMoney(@RequestBody Map<String, Object> map) throws TransactionException {
        ResponseResult result = orderReturnService.onlyReturnMoney(map);
        return result;
    }

    /**
     * 查询退款/退货订单
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/listReturnOrderByRT")
    public ResponseResult listReturnOrderByRT(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderReturnService.listReturnOrderByRT(paraMap);
        return result;
    }

    /**
     * 退货详情
     *
     * @param map
     * @return
     */
    @PostMapping("/returnDetail")
    public ResponseResult returnDetail(@RequestBody Map<String, String> map) {
        ResponseResult result = orderReturnService.returnDetail(map);
        return result;
    }


    /**
     * 查询退款详情
     *
     * @param map
     * @return
     */
    @PostMapping("/refundDetail")
    public ResponseResult refundDetail(@RequestBody Map<String, String> map) {
        ResponseResult result = orderReturnService.refundDetail(map);
        return result;
    }

    /**
     * 买家填写物流信息
     *
     * @param map
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @PostMapping("/return/shipping")
    public ResponseResult returnShipping(@RequestBody Map<String, String> map) {
        ResponseResult result = orderReturnService.returnShipping(map);
        return result;
    }

    /**
     * @Description: 查询退款服务详情
     * @Param: [map]
     * @Return:ResponseResult
     */
    @PostMapping("/selectOrderReturn")
    public ResponseResult selectOrderReturn(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.selectordereturn(map);
        return result;
    }

    /**
     * @Description: 买家撤销
     * @Param: [map]
     * @Return: ResponseResult
     */
    @RepeatClick
    @PostMapping("/revoke")
    public ResponseResult revokeIntervention(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.revokeIntervention(map);
        return result;
    }

    /**
     * @Description: 查询退款服务详情有平台介入
     * @Param: [map]
     * @Return: ResponseResult
     */
    @PostMapping("/selectOrderReturnPlatform")
    public ResponseResult selectOrderReturnPlatform(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.selectordereturnplatform(map);
        return result;
    }


    /**
     * 判断是否参加过售后
     *
     * @param map
     * @return com.yf.ShoppingCart.web.entity.ResultResp
     */
    @PostMapping("/check/afterSales")
    public ResponseResult checkAfterSales(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.checkAfterSales(map);
        return result;
    }

    /**
     * @Description: 买家取消平台介入
     * @Param: [map]
     * @Return: com.yf.ShoppingCart.web.entity.ResultResp
     */
    @RepeatClick
    @PostMapping("/revoke/platform")
    public ResponseResult revokePlatformIntervention(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.revokePlatformIntervention(map);
        return result;
    }

    /**
     * 计算退货价格（退货退款）
     *
     * @param applyReturnOrders
     * @return
     */
    @PostMapping("/calculate/price")
    public ResponseResult calculatePrice(@RequestBody ApplyReturnOrderDTO applyReturnOrders) {
        ResponseResult result = orderReturnService.calculatePrice(applyReturnOrders);
        return result;
    }

    /**
     * 获取退款订单列表
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/listRefundOrder")
    public ResponseResult listRefundOrder(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderReturnService.listRefundOrder(paraMap);
        return result;
    }

    /**
     * 平台退款订单详情
     *
     * @param paraMap
     * @return
     */
    @PostMapping("/platform/queryRefundOrderDetail")
    public ResponseResult PlatformReturnDetail(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderReturnService.PlatformReturnDetail(paraMap);
        return result;
    }

    /**
     * 获取退款订单报表导出
     *
     * @param paraMap
     * @return
     */
    @PostMapping(value = "/platform/listRefundOrderExcel", produces = "application/json;")
    public ResponseResult listrefundorderExp(@RequestBody(required = false) Map<String, String> paraMap, HttpServletResponse response) {
        ResponseResult result = orderReturnService.listRefundOrderExcel(paraMap, response);
        return result;
    }

    /**
     * 查看提醒发货列表
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/queryOrderRemindDeliveryPort")
    public ResponseResult queryOrderRemindDeliveryPort(@RequestBody(required = false) OrderRemindDeliveryDTO param) {
        ResponseResult result = orderReturnService.queryOrderRemindDeliveryPort(param);
        return result;
    }

    /**
     * 提醒发货
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/OrderRemindDeliveryPort")
    public ResponseResult OrderRemindDeliveryPort(@RequestBody(required = false) Map<String, String> param) {
        ResponseResult result = orderReturnService.OrderRemindDeliveryPort(param);
        return result;
    }

    /**
     * 查看提醒发货列表导出
     *
     * @param param
     * @return
     */
    @PostMapping("/platform/remindDeliveryExcel")
    public ResponseResult queryOrderRemindDeliveryPortExcel(@RequestBody(required = false) OrderRemindDeliveryDTO param) {
        ResponseResult result = orderReturnService.queryOrderRemindDeliveryPortExcel(param);
        return result;
    }

    /**
     * 获取退款订单
     *
     * @return
     */
    @RequestMapping("/platform/listReturnOrder")
    public ResponseResult listReturnOrder(@RequestBody Map<String, Object> paraMap) {
        ResponseResult result = orderReturnService.listReturnOrder(paraMap);
        return result;
    }

    /**
     * 获取退货订单报表导出
     *
     * @return
     */
    @PostMapping(value = "/platform/listReturnOrderExcel", produces = "application/json;")
    public ResponseResult listrefundorderExcel(@RequestBody(required = false) Map<String, String> paraMap, HttpServletResponse response) {
        ResponseResult result = orderReturnService.listrefundorderExcel(paraMap, response);
        return result;
    }


    /**
     * 财务退货及退款汇总
     *
     * @param param
     * @return
     */
    @PostMapping("/queryReturnsaleReport")
    public ResponseResult queryReturnsaleReport(@RequestBody ReturnReportQueryDTO param) throws IllegalAccessException {
        ResponseResult result = orderReturnService.queryReturnsaleReport(param);
        return result;
    }


    /**
     * 平台查询 售后服务 列表 导出
     *
     * @param param
     * @return
     */
    @PostMapping("/queryReturnsaleReportExcel")
    public ResponseResult queryReturnsaleReportExcel(@RequestBody ReturnReportQueryDTO param) {
        ResponseResult result = orderReturnService.queryReturnsaleReportExcel(param);
        return result;
    }

    /**
     * 列出待操作订单
     *
     * @param map
     * @return
     */
    @ShopTokenLimit
    @PostMapping("/list/refund")
    public ResponseResult listRefund(@RequestBody Map<String, Object> map) throws ParseException {
        ResponseResult result = orderReturnService.listRefund(map);
        return result;
    }

    /**
     * 商家同意，则 type=1 不同意，则type=0
     *
     * @param map
     * @return
     */
    @PostMapping("/forSellerApprove")
    @GlobalTransactional
    public ResponseResult forSellerApprove(@RequestBody Map<String, String> map) throws TransactionException {
        ResponseResult result = orderReturnService.forSellerApprove(map);
        return result;
    }
    /**
     * 京东取消订单 商城端退款
     *
     * @param jdOrderIds
     * @return
     */
    @PostMapping("/jingdong/forSellerApprove")
    @GlobalTransactional
    public ResponseResult jingdongForSellerApprove(@RequestParam("jdOrderIds") List<String> jdOrderIds) throws TransactionException {
        ResponseResult result = orderReturnService.jingdongForSellerApprove(jdOrderIds);
        return result;
    }
    /**
     * 退货 1.1 退货可能又多件-供应商
     *
     * @param paraMap
     * @return
     */
    @ShopTokenLimit
    @PostMapping("/afterMarket")
    public ResponseResult returnorder(@RequestBody Map<String, Object> paraMap) throws ParseException {
        paraMap.put("type", 0);
        ResponseResult returnorder = orderReturnService.returnorder(paraMap);
        return returnorder;
    }

    /**
     * 退货 1.1 退货可能又多件-经销商
     *
     * @param paraMap
     * @return
     */
    @ShopTokenLimit
    @PostMapping("/afterMarketShop")
    public ResponseResult afterMarketShop(@RequestBody Map<String, Object> paraMap) throws ParseException {
        paraMap.put("type", 1);
        ResponseResult returnorder = orderReturnService.returnorder(paraMap);
        return returnorder;
    }

    /**
     * 商家同意退货与否，则 type=1 同意，则type=0不同意
     *
     * @param map
     * @return
     */
    @RepeatClick
    @PostMapping("/retForSeller/approve")
    public ResponseResult retForSellerApprove(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.retForSellerApprove(map);
        return result;
    }
    /**
     * 京东售后单 退换货 处理
     *
     * @param map
     * @return
     */
    @PostMapping("/jingDong/return/audit")
    public ResponseResult jingDongReturnAudit(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.jingDongReturnAudit(map);
        return result;
    }
    /**
     * 商家确认收货
     *
     * @param map
     * @return
     */
    @RepeatClick
    @PostMapping("/retForSeller/receive")
    @GlobalTransactional
    public ResponseResult retForSellerReceive(@RequestBody Map<String, String> map) throws TransactionException {
        ResponseResult result = orderReturnService.retForSellerReceive(map);
        return result;
    }
    /**
     * 京东 退货 换货 确认收货后 商品质检审核 通过 不通过
     *
     * @param map
     * @return
     */
    @PostMapping("/jingDong/qualityInspection/receive")
    @GlobalTransactional
    public ResponseResult jingDongQualityInspectionReceive(@RequestBody Map<String,Object> map) throws TransactionException {
        ResponseResult result = orderReturnService.jingDongQualityInspectionReceive(map);
        return result;
    }
    /**
     * 商家查询退款服务详情
     *
     * @param map
     * @return
     */
    @PostMapping("/select/shop/orderReturn")
    public ResponseResult selectshopordereturn(@RequestBody Map<String, Object> map) {
        ResponseResult selectshopordereturn = orderReturnService.selectshopordereturn(map);
        return selectshopordereturn;
    }

    /**
     * 卖家上传平台介入凭证
     *
     * @param map
     * @return
     */
    @PostMapping("/add/shop/intervention")
    public ResponseResult insertShopIntervention(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.insertShopIntervention(map);
        return result;
    }

    /**
     * 唤起售后单
     *
     * @param map
     * @return
     */
    @PostMapping("/arouse/return")
    public ResponseResult callReturnOrder(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnService.callReturnOrder(map);
        return result;
    }

    /**
     * 商家填写物流信息
     *
     * @param map
     * @return
     */
    @PostMapping("/shop/return/shipping")
    @GlobalTransactional
    public ResponseResult shopReturnShipping(@RequestBody Map<String, String> map) throws TransactionException {
        ResponseResult result = orderReturnService.shopReturnShipping(map);
        return result;
    }

    /**
     * 平台查询线下支付售后订单
     */
    @AdminTokenLimit
    @GlobalTransactional
    @PostMapping("/platform/queryWaitingForRefund")
    public ResponseResult queryWaitingForPlatformRefund(@RequestBody Map<String, Object> paraMap) {
        return orderReturnService.queryWaitingForPlatformRefund(paraMap);
    }

    /**
     * 平台导出线下支付售后订单
     */
    @PostMapping("/platform/queryWaitingForRefundExp")
    public ResponseResult queryWaitingForPlatformRefundExp(@RequestBody Map<String, String> paraMap) {
        return orderReturnService.queryWaitingForPlatformRefundExp(paraMap);
    }

    /**
     * 线下支付订单 平台上传退款支付凭证
     */
    @AdminTokenLimit
    @GlobalTransactional
    @PostMapping("/platform/waitingForRefund")
    public ResponseResult waitingForPlatformRefund(@RequestBody @Validated WaitPlatformRefundDTO dto) {
        return orderReturnService.waitingForPlatformRefund(dto);
    }

    /**
     * 定时任务商家超时未处理  同意退款
     *
     * @return
     */
    @PostMapping("/job/updateTimeOutReturn")
    public Boolean updateTimeOutReturn() {
        Boolean resultResp = orderReturnService.updateTimeOutReturn();
        return resultResp;
    }


    /**
     * 定时任务商家同意退货
     *
     * @return
     */
    @RequestMapping("/job/updateTimeOutReturnGoods")
    @ResponseBody
    public Boolean updateTimeOutReturnGoods() {
        Boolean resultResp = orderReturnService.updateTimeOutReturnGoods();
        return resultResp;
    }


    /**
     * 定时任务  买家超时未处理 退款
     *
     * @return
     */
    @PostMapping("/job/updateTimeOutbuyReturn")
    public Boolean updateTimeOutBuyReturn() {
        Boolean resultResp = orderReturnService.updateTimeOutBuyReturn();
        return resultResp;
    }


    /**
     * 定时任务 退货 买家超时未处理
     *
     * @return
     */
    @PostMapping("/job/updatebuyTimeOutReturn")
    public Boolean updatebuyTimeOutReturn() {
        Boolean resultResp = orderReturnService.updatebuyTimeOutReturn();
        return resultResp;
    }


    /**
     * 定时任务 商家同意退货 买家未在有效期内填写物流信息
     *
     * @return
     */
    @PostMapping("/job/updatebuyTimeOutReturnGoods")
    public Boolean updatebuyTimeOutReturnGoods() {
        Boolean resultResp = orderReturnService.updatebuyTimeOutReturnGoods();
        return resultResp;
    }

    /**
     * 根据订单号查询退款订单号。
     *
     * @param orderId 原始订单号，用于查询对应的退款订单号。
     * @return 返回退款订单号。
     */
    @GetMapping("/feign/queryRefundOrderNum")
    public String queryRefundOrderNum(@RequestParam("orderId") String orderId) {
        String refundOrderNum = orderReturnService.queryRefundOrderNum(orderId);
        return refundOrderNum;
    }

    /**
     * 查询店铺的退货退款现金金额。
     *
     * @param pojo   销售统计DTO
     * @return 退货退款现金金额
     */
    @PostMapping("/feign/querShopReturnCash")
    public BigDecimal querShopReturnCash(@RequestBody ShopSalesStatisticsDTO pojo) {
        BigDecimal shopReturnCash = orderReturnService.querShopReturnCash(pojo);
        return shopReturnCash;
    }

}
