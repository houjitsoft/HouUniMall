package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName PayUserBaseController
 * @Description 用户基础信息
 * @Author Amos
 * @Date 2023/5/19 18:08
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/userBase")
public class PayUserBaseController extends BaseController{


    /**
     * 创建用户
     *
     * @param userId
     * @return
     */
    @GetMapping("/createUser")
    public ResponseResult createUser(@RequestParam("userId") String userId) {
        ResponseResult result = payUserBaseService.createUser(userId);
        return result;
    }
    /**
     * 设置支付密码与找回
     *
     * @param map
     * @return
     */
    @PostMapping("/setPaymentPassword")
    public ResponseResult setPaymentPassword(@RequestBody Map<String, Object> map) {
        ResponseResult result = payUserBaseService.setPaymentPassword(map);
        return result;
    }
    /**
     * 校验支付密码是否存在
     */
    @PostMapping("checkPayPassword")
    public ResponseResult checkPayPassword(@RequestBody Map<String, String> paraMap){
        ResponseResult result = payUserBaseService.checkPayPassword(paraMap);
        return result;
    }
    /**
     * 修改用户支付密码
     */
    @RequestMapping("/updatePaymentPassword")
    public ResponseResult updatePaymentPassword(@RequestBody Map<String, Object> map) {
        ResponseResult result = payUserBaseService.updatePaymentPassword(map);
        return result;
    }
    /**
     *  plus会员 支付 余额支付 购物卡支付
     */
    @PostMapping("plusMemberpay")
    public ResponseResult plusMemberpay(@RequestBody Map<String, Object> map) {
        ResponseResult result = payUserBaseService.plusMemberpay(map);
        return result;
    }



}
