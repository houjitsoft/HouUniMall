package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 订单服务Feign熔断器
/**
 * 
 * @description 订单服务降级处理
 * @author Amos
 * @date 2024-12-20
 */
@Slf4j
@Component
public class OrderServiceFeignFallback implements OrderServiceFeign {

    @Override
    public ResponseResult callOrderAPI(String path, Map<String, Object> params) {
        log.error("调用订单服务API失败：订单服务可能未启动或网络连接异常，path: {}", path);
        return ResponseResult.fail(503, "调用订单服务API失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryOrderList(Map<String, Object> param) {
        log.error("查询订单列表失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询订单列表失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryOrdersByPC(Map<String, Object> param) {
        return ResponseResult.fail(503, "订单服务暂时不可用");
    }

    @Override
    public ResponseResult importShipmentsOrderExport(MultipartFile file, String token) {
        log.error("批量发货单导入失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "批量发货单导入失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult getOrderDetail(Map<String, Object> param) {
        log.error("查询订单详情失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询订单详情失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult createOrder(Map<String, Object> orderData) {
        log.error("创建订单失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "创建订单失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult calculatePriceRaw(String message) {
        log.error("订单算价失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "算价失败：订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult createOrderRaw(String para) {
        log.error("提交订单失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "提交订单失败：订单服务暂时不可用，请稍后重试");
    }

    @Override
    public ResponseResult updateOrder(Map<String, Object> orderData) {
        log.error("更新订单失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "更新订单失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult cancelOrder(Map<String, Object> param) {
        log.error("取消订单失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "取消订单失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult deleteOrder(Map<String, Object> param) {
        log.error("删除订单失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "删除订单失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult updateOrderStatus(Map<String, Object> param) {
        log.error("更新订单状态失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "更新订单状态失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult payOrder(Map<String, Object> param) {
        log.error("订单支付失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "订单支付失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult getOrderStatistics(Map<String, Object> param) {
        log.error("查询订单统计失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询订单统计失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult confirmOrder(Map<String, Object> param) {
        log.error("确认收货失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "确认收货失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryCartList(Map<String, Object> param) {
        log.error("查询购物车失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询购物车失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult addToCart(Map<String, Object> cartData) {
        log.error("添加购物车失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "添加购物车失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult updateCart(Map<String, Object> param) {
        log.error("更新购物车失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "更新购物车失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult removeFromCart(Map<String, Object> param) {
        log.error("删除购物车商品失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "删除购物车商品失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult cartSelectCount(Map<String, Object> param) {
        log.error("查询购物车数量失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询购物车数量失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult cartAddGoodsNum(Map<String, Object> param) {
        log.error("购物车加数量失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "购物车加数量失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult cartReduceGoodsNum(Map<String, Object> param) {
        log.error("购物车减数量失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "购物车减数量失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult applyRefund(Map<String, Object> refundData) {
        log.error("申请退款失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "申请退款失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult applyReturn(Map<String, Object> returnData) {
        log.error("申请退货失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "申请退货失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryRefundList(Map<String, Object> param) {
        log.error("查询退款记录失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询退款记录失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult processRefund(Map<String, Object> param) {
        log.error("处理退款失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "处理退款失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult addReview(Map<String, Object> reviewData) {
        log.error("添加评价失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "添加评价失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryReviewList(Map<String, Object> param) {
        log.error("查询评价列表失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询评价列表失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult replyReview(Map<String, Object> param) {
        log.error("回复评价失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "回复评价失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult applyInvoice(Map<String, Object> invoiceData) {
        log.error("申请发票失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "申请发票失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryInvoiceList(Map<String, Object> param) {
        log.error("查询发票记录失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询发票记录失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryCommissionList(Map<String, Object> param) {
        log.error("查询佣金记录失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询佣金记录失败：订单服务暂时不可用，请检查订单服务是否正常启动或网络连接是否正常");
    }

    @Override
    public ResponseResult queryDistributionOrders(Map<String, Object> param) {
        log.error("查询分销订单失败：订单服务可能未启动或网络连接异常");
        return ResponseResult.fail(503, "查询分销订单失败：订单服务暂时不可用");
    }

    /** ================= 管理端订单扩展 ================= */

    @Override
    public ResponseResult queryComplaintsList(Map<String, Object> param) {
        log.error("查询投诉列表失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询投诉列表失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryComplaintsDetail(Map<String, Object> param) {
        log.error("查询投诉详情失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询投诉详情失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult handleComplaints(Map<String, Object> param) {
        log.error("处理投诉失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "处理投诉失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryPlatformReturnList(Map<String, Object> param) {
        log.error("查询平台退货列表失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询平台退货列表失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryPlatformReturnDetail(Map<String, Object> param) {
        log.error("查询平台退货详情失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询平台退货详情失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult processPlatformReturn(Map<String, Object> param) {
        log.error("处理平台退货失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "处理平台退货失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult querySettlementList(Map<String, Object> param) {
        log.error("查询结算列表失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询结算列表失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult querySettlementDetail(Map<String, Object> param) {
        log.error("查询结算详情失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询结算详情失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult processSettlement(Map<String, Object> param) {
        log.error("执行结算失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "执行结算失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult applyExchange(Map<String, Object> param) {
        log.error("申请换货失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "申请换货失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryExchangeList(Map<String, Object> param) {
        log.error("查询换货列表失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询换货列表失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult processExchange(Map<String, Object> param) {
        log.error("处理换货失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "处理换货失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult shipOrder(Map<String, Object> param) {
        log.error("订单发货失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "订单发货失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult changeShipments(Map<String, Object> param) {
        log.error("修改快递单号失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "修改快递单号失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryLogistics(Map<String, Object> param) {
        log.error("查询物流信息失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询物流信息失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult updateOrderByMap(Map<String, Object> param) {
        log.error("更新订单备注失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "更新订单备注失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryAfterSaleList(Map<String, Object> param) {
        log.error("查询售后列表失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询售后列表失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryAfterSaleDetail(Map<String, Object> param) {
        log.error("查询售后详情失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询售后详情失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryRefundDetail(Map<String, Object> param) {
        log.error("查询退款详情失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询退款详情失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult sellerApproveAfterSale(Map<String, Object> param) {
        log.error("商家审核售后失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "商家审核售后失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult sellerApproveReturn(Map<String, Object> param) {
        log.error("商家审核退货失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "商家审核退货失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult getOrdersTotal(Map<String, Object> param) {
        log.warn("订单服务不可用，返回空统计降级");
        Map<String, Object> empty = new java.util.LinkedHashMap<>();
        empty.put("totalOrders", 0);
        empty.put("pendingOrders", 0);
        empty.put("completedOrders", 0);
        empty.put("totalAmount", 0);
        empty.put("todayOrders", 0);
        empty.put("todayAmount", 0);
        return ResponseResult.success(empty);
    }

    @Override
    public ResponseResult querySoldOrders(Map<String, Object> param) {
        log.error("查询已售订单失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询已售订单失败：订单服务暂时不可用");
    }

    /** ================= /web/order/* 前端兼容补齐 ================= */

    @Override
    public ResponseResult delayConfirm(Map<String, Object> param) {
        log.error("延迟收货失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "延迟收货失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult exportOrders(Map<String, Object> param) {
        log.error("导出订单失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "导出订单失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult insertShoppingCart(Map<String, Object> cartData) {
        log.error("加入购物车失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "加入购物车失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult clearShoppingCart(Map<String, Object> param) {
        log.error("清空购物车失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "清空购物车失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult submitEvaluation(Map<String, Object> reviewData) {
        log.error("提交评价失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "提交评价失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryMyEvaluation(Map<String, Object> param) {
        log.error("查询评价失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询评价失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult checkAfterSales(Map<String, Object> param) {
        log.error("售后审核失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "售后审核失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryBarterDetail(Map<String, Object> param) {
        log.error("查询换货详情失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询换货详情失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryInvoiceDetail(Map<String, Object> param) {
        log.error("查询发票详情失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询发票详情失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult auditInvoice(Map<String, Object> param) {
        log.error("发票审核失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "发票审核失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult deleteEvaluation(Map<String, Object> param) {
        log.error("删除评价失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "删除评价失败：订单服务暂时不可用");
    }

    @Override
    public ResponseResult queryPlatformInvoiceList(Map<String, Object> param) {
        log.error("查询发票列表失败：订单服务暂时不可用");
        return ResponseResult.fail(503, "查询发票列表失败：订单服务暂时不可用");
    }
}
