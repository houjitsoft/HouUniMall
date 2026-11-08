package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 订单服务Feign客户端
/**
 * 
 * @description 调用paas中台订单服务(order)的接口
 * @author Amos
 * @date 2024-12-20
 */
@FeignClient(name = "order", contextId = "orderServiceFeign", url = "${feign.order.url:}",
        path = "/", fallback = OrderServiceFeignFallback.class)
public interface OrderServiceFeign {

    // ================= 通用API调用接口 =================
    
    /**
     * 通用API调用方法
/**
     * @param path API路径
     * @param params 请求参数
     * @return 响应结果
     */
    @PostMapping("/{path}")
    ResponseResult callOrderAPI(@PathVariable("path") String path, @RequestBody Map<String, Object> params);

    // ================= 订单管理接口 =================
    
    /**
     * 查询订单列表
     */
    @PostMapping("/order/base/queryOrders")
    ResponseResult queryOrderList(@RequestBody Map<String, Object> param);

    @PostMapping("/order/base/queryOrdersByPC")
    ResponseResult queryOrdersByPC(@RequestBody Map<String, Object> param);

    /** 批量发货单导入（BFF 对外暴露为 /web/order/base/importShipmentsOrderExport）。 */
    @PostMapping(value = "/order/base/importShipmentsOrderExport", consumes = "multipart/form-data")
    ResponseResult importShipmentsOrderExport(@RequestPart("file") MultipartFile file,
                                               @RequestParam("token") String token);
    
    /**
     * 查询订单详情
     */
    @PostMapping("/order/base/enquireSoldOrders")
    ResponseResult getOrderDetail(@RequestBody Map<String, Object> param);
    
    /**
     * 创建订单
     * 路径已修正：paas 侧没有 /order/create/** 这组控制器（类级前缀只有 order / supply / order/base 等），
     * 原先的 /order/create/createOrders 一律 404「接口还未加载」，导致 /web/order/create、
     * /web/mall/order/settle、/web/mall/order/submit 三条下单通道全部 5001。
     * 真实入口是 OrderCreateController（类前缀 order）+ /appCreateOrders，且它内部就是调 createOrders。
     */
    @PostMapping("/order/appCreateOrders")
    ResponseResult createOrder(@RequestBody Map<String, Object> orderData);

    /**
     * 确认页预结算（价格计算）。
     *
     * <p>paas 侧 {@code OrderCreateController.calculatePrice} 的入参是 <b>字符串</b>（内部再 JSON 解析），
     * 且必须带 {@code token} 字段；BFF 用 text/plain 原样透传，由控制器补 token。
     * 这条路由此前<b>完全缺失</b>，导致前端 {@code /web/order/order/calculatePrice} 一律 20001，
     * 结算页从来跑不通。
     */
    @PostMapping(value = "/order/calculatePrice", consumes = "text/plain;charset=UTF-8")
    ResponseResult calculatePriceRaw(@RequestBody String message);

    /**
     * 提交订单（App 端）：前端传的是 AES 加密后的 Base64 原文，必须 text/plain 原样透传。
     */
    @PostMapping(value = "/order/appCreateOrders", consumes = "text/plain;charset=UTF-8")
    ResponseResult createOrderRaw(@RequestBody String para);
    
    /**
     * 更新订单（路径已修正：paas 侧无独立 updateOrder，统一走 updateByMap）
     */
    @PostMapping("/order/base/updateByMap")
    ResponseResult updateOrder(@RequestBody Map<String, Object> orderData);

    /**
     * 取消订单
     */
    @PostMapping("/order/base/cancelTheOrder")
    ResponseResult cancelOrder(@RequestBody Map<String, Object> param);

    /**
     * 删除订单
     */
    @PostMapping("/order/base/deleteOrder")
    ResponseResult deleteOrder(@RequestBody Map<String, Object> param);

    /**
     * 更新订单状态（路径已修正：paas 侧无 updateOrderStatus，统一走 updateByMap 传 state 字段）
     */
    @PostMapping("/order/base/updateByMap")
    ResponseResult updateOrderStatus(@RequestBody Map<String, Object> param);

    /**
     * 订单支付（paas 层 /order/paymentVocher/payOrder：订单校验 + 返回 payment 服务所需支付参数）
     */
    @PostMapping("/order/paymentVocher/payOrder")
    ResponseResult payOrder(@RequestBody Map<String, Object> param);

    /**
     * 查询订单统计（路径已修正：paas 侧无 queryOrderStatistics，等价于 ordersTotal）
     */
    @PostMapping("/order/base/ordersTotal")
    ResponseResult getOrderStatistics(@RequestBody Map<String, Object> param);
    
    /**
     * 确认收货
     */
    @PostMapping("/order/base/confirmTheGoods")
    ResponseResult confirmOrder(@RequestBody Map<String, Object> param);
    
    // ================= 购物车管理接口 =================
    
    /**
     * 查询购物车列表
     */
    @PostMapping("/order/userCart/queryShoppingCart")
    ResponseResult queryCartList(@RequestBody Map<String, Object> param);
    
    /**
     * 添加到购物车（路径已修正：paas 侧为 insertShoppingCart，保留 addToCart 名称兼容旧调用）
     */
    @PostMapping("/order/userCart/insertShoppingCart")
    ResponseResult addToCart(@RequestBody Map<String, Object> cartData);
    
    /**
     * 更新购物车
     */
    @PostMapping("/order/userCart/changeGoodsNum")
    ResponseResult updateCart(@RequestBody Map<String, Object> param);
    
    /**
     * 删除购物车商品
     */
    @PostMapping("/order/userCart/batchDeleteGoods")
    ResponseResult removeFromCart(@RequestBody Map<String, Object> param);

    /**
     * 购物车角标数量（H5 底栏红点）
     */
    @PostMapping("/order/userCart/selectCount")
    ResponseResult cartSelectCount(@RequestBody Map<String, Object> param);

    /**
     * H5 购物车加数量（老详情/购物车页调用）
     */
    @PostMapping("/order/userCart/h5/addGoodsNum")
    ResponseResult cartAddGoodsNum(@RequestBody Map<String, Object> param);

    /**
     * H5 购物车减数量（老详情/购物车页调用）
     */
    @PostMapping("/order/userCart/h5/reduceGoodsNum")
    ResponseResult cartReduceGoodsNum(@RequestBody Map<String, Object> param);
    
    // ================= 订单退款接口 =================
    
    /**
     * 申请退款
     */
    @PostMapping("/order/return/applyRefund")
    ResponseResult applyRefund(@RequestBody Map<String, Object> refundData);
    
    /**
     * 申请退货
     */
    @PostMapping("/order/return/applyReturn")
    ResponseResult applyReturn(@RequestBody Map<String, Object> returnData);
    
    /**
     * 查询退款记录（路径已修正：统一退款/退货列表走 listReturnOrderByRT）
     */
    @PostMapping("/order/return/listReturnOrderByRT")
    ResponseResult queryRefundList(@RequestBody Map<String, Object> param);

    /**
     * 处理退款（路径已修正：paas 侧无 processRefund，商家审批退款/退货统一走 check/afterSales）
     */
    @PostMapping("/order/return/check/afterSales")
    ResponseResult processRefund(@RequestBody Map<String, Object> param);

    /**
     * 订单发货
     */
    @PostMapping("/order/base/shipments")
    ResponseResult shipOrder(@RequestBody Map<String, Object> param);

    /**
     * 修改快递单号（重新发货）
     */
    @PostMapping("/order/base/changeShipments")
    ResponseResult changeShipments(@RequestBody Map<String, Object> param);

    /**
     * 查询物流信息
     */
    @PostMapping("/order/goods/queryLogistics")
    ResponseResult queryLogistics(@RequestBody Map<String, Object> param);

    /**
     * 商家备注（更新订单字段）
     */
    @PostMapping("/order/base/updateByMap")
    ResponseResult updateOrderByMap(@RequestBody Map<String, Object> param);

    /**
     * 管理端-售后服务列表（退货/退款列表）
     */
    @PostMapping("/order/return/listReturnOrderByRT")
    ResponseResult queryAfterSaleList(@RequestBody Map<String, Object> param);

    /**
     * 售后详情
     */
    @PostMapping("/order/return/returnDetail")
    ResponseResult queryAfterSaleDetail(@RequestBody Map<String, Object> param);

    /**
     * 退款详情
     */
    @PostMapping("/order/return/refundDetail")
    ResponseResult queryRefundDetail(@RequestBody Map<String, Object> param);

    /**
     * 商家审核退货/退款（路径已修正：paas 侧无 /forSellerApprove，统一走 /check/afterSales）
     */
    @PostMapping("/order/return/check/afterSales")
    ResponseResult sellerApproveAfterSale(@RequestBody Map<String, Object> param);

    /**
     * 商家审核退货与否（路径已修正：paas 侧无 /retForSeller/approve，统一走 /check/afterSales）
     */
    @PostMapping("/order/return/check/afterSales")
    ResponseResult sellerApproveReturn(@RequestBody Map<String, Object> param);

    /**
     * 查询订单统计（各状态数量）
     */
    @PostMapping("/order/base/ordersTotal")
    ResponseResult getOrdersTotal(@RequestBody Map<String, Object> param);

    /**
     * 管理端订单列表查询（platform - allOrders等价）
     */
    @PostMapping("/order/base/soldOrders")
    ResponseResult querySoldOrders(@RequestBody Map<String, Object> param);

    // ================= 订单评价接口 =================
    
    /**
     * 添加评价（路径已修正：paas 侧评价路由为 /order/goodsEvaluation/add）
     */
    @PostMapping("/order/goodsEvaluation/add")
    ResponseResult addReview(@RequestBody Map<String, Object> reviewData);
    
    /**
     * 查询评价列表（路径已修正：paas 侧对应 /order/goodsEvaluation/getAllComments）
     */
    @PostMapping("/order/goodsEvaluation/getAllComments")
    ResponseResult queryReviewList(@RequestBody Map<String, Object> param);
    
    /**
     * 回复评价（路径已修正：paas 侧对应 /order/goodsEvaluation/replyToComment）
     */
    @PostMapping("/order/goodsEvaluation/replyToComment")
    ResponseResult replyReview(@RequestBody Map<String, Object> param);
    
    // ================= 订单发票接口 =================
    
    /**
     * 申请发票（路径已修正：paas 侧为 /orderInvoiceInfo/apply/invoice）
     */
    @PostMapping("/orderInvoiceInfo/apply/invoice")
    ResponseResult applyInvoice(@RequestBody Map<String, Object> invoiceData);
    
    /**
     * 查询发票记录（路径已修正：paas 侧商家视角列表为 /orderInvoiceInfo/seller/query/list）
     */
    @PostMapping("/orderInvoiceInfo/seller/query/list")
    ResponseResult queryInvoiceList(@RequestBody Map<String, Object> param);
    
    // ================= 订单佣金接口 =================
    
    /**
     * 查询佣金记录（paas 层 /order/commission/queryCommissionList：精确查询当前用户的全部佣金记录）
     */
    @PostMapping("/order/commission/queryCommissionList")
    ResponseResult queryCommissionList(@RequestBody Map<String, Object> param);
    
    /**
     * 查询分销订单
     */
    @PostMapping("/order/commission/queryUserFxOrderList")
    ResponseResult queryDistributionOrders(@RequestBody Map<String, Object> param);

    // ================= 管理端订单扩展接口 =================

    /**
     * 查询投诉列表（路径已修正：paas 侧平台视角为 /order/complaints/platform/queryComplaints）
     */
    @PostMapping("/order/complaints/platform/queryComplaints")
    ResponseResult queryComplaintsList(@RequestBody Map<String, Object> param);

    /**
     * 查询投诉详情（路径已修正：paas 侧为 /order/complaints/platform/queryComplaintsDetail）
     */
    @PostMapping("/order/complaints/platform/queryComplaintsDetail")
    ResponseResult queryComplaintsDetail(@RequestBody Map<String, Object> param);

    /**
     * 处理投诉（路径已修正：paas 侧平台更新为 /order/complaints/platform/updateComplaint）
     */
    @PostMapping("/order/complaints/platform/updateComplaint")
    ResponseResult handleComplaints(@RequestBody Map<String, Object> param);

    /**
     * 平台退货列表（路径已修正：paas 侧为 /order/return/platform/list）
     */
    @PostMapping("/order/return/platform/list")
    ResponseResult queryPlatformReturnList(@RequestBody Map<String, Object> param);

    /**
     * 平台退货详情（路径已修正：paas 侧为 /order/return/platform/query/intervention）
     */
    @PostMapping("/order/return/platform/query/intervention")
    ResponseResult queryPlatformReturnDetail(@RequestBody Map<String, Object> param);

    /**
     * 平台处理退货（路径已修正：paas 侧为 /order/return/platform/approve）
     */
    @PostMapping("/order/return/platform/approve")
    ResponseResult processPlatformReturn(@RequestBody Map<String, Object> param);

    /**
     * 查询结算列表（路径已修正：paas 侧商家结算列表为 /order/settlement/business/list）
     */
    @PostMapping("/order/settlement/business/list")
    ResponseResult querySettlementList(@RequestBody Map<String, Object> param);

    /**
     * 查询结算详情（路径已修正：paas 侧为 /order/settlement/detail）
     */
    @PostMapping("/order/settlement/detail")
    ResponseResult querySettlementDetail(@RequestBody Map<String, Object> param);

    /**
     * 执行结算（paas 层 /order/settlement/manual/settle：复用 confirmSettlement 业务逻辑，支持按订单/店铺维度手动触发结算）
     */
    @PostMapping("/order/settlement/manual/settle")
    ResponseResult processSettlement(@RequestBody Map<String, Object> param);

    /**
     * 申请换货（路径已修正：paas 侧换货为独立 /orderBarter 模块，/applyForBarter）
     */
    @PostMapping("/orderBarter/applyForBarter")
    ResponseResult applyExchange(@RequestBody Map<String, Object> param);

    /**
     * 查询换货列表（路径已修正：paas 侧平台换货列表为 /orderBarter/platform/selectBarterOrderList）
     */
    @PostMapping("/orderBarter/platform/selectBarterOrderList")
    ResponseResult queryExchangeList(@RequestBody Map<String, Object> param);

    /**
     * 处理换货（路径已修正：paas 侧商家审核换货为 /orderBarter/approveBarter；平台审核为 /orderBarter/platform/approve）
     */
    @PostMapping("/orderBarter/approveBarter")
    ResponseResult processExchange(@RequestBody Map<String, Object> param);

    // ================= /web/order/* 前端兼容补齐 =================

    /**
     * 延迟收货（前端 /web/order/delay）
/**
     * 对应 paas: /order/base/delayToReceiveGoods
     */
    @PostMapping("/order/base/delayToReceiveGoods")
    ResponseResult delayConfirm(@RequestBody Map<String, Object> param);

    /**
     * 订单 Excel 导出（前端 /web/order/export）
/**
     * 对应 paas: /order/base/soldOrdersExp
     */
    @PostMapping("/order/base/soldOrdersExp")
    ResponseResult exportOrders(@RequestBody Map<String, Object> param);

    /**
     * 加入购物车（修正路径版本，对应 paas: /order/userCart/insertShoppingCart）
/**
     * 注：保留 addToCart 历史方法以避免破坏现存调用
     */
    @PostMapping("/order/userCart/insertShoppingCart")
    ResponseResult insertShoppingCart(@RequestBody Map<String, Object> cartData);

    /**
     * 清空购物车失效商品（前端 /web/order/cart/clear）
/**
     * 对应 paas: /order/userCart/deleteInvalidProductByCart
     */
    @PostMapping("/order/userCart/deleteInvalidProductByCart")
    ResponseResult clearShoppingCart(@RequestBody Map<String, Object> param);

    /**
     * 提交评价（前端 /web/order/review/submit）
/**
     * 对应 paas: /order/goodsEvaluation/add
     */
    @PostMapping("/order/goodsEvaluation/add")
    ResponseResult submitEvaluation(@RequestBody Map<String, Object> reviewData);

    /**
     * 查询我的评价（前端 /web/order/review/list）
/**
     * 对应 paas: /order/goodsEvaluation/queryMyEvaluation
     */
    @PostMapping("/order/goodsEvaluation/queryMyEvaluation")
    ResponseResult queryMyEvaluation(@RequestBody Map<String, Object> param);

    /**
     * 商家审核售后（同意/拒绝），覆盖 forSellerApprove
     */
    @PostMapping("/order/return/check/afterSales")
    ResponseResult checkAfterSales(@RequestBody Map<String, Object> param);

    // ================= v4 补充：缺失 Feign 方法 =================

    /**
     * 查询换货订单详情
/**
     * 对应 paas: /orderBarter/barterOrderDetail
     */
    @PostMapping("/orderBarter/barterOrderDetail")
    ResponseResult queryBarterDetail(@RequestBody Map<String, Object> param);

    /**
     * 查询发票详情（平台视角）
/**
     * 对应 paas: /orderInvoiceInfo/platform/query/detail
     */
    @PostMapping("/orderInvoiceInfo/platform/query/detail")
    ResponseResult queryInvoiceDetail(@RequestBody Map<String, Object> param);

    /**
     * 发票审核（商家开票/驳回）
/**
     * 对应 paas: /orderInvoiceInfo/seller/audit/invoice
     */
    @PostMapping("/orderInvoiceInfo/seller/audit/invoice")
    ResponseResult auditInvoice(@RequestBody Map<String, Object> param);

    /**
     * 删除买家评价
/**
     * 对应 paas: /order/goodsEvaluation/platForm/deleteBuyerComments
     */
    @PostMapping("/order/goodsEvaluation/platForm/deleteBuyerComments")
    ResponseResult deleteEvaluation(@RequestBody Map<String, Object> param);

    /**
     * 查询发票列表（平台视角）
/**
     * 对应 paas: /orderInvoiceInfo/platform/query/list
     */
    @PostMapping("/orderInvoiceInfo/platform/query/list")
    ResponseResult queryPlatformInvoiceList(@RequestBody Map<String, Object> param);
}
