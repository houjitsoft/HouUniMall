package com.houjiang.userms.controller;


import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName UserPlusOrderController
 * @Description
 * @Author Amos
 * @Date 2023-07-04 09:36:31
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/plusInfoOrder")
public class UserPlusOrderController extends BaseController {
    /**
     * 查询会员plus充值记录
     * @param pageUserTokenBeanDTO
     * @return
     */
    @PostMapping("/get")
    public ResponseResult get(@RequestBody PageUserTokenBeanDTO pageUserTokenBeanDTO) {
        return userPlusOrderService.get(pageUserTokenBeanDTO);
    }
    /**
     *会员充值列表导出
/**
     * @param map
     * @return
     */
    @PostMapping("/exp")
    public ResponseResult exp(@RequestBody Map<String, Object> map) {
        ResponseResult result=userPlusOrderService.exp(map);
        return result;
    }

    /**
     * 会员充值列表查询
     * @param map
     * @return
     */
    @PostMapping("/list")
    public ResponseResult list(@RequestBody Map<String, Object> map) {
        ResponseResult result = userPlusOrderService.list(map);
        return result;
    }
    /**
     * 支付宝 微信 支付plus会员时创建 预支付订单  fegin
     */
    @PostMapping("/aLiWxCreatePlusOrder")
    public ResponseResult aLiWxCreatePlusOrder(@RequestBody Map<String, Object> map){
        ResponseResult result = userPlusOrderService.aLiWxCreatePlusOrder(map);
        return result;
    }

    /**
     * 查询plus 会员订单是否支付成功
     * @param userId
     * @param plusOrder
     * @return
     */
    @GetMapping("/feign/queryplusorder")
    public Integer queryplusorder(@RequestParam("userId")String userId, @RequestParam("plusOrder")String plusOrder){
        Integer result = userPlusOrderService.queryplusorder(userId,plusOrder);
        return result;
    }

    /**
     * 根据用户ID查询用户的Plus会员状态。
     *
     * @param userId 用户的唯一标识，用于查询用户的Plus会员状态。
     * @return 返回用户的Plus会员状态。具体状态码的含义需要参考相关文档或枚举定义。
     */
    @GetMapping("/feign/queryStatusByPlus")
    public Integer queryStatusByPlus(@RequestParam("userId") String userId){
        Integer result = userPlusOrderService.queryStatusByPlus(userId);
        return result;
    }
}
