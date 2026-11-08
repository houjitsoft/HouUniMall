package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.UserServiceFeign;
import com.houjiang.bffweb.feign.OrderServiceFeign;
import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.feign.ActivityServiceFeign;
import com.houjiang.bffweb.feign.UserAddressServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall商城会员中心接口控制器
/**
 * 面向注册用户的个人中心接口
 * 
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/member")
public class MallMemberController {

    @Autowired
    private UserAddressServiceFeign userAddressServiceFeign;
    
    @Autowired
    private UserServiceFeign userServiceFeign;
    
    @Autowired
    private OrderServiceFeign orderServiceFeign;
    
    @Autowired
    private MallServiceFeign mallServiceFeign;
    
    @Autowired
    private ActivityServiceFeign activityServiceFeign;
    
    /** ================= 个人信息 ================= */
    
    @GetMapping("/info")
    public ResponseResult getMemberInfo() {
        return userServiceFeign.getUserInfo(new HashMap<>());
    }
    
    @PostMapping("/update")
    public ResponseResult updateMemberInfo(@RequestBody Map<String, Object> param) {
        return userServiceFeign.updateUserInfo(param);
    }
    
    @PostMapping("/password")
    public ResponseResult changePassword(@RequestBody Map<String, Object> param) {
        return userServiceFeign.changePassword(param);
    }
    
    /** ================= 收货地址 ================= */
    
    @GetMapping("/address/list")
    public ResponseResult getAddressList(jakarta.servlet.http.HttpServletRequest request) {
        return userAddressServiceFeign.getAddressList(addressParam(null, request));
    }
    
    @PostMapping("/address/create")
    public ResponseResult createAddress(@RequestBody Map<String, Object> param) {
        return userAddressServiceFeign.addAddress(addressParam(param, null));
    }
    
    @PostMapping("/address/update")
    public ResponseResult updateAddress(@RequestBody Map<String, Object> param) {
        return userAddressServiceFeign.updateAddress(addressParam(param, null));
    }
    
    @DeleteMapping("/address/delete/{id}")
    public ResponseResult deleteAddress(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest request) {
        Map<String,String> p = addressParam(new HashMap<>(), request); p.put("userAddressId", String.valueOf(id));
        return userAddressServiceFeign.deleteAddress(p);
    }
    
    @PostMapping("/address/default/{id}")
    public ResponseResult setDefaultAddress(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest request) {
        Map<String,String> p = addressParam(new HashMap<>(), request); p.put("userAddressId", String.valueOf(id));
        return userAddressServiceFeign.setDefaultAddress(p);
    }
    
    /** ================= 售后服务 ================= */
    
    @PostMapping("/aftersale/list")
    public ResponseResult getAftersaleList(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.queryRefundList(param);
    }
    
    @PostMapping("/aftersale/apply")
    public ResponseResult applyAftersale(@RequestBody Map<String, Object> param) {
        return orderServiceFeign.applyRefund(param);
    }
    
    @GetMapping("/aftersale/detail/{id}")
    public ResponseResult getAftersaleDetail(@PathVariable Long id) {
        Map<String, Object> param = new HashMap<>();
        param.put("id", id);
        return orderServiceFeign.queryRefundList(param);
    }
    
    /** ================= 财产中心 ================= */
    
    @GetMapping("/balance")
    public ResponseResult getBalance() {
        Map<String, Object> data = new HashMap<>();
        data.put("balance", 0);
        data.put("frozen", 0);
        return ResponseResult.success(data);
    }
    
    @GetMapping("/points")
    public ResponseResult getPoints() {
        Map<String, Object> data = new HashMap<>();
        data.put("points", 0);
        data.put("expiring", 0);
        return ResponseResult.success(data);
    }
    
    @PostMapping("/coupons")
    public ResponseResult getCoupons(@RequestBody Map<String, Object> param) {
        return activityServiceFeign.queryActivityList(param);
    }
    
    /** ================= 收藏 ================= */
    
    @PostMapping("/collect/list")
    public ResponseResult getCollectList(@RequestBody Map<String, Object> param) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/collect/add")
    public ResponseResult addCollect(@RequestBody Map<String, Object> param) {
        return ResponseResult.success("收藏成功");
    }
    
    @DeleteMapping("/collect/remove/{id}")
    public ResponseResult removeCollect(@PathVariable Long id) {
        return ResponseResult.success("取消收藏");
    }
    
    /** ================= 足迹 ================= */
    
    @PostMapping("/footprint/list")
    public ResponseResult getFootprintList(@RequestBody Map<String, Object> param) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @PostMapping("/footprint/clear")
    public ResponseResult clearFootprint() {
        return ResponseResult.success("清空成功");
    }
    
    /** ================= 消息 ================= */
    
    @PostMapping("/message/list")
    public ResponseResult getMessageList(@RequestBody Map<String, Object> param) {
        return ResponseResult.success(new java.util.ArrayList<>());
    }
    
    @GetMapping("/message/detail/{id}")
    public ResponseResult getMessageDetail(@PathVariable Long id) {
        return ResponseResult.success(new HashMap<>());
    }
    
    @PostMapping("/message/read/{id}")
    public ResponseResult markMessageRead(@PathVariable Long id) {
        return ResponseResult.success("已读");
    }
    
    /** ================= 分销中心 ================= */
    
    @GetMapping("/distribution/info")
    public ResponseResult getDistributionInfo() {
        return mallServiceFeign.getDistributorList(new HashMap<>());
    }
    
    @PostMapping("/distribution/commission")
    public ResponseResult getCommissionList(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.getCommissionRecordList(param);
    }
    
    @PostMapping("/distribution/withdraw")
    public ResponseResult withdrawCommission(@RequestBody Map<String, Object> param) {
        return mallServiceFeign.withdrawCommission(param);
    }

    private Map<String, String> addressParam(Map<String, Object> source, jakarta.servlet.http.HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        if (source != null) source.forEach((k, v) -> { if (v != null) result.put(k, String.valueOf(v)); });
        if (request != null) {
            String token = MallTokenSupport.resolveToken(request);
            if (token != null) result.put("token", token);
        }
        if (result.containsKey("id") && !result.containsKey("userAddressId")) result.put("userAddressId", result.get("id"));
        if (result.containsKey("contact") && !result.containsKey("userAddressContact")) result.put("userAddressContact", result.get("contact"));
        if (result.containsKey("mobile") && !result.containsKey("userAddressPhone")) result.put("userAddressPhone", result.get("mobile"));
        if (result.containsKey("address") && !result.containsKey("userAddressAddress")) result.put("userAddressAddress", result.get("address"));
        return result;
    }
}
