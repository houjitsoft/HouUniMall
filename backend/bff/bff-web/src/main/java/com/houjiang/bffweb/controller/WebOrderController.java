package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.OrderServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 订单管理Web控制器
/**
 * 
 * @description BFF层订单服务聚合接口
 * @author Amos
 * @date 2024-12-20
 */
@Slf4j
@RestController
@RequestMapping("/web/order")
public class WebOrderController {

    @Autowired
    private OrderServiceFeign orderServiceFeign;

    // ================= 订单基础管理接口 =================

    /**
     * 订单列表查询
     *
     * <p>注意：order 服务的 queryOrders 从<b>请求体</b>读取 token 字段（该路径在 order 侧 JWT
     * 白名单中，不依赖 SecurityContext），而前端只在 Header 带 Authorization。BFF 层必须补
     * 充注入，否则 order 侧取不到 token，直接返回 10016「token失效,请重新登陆」。
     * 与 {@code /statistics} 同一处理，见 {@link #injectOrderToken}。
     */
    @PostMapping("/list")
    public ResponseResult getOrderList(@RequestBody Map<String, Object> params,
                                       HttpServletRequest request) {
        injectOrderToken(params, request);
        return orderServiceFeign.queryOrderList(params);
    }

    @PostMapping("/order/base/queryOrdersByPC")
    public ResponseResult queryOrdersByPC(@RequestBody Map<String, Object> params,
                                          HttpServletRequest request) {
        injectOrderToken(params, request);
        return orderServiceFeign.queryOrdersByPC(params);
    }

    /** 批量发货单导入，统一走 BFF，不再拼接旧 mall-order 地址。 */
    @PostMapping(value = "/base/importShipmentsOrderExport", consumes = "multipart/form-data")
    public ResponseResult importShipmentsOrderExport(@RequestPart("file") MultipartFile file,
                                                     @RequestParam("token") String token) {
        return orderServiceFeign.importShipmentsOrderExport(file, token);
    }

    /**
     * 订单详情查询
/**
     * @param params 参数，包含 orderId
     */
    @PostMapping("/detail")
    public ResponseResult getOrderDetail(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.getOrderDetail(params);
    }

    /**
     * 订单详情查询（路径参数兴容旧API）
/**
     * @param orderId 订单ID
     */
    @GetMapping("/detail/{orderId}")
    public ResponseResult getOrderDetailById(@PathVariable("orderId") String orderId) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("orderId", orderId);
        return orderServiceFeign.getOrderDetail(params);
    }

    /**
     * 确认页预结算（价格计算）。
     *
     * <p>前端 {@code /mall-order/order/calculatePrice} 经路径归一化后落到本路由。此前 BFF 完全
     * 没有这条路由，请求一律 20001 且从未到达 order 服务——结算页因此从来跑不通。
     * paas 侧入参是字符串（内部再 JSON 解析）并从请求体取 {@code token}，故此处补 token 后原样透传。
     */
    @PostMapping(value = "/order/calculatePrice",
            consumes = {"text/plain", "text/plain;charset=UTF-8", "application/json", "application/json;charset=UTF-8"})
    public ResponseResult calculatePrice(@RequestBody String message, HttpServletRequest request) {
        return orderServiceFeign.calculatePriceRaw(injectTokenIntoJson(message, request));
    }

    /**
     * 提交订单（App 端）：前端传 AES 加密后的 Base64 原文，text/plain 原样透传。
     */
    @PostMapping(value = "/order/appCreateOrders",
            consumes = {"text/plain", "text/plain;charset=UTF-8",
                    "application/json", "application/json;charset=UTF-8"})
    public ResponseResult appCreateOrders(@RequestBody String para) {
        return orderServiceFeign.createOrderRaw(para);
    }

    /**
     * 把请求头里的 Bearer token 注入到 JSON 文本的 {@code token} 字段（已存在则不覆盖）。
     * 用字符串拼接而不是反序列化，避免为一条透传路由引入额外的 JSON 依赖与字段丢失风险。
     */
    private String injectTokenIntoJson(String body, HttpServletRequest request) {
        String token = resolveClientToken(request);
        if (body == null || token == null) {
            return body;
        }
        if (body.contains("\"token\"")) {
            return body;
        }
        int open = body.indexOf('{');
        if (open < 0) {
            return body;
        }
        String rest = body.substring(open + 1);
        String tokenField = "\"token\":\"" + token + "\"";
        if (rest.trim().isEmpty() || rest.trim().startsWith("}")) {
            return body.substring(0, open + 1) + tokenField + body.substring(open + 1);
        }
        return body.substring(0, open + 1) + tokenField + "," + rest;
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public ResponseResult createOrder(@RequestBody Map<String, Object> orderData) {
        return orderServiceFeign.createOrder(orderData);
    }

    /**
     * 更新订单信息
     */
    @PostMapping("/update")
    public ResponseResult updateOrder(@RequestBody Map<String, Object> orderData) {
        return orderServiceFeign.updateOrder(orderData);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public ResponseResult cancelOrder(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.cancelOrder(params);
    }

    /**
     * 删除订单
     */
    @PostMapping("/delete")
    public ResponseResult deleteOrder(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.deleteOrder(params);
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirm")
    public ResponseResult confirmOrder(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.confirmOrder(params);
    }

    /**
     * 更新订单状态
     */
    @PostMapping("/updateStatus")
    public ResponseResult updateOrderStatus(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.updateOrderStatus(params);
    }

    /**
     * 订单发货（封装实际发货接口）
/**
     * 参数：orderId-订单id, orderShippingExpressId-快递公司id, orderShippingCode-快递单号
     */
    @PostMapping("/ship")
    public ResponseResult shipOrder(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.shipOrder(params);
    }

    /**
     * 修改快递单号（重新发货）
     */
    @PostMapping("/changeShip")
    public ResponseResult changeShipments(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.changeShipments(params);
    }

    /**
     * 商家备注订单
/**
     * 参数：orderId-订单id, orderShippingMessage-备注内容
     */
    @PostMapping("/remark")
    public ResponseResult remarkOrder(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.updateOrderByMap(params);
    }

    /**
     * 查询物流信息
/**
     * 参数：orderId-订单id
     */
    @PostMapping("/logistics")
    public ResponseResult queryLogistics(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryLogistics(params);
    }

    // ================= 订单统计接口 =================

    /**
     * 订单统计数据（各状态订单数量）
/**
     * 注意：order 服务的 ordersTotal 从请求体读取 token 字段，
     * 而前端只在 Header 带 Authorization，需要 BFF 层补充注入。
     */
    @PostMapping("/statistics")
    public ResponseResult getOrderStatistics(@RequestBody Map<String, Object> params,
                                             HttpServletRequest request) {
        // 从 Authorization 头提取 token 并注入请求体（兼容 order 服务老代码）
        injectOrderToken(params, request);
        return safeGetOrdersTotal(params);
    }

    /**
     * 把 Authorization 头里的 JWT 注入请求体，供 order 服务的老接口从 body 取 token。
     *
     * <p>order 服务大量历史接口（queryOrders / ordersTotal 等）以 {@code map.get("token")}
     * 取身份，且这些路径在 order 侧 JWT 白名单中不建立 SecurityContext；而前端统一只在
     * Header 带 {@code Authorization: Bearer xxx}。缺失注入会表现为
     * 10016「token失效,请重新登陆」。
     *
     * <p>只在请求体未自带 token 时注入，避免覆盖调用方显式传入的值。
     */
    private void injectOrderToken(Map<String, Object> params, HttpServletRequest request) {
        String token = resolveClientToken(request);
        if (params == null || token == null || params.containsKey("token")) {
            return;
        }
        params.put("token", token);
    }

    /**
     * 从请求头解析客户端 JWT：优先 Authorization: Bearer，其次老 H5 的自定义头 Token。
     */
    private static String resolveClientToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        String legacy = request.getHeader("Token");
        if (legacy == null || legacy.isBlank()) {
            legacy = request.getHeader("token");
        }
        if (legacy != null && !legacy.isBlank() && !"undefined".equals(legacy) && !"null".equals(legacy)) {
            return legacy.trim();
        }
        return null;
    }

    // ================= 购物车（老 H5 契约） =================
    // uniapp 购物车页与详情页调的是 /web/order/userCart/**，BFF 此前只别名了
    // insertShoppingCart 一条，其余路由完全缺失 → 一律 20001「请求的资源不存在」，
    // 表现是购物车页永远空列表、改数量/删除静默失败。
    // order 侧这些方法都从<b>请求体</b>取 token（路径在 JWT 白名单内），故统一补齐。

    @PostMapping("/userCart/queryShoppingCart")
    public ResponseResult queryShoppingCart(@RequestBody(required = false) Map<String, Object> params,
                                            HttpServletRequest request) {
        Map<String, Object> body = params == null ? new LinkedHashMap<>() : params;
        injectOrderToken(body, request);
        return orderServiceFeign.queryCartList(body);
    }

    @PostMapping("/userCart/changeGoodsNum")
    public ResponseResult changeGoodsNum(@RequestBody(required = false) Map<String, Object> params,
                                         HttpServletRequest request) {
        Map<String, Object> body = params == null ? new LinkedHashMap<>() : params;
        injectOrderToken(body, request);
        return orderServiceFeign.updateCart(body);
    }

    @PostMapping("/userCart/h5/addGoodsNum")
    public ResponseResult cartAddGoodsNum(@RequestBody(required = false) Map<String, Object> params,
                                          HttpServletRequest request) {
        Map<String, Object> body = params == null ? new LinkedHashMap<>() : params;
        injectOrderToken(body, request);
        return orderServiceFeign.cartAddGoodsNum(body);
    }

    @PostMapping("/userCart/h5/reduceGoodsNum")
    public ResponseResult cartReduceGoodsNum(@RequestBody(required = false) Map<String, Object> params,
                                             HttpServletRequest request) {
        Map<String, Object> body = params == null ? new LinkedHashMap<>() : params;
        injectOrderToken(body, request);
        return orderServiceFeign.cartReduceGoodsNum(body);
    }

    @PostMapping("/userCart/batchDeleteGoods")
    public ResponseResult batchDeleteGoods(@RequestBody(required = false) Map<String, Object> params,
                                           HttpServletRequest request) {
        Map<String, Object> body = params == null ? new LinkedHashMap<>() : params;
        injectOrderToken(body, request);
        return orderServiceFeign.removeFromCart(body);
    }

    @PostMapping("/userCart/deleteInvalidProductByCart")
    public ResponseResult deleteInvalidProductByCart(@RequestBody(required = false) Map<String, Object> params,
                                                     HttpServletRequest request) {
        Map<String, Object> body = params == null ? new LinkedHashMap<>() : params;
        injectOrderToken(body, request);
        return orderServiceFeign.clearShoppingCart(body);
    }

    @PostMapping("/userCart/selectCount")
    public ResponseResult cartSelectCount(@RequestBody(required = false) Map<String, Object> params,
                                          HttpServletRequest request) {
        Map<String, Object> body = params == null ? new LinkedHashMap<>() : params;
        injectOrderToken(body, request);
        return orderServiceFeign.cartSelectCount(body);
    }

    // ================= 售后服务接口 =================

    /**
     * 售后列表查询
/**
     * 参数：type-0退货 1退款, 分页相关参数
     */
    @PostMapping("/aftersale/list")
    public ResponseResult getAfterSaleList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryAfterSaleList(params);
    }

    /**
     * 售后详情查询
/**
     * 参数：orderReturnId-售后单id
     */
    @PostMapping("/aftersale/detail")
    public ResponseResult getAfterSaleDetail(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryAfterSaleDetail(params);
    }

    /**
     * 售后处理（商家审核退款）
/**
     * 参数：orderReturnId-售后单id, type-1同意 0拒绝, remark-备注
     */
    @PostMapping("/aftersale/approve")
    public ResponseResult approveAfterSale(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.sellerApproveAfterSale(params);
    }

    /**
     * 售后处理（商家审核退货）
/**
     * 参数：orderReturnId-售后单id, type-1同意 0拒绝
     */
    @PostMapping("/aftersale/approveReturn")
    public ResponseResult approveReturnAfterSale(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.sellerApproveReturn(params);
    }

    /**
     * 申请售后
     */
    @PostMapping("/aftersale/apply")
    public ResponseResult applyAfterSale(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.applyRefund(params);
    }

    /**
     * 更新售后状态（处理退款）
     */
    @PostMapping("/aftersale/update")
    public ResponseResult updateAfterSaleStatus(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.processRefund(params);
    }

    // ================= 购物车管理接口=================

    /**
     * 购物车列表
     */
    @PostMapping("/cart/list")
    public ResponseResult getCartList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryCartList(params);
    }

    /**
     * 添加到购物车
     */
    @PostMapping("/cart/add")
    public ResponseResult addToCart(@RequestBody Map<String, Object> cartData) {
        return orderServiceFeign.addToCart(cartData);
    }

    /**
     * 更新购物车商品数量
     */
    @PostMapping("/cart/update")
    public ResponseResult updateCart(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.updateCart(params);
    }

    /**
     * 删除购物车商品
     */
    @PostMapping("/cart/remove")
    public ResponseResult removeFromCart(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.removeFromCart(params);
    }

    // ================= 订单退换货接口 =================

    /**
     * 申请退款
     */
    @PostMapping("/refund/apply")
    public ResponseResult applyRefund(@RequestBody Map<String, Object> refundData) {
        return orderServiceFeign.applyRefund(refundData);
    }

    /**
     * 申请退货
     */
    @PostMapping("/return/apply")
    public ResponseResult applyReturn(@RequestBody Map<String, Object> returnData) {
        return orderServiceFeign.applyReturn(returnData);
    }

    /**
     * 查询退换货记录
     */
    @PostMapping("/return/list")
    public ResponseResult getReturnList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryRefundList(params);
    }

    // ================= 订单评价接口 =================

    /**
     * 添加商品评价
     */
    @PostMapping("/evaluation/add")
    public ResponseResult addEvaluation(@RequestBody Map<String, Object> evaluationData) {
        return orderServiceFeign.addReview(evaluationData);
    }

    /**
     * 查询商品评价列表
     */
    @PostMapping("/evaluation/list")
    public ResponseResult getEvaluationList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryReviewList(params);
    }

    /**
     * 回复商品评价
     */
    @PostMapping("/evaluation/reply")
    public ResponseResult replyEvaluation(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.replyReview(params);
    }

    // ================= 订单发票接口 =================

    /**
     * 申请发票
     */
    @PostMapping("/invoice/apply")
    public ResponseResult applyInvoice(@RequestBody Map<String, Object> invoiceData) {
        return orderServiceFeign.applyInvoice(invoiceData);
    }

    /**
     * 查询发票记录
     */
    @PostMapping("/invoice/list")
    public ResponseResult getInvoiceList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryInvoiceList(params);
    }

    // ================= 订单佣金接口 =================

    /**
     * 查询分销佣金
     */
    @PostMapping("/commission/list")
    public ResponseResult getCommissionList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryCommissionList(params);
    }

    /**
     * 查询分销订单
     */
    @PostMapping("/commission/orders")
    public ResponseResult getCommissionOrders(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryDistributionOrders(params);
    }

    // ================= 异常订单管理接口 =================

    /**
     * 异常订单列表
     */
    @PostMapping("/exception/list")
    public ResponseResult getExceptionOrders(@RequestBody Map<String, Object> params) {
        try {
            // TODO: 调用order服务获取异常订单列表
            // 临时返回模拟数据
            java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Map<String, Object> order = new java.util.HashMap<>();
                order.put("id", "EXC" + i);
                order.put("orderNo", "SO202401" + String.format("%06d", 1000 + i));
                order.put("exceptionType", i % 4 == 0 ? "payment_timeout" : 
                          i % 4 == 1 ? "address_error" : 
                          i % 4 == 2 ? "stock_shortage" : "system_error");
                order.put("exceptionDesc", "异常描述信息 " + i);
                order.put("customerName", "客户" + i);
                order.put("orderAmount", 199.00 + i * 10);
                order.put("exceptionTime", "2024-01-0" + (i % 9 + 1) + " 10:" + String.format("%02d", i * 5) + ":00");
                order.put("handleStatus", i % 3 == 0 ? "pending" : 
                          i % 3 == 1 ? "processing" : "completed");
                order.put("handler", i % 3 == 2 ? "管理员" : "");
                list.add(order);
            }
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("list", list);
            result.put("total", 100);
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.fail(500, "获取异常订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 异常订单统计
     */
    @PostMapping("/exception/stats")
    public ResponseResult getExceptionStats() {
        try {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("paymentTimeout", 15);
            stats.put("addressError", 8);
            stats.put("stockShortage", 12);
            stats.put("systemError", 5);
            
            return ResponseResult.success(stats);
        } catch (Exception e) {
            return ResponseResult.fail(500, "获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 处理异常订单
     */
    @PostMapping("/exception/process")
    public ResponseResult processException(@RequestBody Map<String, Object> params) {
        try {
            String orderId = (String) params.get("orderId");
            String solution = (String) params.get("solution");
            String remark = (String) params.get("remark");
            
            if (orderId == null || solution == null || remark == null) {
                return ResponseResult.fail(400, "参数不完整");
            }
            
            // TODO: 调用order服务处理异常订单
            // 记录处理日志
            
            return ResponseResult.success("处理成功");
        } catch (Exception e) {
            return ResponseResult.fail(500, "处理异常订单失败: " + e.getMessage());
        }
    }

    /**
     * 标记异常已解决
     */
    @PostMapping("/exception/resolve")
    public ResponseResult resolveException(@RequestBody Map<String, Object> params) {
        try {
            String orderId = (String) params.get("orderId");
            if (orderId == null) {
                return ResponseResult.fail(400, "订单ID不能为空");
            }
            
            // TODO: 更新异常订单状态为已解决
            
            return ResponseResult.success("标记成功");
        } catch (Exception e) {
            return ResponseResult.fail(500, "标记失败: " + e.getMessage());
        }
    }

    /**
     * 忽略异常
     */
    @PostMapping("/exception/ignore")
    public ResponseResult ignoreException(@RequestBody Map<String, Object> params) {
        try {
            String orderId = (String) params.get("orderId");
            if (orderId == null) {
                return ResponseResult.fail(400, "订单ID不能为空");
            }
            
            // TODO: 更新异常订单状态为已忽略
            
            return ResponseResult.success("操作成功");
        } catch (Exception e) {
            return ResponseResult.fail(500, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取异常处理日志
     */
    @PostMapping("/exception/logs")
    public ResponseResult getExceptionLogs(@RequestBody Map<String, Object> params) {
        try {
            String orderId = (String) params.get("orderId");
            if (orderId == null) {
                return ResponseResult.fail(400, "订单ID不能为空");
            }
            
            // TODO: 查询异常处理日志
            // 临时返回模拟数据
            java.util.List<Map<String, Object>> logs = new java.util.ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                Map<String, Object> log = new java.util.HashMap<>();
                log.put("action", i == 1 ? "异常发现" : i == 2 ? "处理中" : "已解决");
                log.put("content", "日志详情 " + i);
                log.put("operator", "管理员");
                log.put("createTime", "2024-01-0" + i + " 10:00:00");
                logs.add(log);
            }
            
            return ResponseResult.success(logs);
        } catch (Exception e) {
            return ResponseResult.fail(500, "获取日志失败: " + e.getMessage());
        }
    }

    // ================= 退款管理接口 =================

    /**
     * 退款列表（调用 paas 真实接口）
     */
    @PostMapping("/refund/list")
    public ResponseResult getRefundList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryRefundList(params);
    }

    /**
     * 退款统计（复用订单统计接口）
/**
     * Z 闭环修复：原签名无 @RequestBody，前端 getRefundMoney(queryParams) 传递的筛选参数被丢弃。
     * 调用方 refund_record.vue#L225 期望按筛选条件返回退款金额统计，加 @RequestBody 透传参数。
     */
    @PostMapping("/refund/stats")
    public ResponseResult getRefundStats(@RequestBody(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        return orderServiceFeign.getOrdersTotal(params);
    }

    /**
     * 审核通过（调用 paas 售后审核接口，type=1）
     */
    @PostMapping("/refund/approve")
    public ResponseResult approveRefund(@RequestBody Map<String, Object> params) {
        params.put("type", 1);
        return orderServiceFeign.sellerApproveAfterSale(params);
    }

    /**
     * 审核拒绝（调用 paas 售后审核接口，type=0）
     */
    @PostMapping("/refund/reject")
    public ResponseResult rejectRefund(@RequestBody Map<String, Object> params) {
        params.put("type", 0);
        return orderServiceFeign.sellerApproveAfterSale(params);
    }

    /**
     * 执行退款（调用 paas 退款处理接口）
     */
    @PostMapping("/refund/process")
    public ResponseResult processRefund(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.processRefund(params);
    }

    /**
     * 退款详情（调用 paas 售后详情接口）
     */
    @PostMapping("/refund/detail")
    public ResponseResult getRefundDetail(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryAfterSaleDetail(params);
    }

    // ================= 物流管理接口（替换 mock） =================

    /**
     * 物流列表（查询已发货订单）
     */
    @PostMapping("/logistics/list")
    public ResponseResult getLogisticsList(@RequestBody Map<String, Object> params) {
        params.put("orderStatus", 4);
        return orderServiceFeign.querySoldOrders(params);
    }

    /**
     * 物流统计
     */
    @PostMapping("/logistics/stats")
    public ResponseResult getLogisticsStats() {
        Map<String, Object> params = new java.util.HashMap<>();
        return orderServiceFeign.getOrdersTotal(params);
    }

    /**
     * 物流跟踪（接入真实物流查询）
     */
    @PostMapping("/logistics/track")
    public ResponseResult trackLogistics(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryLogistics(params);
    }

    // ================= 订单统计接口 =================

    /**
     * 订单统计概览（调用 paas 统计接口）
     */
    @PostMapping("/statistics/overview")
    public ResponseResult getStatisticsOverview(@RequestBody Map<String, Object> params) {
        return safeGetOrdersTotal(params);
    }

    /**
     * 订单趋势统计（paas 无日期趋势接口，保留 mock）
     */
    @PostMapping("/statistics/trend")
    public ResponseResult getStatisticsTrend(@RequestBody Map<String, Object> params) {
        try {
            Map<String, Object> trend = new java.util.HashMap<>();
            java.util.List<String> dates = new java.util.ArrayList<>();
            java.util.List<Integer> orderCounts = new java.util.ArrayList<>();
            java.util.List<Double> orderAmounts = new java.util.ArrayList<>();
            for (int i = 1; i <= 7; i++) {
                dates.add("2024-01-" + String.format("%02d", i));
                orderCounts.add(50 + (int)(Math.random() * 50));
                orderAmounts.add(5000.0 + Math.random() * 5000);
            }
            trend.put("dates", dates);
            trend.put("orderCounts", orderCounts);
            trend.put("orderAmounts", orderAmounts);
            return ResponseResult.success(trend);
        } catch (Exception e) {
            return ResponseResult.fail(500, "获取趋势统计失败: " + e.getMessage());
        }
    }

    /**
     * 订单状态分布（调用 paas 统计接口）
     */
    @PostMapping("/statistics/status")
    public ResponseResult getStatusDistribution(@RequestBody Map<String, Object> params) {
        return safeGetOrdersTotal(params);
    }

    /**
     * TOP排行统计（paas 无 TOP 接口，保留 mock）
     */
    @PostMapping("/statistics/top")
    public ResponseResult getTopStatistics(@RequestBody Map<String, Object> params) {
        try {
            Map<String, Object> topData = new java.util.HashMap<>();
            java.util.List<Map<String, Object>> topGoods = new java.util.ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Map<String, Object> goods = new java.util.HashMap<>();
                goods.put("goodsName", "商品 " + i);
                goods.put("salesCount", 500 - i * 30);
                goods.put("salesAmount", 50000.0 - i * 3000);
                topGoods.add(goods);
            }
            java.util.List<Map<String, Object>> topCustomers = new java.util.ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Map<String, Object> customer = new java.util.HashMap<>();
                customer.put("customerName", "客户 " + i);
                customer.put("orderCount", 50 - i * 3);
                customer.put("totalAmount", 20000.0 - i * 1500);
                topCustomers.add(customer);
            }
            topData.put("topGoods", topGoods);
            topData.put("topCustomers", topCustomers);
            return ResponseResult.success(topData);
        } catch (Exception e) {
            return ResponseResult.fail(500, "获取TOP统计失败: " + e.getMessage());
        }
    }

    // ================= 新增端点（v4） =================

    /**
     * 订单支付（校验订单并获取支付参数）
/**
     * 参数：orderId-订单ID, payType-支付方式
     * 通道：paas /order/paymentVocher/payOrder
     */
    @PostMapping("/pay")
    public ResponseResult payOrder(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.payOrder(params);
    }

    /**
     * 延迟收货
     */
    @PostMapping("/delay")
    public ResponseResult delayReceive(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.delayConfirm(params);
    }

    /**
     * 订单导出
     */
    @PostMapping("/export")
    public ResponseResult exportOrderList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.exportOrders(params);
    }

    // ================= 换货管理接口 =================

    /**
     * 换货列表
     */
    @PostMapping("/exchange/list")
    public ResponseResult getExchangeList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryExchangeList(params);
    }

    /**
     * 换货详情
     */
    @PostMapping("/exchange/detail")
    public ResponseResult getExchangeDetail(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryBarterDetail(params);
    }

    /**
     * 换货审核（商家同意/拒绝）
     */
    @PostMapping("/exchange/approve")
    public ResponseResult approveExchange(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.processExchange(params);
    }

    // ================= 投诉管理接口 =================

    /**
     * 投诉列表
     */
    @PostMapping("/complaint/list")
    public ResponseResult getComplaintList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryComplaintsList(params);
    }

    /**
     * 投诉详情
     */
    @PostMapping("/complaint/detail")
    public ResponseResult getComplaintDetail(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryComplaintsDetail(params);
    }

    /**
     * 处理投诉
     */
    @PostMapping("/complaint/handle")
    public ResponseResult handleComplaint(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.handleComplaints(params);
    }

    // ================= 平台介入接口 =================

    /**
     * 平台介入列表
     */
    @PostMapping("/platform/return/list")
    public ResponseResult getPlatformReturnList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryPlatformReturnList(params);
    }

    /**
     * 平台介入详情
     */
    @PostMapping("/platform/return/detail")
    public ResponseResult getPlatformReturnDetail(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryPlatformReturnDetail(params);
    }

    /**
     * 平台介入处理
     */
    @PostMapping("/platform/return/process")
    public ResponseResult processPlatformReturn(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.processPlatformReturn(params);
    }

    // ================= 结算管理接口 =================

    /**
     * 结算列表
     */
    @PostMapping("/settlement/list")
    public ResponseResult getSettlementList(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.querySettlementList(params);
    }

    /**
     * 结算详情
     */
    @PostMapping("/settlement/detail")
    public ResponseResult getSettlementDetail(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.querySettlementDetail(params);
    }

    /**
     * 手动结算
     */
    @PostMapping("/settlement/process")
    public ResponseResult processSettlement(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.processSettlement(params);
    }

    // ================= 发票管理接口 =================

    /**
     * 发票详情
     */
    @PostMapping("/invoice/detail")
    public ResponseResult getInvoiceDetail(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.queryInvoiceDetail(params);
    }

    /**
     * 发票审核（开票/驳回）
     */
    @PostMapping("/invoice/audit")
    public ResponseResult auditInvoice(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.auditInvoice(params);
    }

    // ================= 评价管理接口 =================

    /**
     * 删除评价
     */
    @PostMapping("/evaluation/delete")
    public ResponseResult deleteEvaluation(@RequestBody Map<String, Object> params) {
        return orderServiceFeign.deleteEvaluation(params);
    }

    /**
     * 订单统计降级：circuitbreaker 关闭时 Feign fallback 不生效，统计类接口在此兜底。
/**
     *
     * @param params 查询参数（含 token）
     * @return 正常统计或空统计（order 未启动时不抛 5001）
     */
    private ResponseResult safeGetOrdersTotal(Map<String, Object> params) {
        try {
            return orderServiceFeign.getOrdersTotal(params);
        } catch (Exception e) {
            log.warn("订单统计降级（order 不可达）: {}", e.getMessage());
            return ResponseResult.success(emptyOrderStatistics());
        }
    }

    /**
     * 空订单统计数据（与 OrderServiceFeignFallback 保持一致）
/**
     *
     * @return 全 0 统计 Map
     */
    private Map<String, Object> emptyOrderStatistics() {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("totalOrders", 0);
        empty.put("pendingOrders", 0);
        empty.put("completedOrders", 0);
        empty.put("totalAmount", 0);
        empty.put("todayOrders", 0);
        empty.put("todayAmount", 0);
        return empty;
    }
}
