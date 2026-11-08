package com.houjiang.order.controller;

import com.houjiang.commons.annnotation.RepeatClick;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.OrderReturnPlatformListDTO;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @ClassName OrderReturnPlatformController
 * @Description 平台介入
 * @Author Amos
 * @Date 2023/6/30 10:44
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/order/return/platform")
public class OrderReturnPlatformController extends BaseController{



    /**
     * @Description: 买家新增/修改平台介入
     * @Param: [map]
     * @Return: ResponseResult
     */
    @PostMapping("/add/intervention")
    public ResponseResult insertPlatformIntervention(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnPlatformService.insertPlatformIntervention(map);
        return result;
    }


    /**
     * @Description: 检查是否有平台介入
     * @Param:  [map]
     * @Return: ResponseResult
     */
    @PostMapping("/check")
    public ResponseResult checkplatform(@RequestBody Map<String, Object> map){
        ResponseResult result = orderReturnPlatformService.checkplatform(map);
        return result;
    }

    /**
     * 查看交易维权详情
     * @param map
     * @return
     */
    @PostMapping("/query/intervention")
    public ResponseResult queryPlatformIntervention(@RequestBody Map<String, Object> map) {
        ResponseResult result = orderReturnPlatformService.queryPlatformIntervention(map);
        return result;
    }

    /**
     * 平台审核交易维权 退款
     * @param map
     * @return
     */
    @RepeatClick
    @GlobalTransactional
    @PostMapping("/approve")
    public ResponseResult forPlatformApprove(@RequestBody Map<String, String> map) throws TransactionException {
        ResponseResult result = orderReturnPlatformService.forPlatformApprove(map);
        return result;
    }

    /**
     * 平台介入退货审核 退货
     * @param map
     * @return
     */
    @RepeatClick
    @GlobalTransactional
    @PostMapping("/approveAudit")
    public ResponseResult returnforPlatformApprove(@RequestBody Map<String, String> map) throws TransactionException {
        ResponseResult result = orderReturnPlatformService.returnforPlatformApprove(map);
        return result;
    }

    /**
     * 平台查询交易维权列表
     * @param map
     * @return
     */
    @PostMapping("/list")
    public ResponseResult queryReturnPlatform(@RequestBody OrderReturnPlatformListDTO map) {
        ResponseResult result = orderReturnPlatformService.queryReturnPlatform(map);
        return result;
    }

    /**
     * 检查是否有平台介入
     * @param map
     * @return
     */
    @PostMapping("/checkPlatformInfo")
    public ResponseResult checkPlatformInfo(@RequestBody Map<String, Object> map){
        ResponseResult result = orderReturnPlatformService.checkPlatformInfo(map);
        return result;
    }

    /**
     * 平台超时未处理
     * @return
     */
    @PostMapping("/update")
    public Boolean platformUpdate(){
        Boolean bolean = orderReturnPlatformService.platformUpdate();
        return bolean;
    }


    /**
     * 定时任务 申请平台介入卖家超时未处理
     * @return
     */
    @PostMapping("/job/updateTimeOut")
    public Boolean updateTimeOutReturnplatform(){
        Boolean resultResp = orderReturnPlatformService.updateTimeOutReturnplatform();
        return resultResp;
    }

}
