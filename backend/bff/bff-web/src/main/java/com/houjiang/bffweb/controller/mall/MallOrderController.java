package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.OrderServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall商城订单接口控制器
 *
 * 面向PC前台用户的订单相关接口。
 * 注意：paas order 服务统一从请求体 map.get("token") 取 token，
 * 前端 token 放在请求头，这里通过 {@link MallTokenSupport} 统一注入；
 * 列表/详情等接口前端走 GET + query 参数，这里合并到请求体后转发。
 *
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/order")
public class MallOrderController {

    @Autowired
    private OrderServiceFeign orderServiceFeign;

    /**
     * 订单结算（确认页预结算）
     * 前端 /mall.order/settle 映射至此；下游创建/结算统一走 createOrders
     */
    @PostMapping("/settle")
    public ResponseResult settleOrder(@RequestBody Map<String, Object> param,
                                      HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "orderId", "id");
        return orderServiceFeign.createOrder(body);
    }

    /**
     * 确认订单（预结算）
     */
    @PostMapping("/confirm")
    public ResponseResult confirmOrder(@RequestBody Map<String, Object> param,
                                       HttpServletRequest request) {
        return orderServiceFeign.createOrder(MallTokenSupport.withToken(param, request));
    }

    /**
     * 提交订单
     */
    @PostMapping("/submit")
    public ResponseResult submitOrder(@RequestBody Map<String, Object> param,
                                      HttpServletRequest request) {
        return orderServiceFeign.createOrder(MallTokenSupport.withToken(param, request));
    }

    /**
     * 获取订单详情（前端 GET /mall.order/detail?id= 映射至此，兼容 query 参数）
     */
    @RequestMapping(value = "/detail", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getOrderDetail(@RequestBody(required = false) Map<String, Object> param,
                                         @RequestParam(required = false) Map<String, Object> query,
                                         HttpServletRequest request) {
        Map<String, Object> body = mergeBody(param, query, request);
        MallTokenSupport.putIfAbsentFrom(body, "orderId", "id");
        if (body.get("orderId") == null) {
            return ResponseResult.error("订单ID不能为空");
        }
        return orderServiceFeign.getOrderDetail(body);
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public ResponseResult payOrder(@RequestBody Map<String, Object> param,
                                   HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "orderId", "id");
        return orderServiceFeign.payOrder(body);
    }

    /**
     * 获取用户订单列表（前端 GET /mall.order/lists 映射至此，兼容 query 参数）
     */
    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseResult getOrderList(@RequestBody(required = false) Map<String, Object> param,
                                       @RequestParam(required = false) Map<String, Object> query,
                                       HttpServletRequest request) {
        return orderServiceFeign.queryOrderList(mergeBody(param, query, request));
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public ResponseResult cancelOrder(@RequestBody Map<String, Object> param,
                                      HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "orderId", "id");
        return orderServiceFeign.cancelOrder(body);
    }

    /**
     * 确认收货
     */
    @PostMapping("/receive")
    public ResponseResult receiveOrder(@RequestBody Map<String, Object> param,
                                       HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "orderId", "id");
        return orderServiceFeign.confirmOrder(body);
    }

    /**
     * 删除订单（前端 /mall.order/delete 映射至此）
     */
    @PostMapping("/delete")
    public ResponseResult deleteOrder(@RequestBody Map<String, Object> param,
                                      HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "orderId", "id");
        return orderServiceFeign.deleteOrder(body);
    }

    /**
     * 查询物流（前端 /mall.order/logistics 映射至此）
     */
    @PostMapping("/logistics")
    public ResponseResult queryLogistics(@RequestBody Map<String, Object> param,
                                         HttpServletRequest request) {
        Map<String, Object> body = MallTokenSupport.withToken(param, request);
        MallTokenSupport.putIfAbsentFrom(body, "orderId", "id");
        return orderServiceFeign.queryLogistics(body);
    }

    /**
     * 合并 POST body 与 GET query 参数（body 优先），并注入 token
     */
    private Map<String, Object> mergeBody(Map<String, Object> param,
                                          Map<String, Object> query,
                                          HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        if (query != null) {
            body.putAll(query);
        }
        if (param != null) {
            body.putAll(param);
        }
        return MallTokenSupport.withToken(body, request);
    }
}
