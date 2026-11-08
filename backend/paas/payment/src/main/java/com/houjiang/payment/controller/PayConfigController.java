package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.payment.service.PayConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付配置信息表(PayConfig)表控制层
 * @author Amos
 */
@RestController
@RequestMapping("pay/payConfig")
public class PayConfigController {

    @Autowired
    private PayConfigService payConfigService;



    /**
     * 查询支付配置信息 feign 后端查询
     *
     * @param map
     * @return
     */
    @PostMapping("/queryFeign")
    public ResponseResult queryFeign(@RequestBody Map<String, Object> map) {
        ResponseResult result = payConfigService.queryFeign(map);
        return result;
    }

    /**
     * 平台修改支付宝配置
     *
     * @param map
     * @return
     */
    @PostMapping("/update/queryZFBPayConfig")
    public ResponseResult updateZFBPayConfig(@RequestBody Map<String, Object> map) {
        ResponseResult result=payConfigService.updateZFBPayConfig(map);
        return result;
    }


    /**
     * 平台查询支付宝配置
     *
     * @param map
     * @return
     */
    @PostMapping("/queryZFBPayConfig")
    public ResponseResult queryZFBPayConfig(@RequestBody Map<String, Object> map) {
        ResponseResult result=payConfigService.queryZFBPayConfig(map);
        return result;
    }


    /**
     * 平台修改微信配置
     *
     * @param map
     * @return
     */
    @PostMapping("/update/queryWXPayConfig")
    public ResponseResult updateWXPayConfig(@RequestBody Map<String, Object> map) {
        ResponseResult result=payConfigService.updateWXPayConfig(map);
        return result;
    }

    /**
     * 平台查询微信配置
     *
     * @param map
     * @return
     */
    @PostMapping("/queryWXPayConfig")
    public ResponseResult queryWXPayConfig(@RequestBody Map<String, Object> map) {
        ResponseResult result=payConfigService.queryWXPayConfig(map);
        return result;
    }

    /**
     * 用户提现金额配置
     * @return
     */
    @GetMapping("/h5/queryWithdrawConfig")
    public ResponseResult queryWithdrawConfig(){
        ResponseResult result = payConfigService.queryWithdrawConfig();
        return result;
    }

    /**
     * 删除微客信息
     * @param userId
     * @return
     */
    @PostMapping("/h5/delFxInfo")
    public ResponseResult delFxInfo(@RequestParam String userId) {
        return payConfigService.delFxInfo(userId);
    }
    /**
     * 查询支付配置信息 feign 后端查询
     *
     * @param map
     * @return
     */
    @PostMapping("/queryPayConfigFeign")
    public ResponseResult queryPayConfigFeign(@RequestBody Map<String, Object> map) {
        return payConfigService.queryPayConfigFeign(map);
    }
}

