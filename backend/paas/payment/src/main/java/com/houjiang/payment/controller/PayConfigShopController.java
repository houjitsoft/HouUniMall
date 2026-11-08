package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName PayConfigShopController
 * @Description
 * @Author Amos
 * @Date 2023/8/4 11:00
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/config/shop")
public class PayConfigShopController extends BaseController{


    /**
     * 查询支付配置
     * @param map
     * @return
     */
    @PostMapping("/select")
    public ResponseResult select(@RequestBody Map<String,Object> map) {
        ResponseResult result=payConfigShopService.select(map);
        return result;
    }


    /**
     * 支付配置
     * @param map
     * @return
     */
    @PostMapping("/update")
    public ResponseResult update(@RequestBody Map<String,Object> map) {
        ResponseResult result=payConfigShopService.update(map);
        return result;
    }
    /**
     * 查询支付配置 feign
     * @param map
     * @return
     */
    @PostMapping("/selectFeign")
    public ResponseResult selectPayConfigFeign(@RequestBody Map<String,Object> map) {
        ResponseResult result=payConfigShopService.selectPayConfigFeign(map);
        return result;
    }
}
