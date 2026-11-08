package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.entity.OrderCancelReasonEntity;

import java.util.Map;

/**
 * @author Amos
 */
public interface OrderCancelReasonService extends IService<OrderCancelReasonEntity> {

    /**
     * 订单取消原因
     *
     * @param paraMap
     * @return
     */
    ResponseResult list(Map<String, String> paraMap);

    /**
     * 删除退款/退货理由
     *
     * @param paraMap
     */
    ResponseResult delete(Map<String, String> paraMap);

    /**
     * 新增退款/退货理由
     *
     * @param paraMap
     */
    ResponseResult addCancelreason(Map<String, String> paraMap);

    /**
     * 更新退款/退货理由
     *
     * @param paraMap
     */
    ResponseResult updateCancelreason(Map<String, String> paraMap);

}
