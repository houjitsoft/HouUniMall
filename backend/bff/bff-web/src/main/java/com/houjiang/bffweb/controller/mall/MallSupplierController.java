package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.GoodsServiceFeign;
import com.houjiang.bffweb.feign.OrderServiceFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.SupplierServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall商城供应商后台接口控制器
/**
 * 面向供应商的管理接口
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/supplier")
public class MallSupplierController {
    
    @Autowired
    private GoodsServiceFeign goodsServiceFeign;
    
    @Autowired
    private OrderServiceFeign orderServiceFeign;
    
    @Autowired
    private MallServiceFeign mallServiceFeign;
    
    @Autowired
    private SupplierServiceFeign supplierServiceFeign;
    
    /** ================= 认证接口 ================= */
    
    @PostMapping("/login")
    public ResponseResult login(@RequestBody Map<String, Object> param) {
        return ResponseResult.success("登录成功");
    }
    
    @PostMapping("/logout")
    public ResponseResult logout() {
        return ResponseResult.success("退出成功");
    }
    
    @GetMapping("/info")
    public ResponseResult getSupplierInfo() {
        return ResponseResult.success(new HashMap<>());
    }
    
    /** ================= 店铺管理 ================= */
    
    @GetMapping("/shop/info")
    public ResponseResult getShopInfo() {
        return supplierServiceFeign.getSupplierDetail(new HashMap<>());
    }
    
    @PostMapping("/shop/update")
    public ResponseResult updateShop(@RequestBody Map<String, Object> param) {
        return supplierServiceFeign.updateSupplier(param);
    }
    
    /** ================= 商品管理 ================= */
    
    @PostMapping("/goods/list")
    public ResponseResult getGoodsList(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.queryGoodsList(param);
    }
    
    @GetMapping("/goods/detail/{id}")
    public ResponseResult getGoodsDetail(@PathVariable Long id) {
        return goodsServiceFeign.getGoodsDetail(id);
    }
    
    @PostMapping("/goods/create")
    public ResponseResult createGoods(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.createGoods(param);
    }
    
    @PostMapping("/goods/update")
    public ResponseResult updateGoods(@RequestBody Map<String, Object> param) {
        return goodsServiceFeign.updateGoods(param);
    }
    
    @DeleteMapping("/goods/delete/{id}")
    public ResponseResult deleteGoods(@PathVariable Long id) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        return goodsServiceFeign.deleteGoods(param);
    }
    
    /** ================= 订单管理 ================= */
    
    @PostMapping("/order/list")
    public ResponseResult getOrderList(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryOrderList(param);
    }
    
    @GetMapping("/order/detail/{id}")
    public ResponseResult getOrderDetail(@PathVariable String id) {
        java.util.Map<String, Object> param = new java.util.HashMap<>();
        param.put("orderId", id);
        return orderServiceFeign.getOrderDetail(param);
    }
    
    @PostMapping("/order/ship")
    public ResponseResult shipOrder(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.updateOrderStatus(param);
    }
    
    @PostMapping("/order/refund")
    public ResponseResult processRefund(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.processRefund(param);
    }
    
    /** ================= 结算管理 ================= */
    
    @PostMapping("/settlement/list")
    public ResponseResult getSettlementList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getRebateRecordList(param);
    }
    
    @GetMapping("/settlement/detail/{id}")
    public ResponseResult getSettlementDetail(@PathVariable Long id) {
        return mallServiceFeign.getRebateRecordDetail(id);
    }
    
    @PostMapping("/settlement/withdraw")
    public ResponseResult withdraw(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.withdrawCommission(param);
    }
    
    /** ================= 经销商管理 ================= */
    
    @PostMapping("/distributor/list")
    public ResponseResult getDistributorList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributorList(param);
    }
    
    @PostMapping("/distributor/detail")
    public ResponseResult getDistributorDetail(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getDistributorDetail(param);
    }
    
    @PostMapping("/distributor/update")
    public ResponseResult updateDistributor(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.updateDistributorLevel(param);
    }
    
    /** ================= 统计 ================= */
    
    @GetMapping("/stats/dashboard")
    public ResponseResult getDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayOrders", 0);
        data.put("todaySales", 0);
        data.put("totalGoods", 0);
        data.put("pendingOrders", 0);
        return ResponseResult.success(data);
    }
}
