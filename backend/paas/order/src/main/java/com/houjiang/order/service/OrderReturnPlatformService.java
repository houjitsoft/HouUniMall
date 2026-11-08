package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.dto.OrderReturnPlatformListDTO;
import com.houjiang.order.entity.OrderReturnPlatformEntity;
import io.seata.core.exception.TransactionException;

import java.util.Map;

/**
 * @author Amos
 */
public interface OrderReturnPlatformService extends IService<OrderReturnPlatformEntity> {

    /**
     * 买家新增/修改平台介入
     *
     * @param map
     * @return
     */
    ResponseResult insertPlatformIntervention(Map<String, Object> map);

    /**
     * @Description: 检查是否有平台介入
     * @Param: [map]
     * @Return: ResponseResult
     */
    ResponseResult checkplatform(Map<String, Object> map);

    /**
     * 查看交易维权详情
     *
     * @param map
     * @return
     */
    ResponseResult queryPlatformIntervention(Map<String, Object> map);

    /**
     * 平台审核交易维权 退款
     *
     * @param map
     * @return
     */
    ResponseResult forPlatformApprove(Map<String, String> map) throws TransactionException;

    /**
     * 平台介入退货审核
     *
     * @param map
     * @return
     */
    ResponseResult returnforPlatformApprove(Map<String, String> map) throws TransactionException;

    /**
     * 平台查询交易维权列表
     *
     * @param map
     * @return
     */
    ResponseResult queryReturnPlatform(OrderReturnPlatformListDTO map);

    /**
     * 检查是否有平台介入
     * @param map
     * @return
     */
    ResponseResult checkPlatformInfo(Map<String, Object> map);

    /**
     * 平台超时未处理
     * @return
     */
    Boolean platformUpdate();


    /**
     * 定时任务 申请平台介入卖家超时未处理
     *
     * @return
     */
    Boolean updateTimeOutReturnplatform();

}
