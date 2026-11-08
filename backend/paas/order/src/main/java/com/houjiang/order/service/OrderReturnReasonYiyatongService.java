package com.houjiang.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.entity.OrderReturnReasonYiyatongEntity;

/**
 * @author Amos
 */
public interface OrderReturnReasonYiyatongService extends IService<OrderReturnReasonYiyatongEntity> {
    /**
     * 获取怡亚通退款原因
     *
     * @return
     */
    ResponseResult listCancelReasonByYiyatong(Integer orderReturnReasonType);
}
