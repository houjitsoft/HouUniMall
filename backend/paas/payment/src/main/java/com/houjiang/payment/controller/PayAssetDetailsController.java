package com.houjiang.payment.controller;

import com.houjiang.commons.util.ResponseResult;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName PayAssetDetailsController
 * @Description
 * @Author Amos
 * @Date 2023/6/26 15:07
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("pay/assetDetails")
public class PayAssetDetailsController extends BaseController{

    /**
     * 我的余额-明细
     *
     * @param map
     * @return
     */
    @PostMapping("/queryList")
    public ResponseResult queryAssetList(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAssetDetailsService.queryAssetList(map);
        return result;
    }

    /**
     * 我的余额-查看详情
     *
     * @param map
     * @return
     */
    @PostMapping("/queryDetail")
    public ResponseResult queryAssetDetail(@RequestBody Map<Object, Object> map) {
        ResponseResult result = payAssetDetailsService.queryAssetDetail(map);
        return result;
    }


    /**
     * 平台-明细
     *
     * @return
     */
    @RequestMapping("/platform/queryList")
    public ResponseResult queryPlatformAssetList(@RequestBody Map<String, Object> map) {
        ResponseResult result = payAssetDetailsService.queryPlatformAssetList(map);
        return result;
    }

    /**
     * 平台-明细
     *
     * @return
     */
    @RequestMapping("/platform/queryList/export")
    public ResponseResult exportPlatformAssetList(@RequestBody Map<Object, Object> map) {
        ResponseResult result = payAssetDetailsService.exportPlatformAssetList(map);
        return result;
    }

    /**
     * 插入明细
     * @param map
     */
    @PostMapping("/feign/insertDetail")
    public void insertDetail(@RequestBody Map<Object, Object> map){
        payAssetDetailsService.insertDetail(map);
    }
}
