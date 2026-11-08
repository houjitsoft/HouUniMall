package com.houjiang.userms.controller;

import com.houjiang.commons.annnotation.RepeatClick;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName UserPointsLogController
 * @Description 用户积分
 * @Author Amos
 * @Date 2023/5/19 9:01
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("user/pointsLog")
public class UserPointsLogController extends BaseController{

    /**
     * 判断当前用户当天是否已经登录过
     *
     * @param map
     * @return
     */
    @PostMapping("/infoByMap")
    public ResponseResult queryLoginExit(@RequestBody Map<String, String> map) {
        ResponseResult result = userPointsLogService.queryLoginExit(map);
        return result;
    }


    /**
     * 添加积分修改记录
     *
     * @param map
     * @return
     */
    @RepeatClick
    @PostMapping("/insertByMap")
    public ResponseResult insertByMap(@RequestBody Map<String, Object> map) {
        ResponseResult result = userPointsLogService.insertByMap(map);
        return result;
    }

    /**
     * 积分明细列表
     *
     * @param params
     * @return
     */
    @PostMapping("/integral/detail")
    public ResponseResult integralDetail(@RequestBody Map<String, Object> params) {
        ResponseResult result = userPointsLogService.queryIntegralDetailList(params);
        return result;
    }



    /**
     * 平台查询 积分明细列表
     *
     * @param params
     * @return
     */
    @PostMapping("/query")
    public ResponseResult query(@RequestBody Map<String, Object> params) {
        ResponseResult result = userPointsLogService.query(params);
        return result;
    }

    /**
     * 查询订单获取的积分
     *
     * @param orderId
     * @return
     */
    @PostMapping("/queryPointsByOrderId")
    public Integer queryPointsByOrderId(@RequestParam("orderId") String orderId) {
        Integer result = userPointsLogService.queryPointsByOrderId(orderId);
        return result;
    }

}



