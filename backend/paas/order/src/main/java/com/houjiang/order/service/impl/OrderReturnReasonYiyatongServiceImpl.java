package com.houjiang.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.order.entity.OrderReturnReasonYiyatongEntity;
import com.houjiang.order.mapper.OrderReturnReasonYiyatongMapper;
import com.houjiang.order.service.OrderReturnReasonYiyatongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Amos
 */
@Service
public class OrderReturnReasonYiyatongServiceImpl extends ServiceImpl<OrderReturnReasonYiyatongMapper, OrderReturnReasonYiyatongEntity>
    implements OrderReturnReasonYiyatongService {

    @Autowired
    private OrderReturnReasonYiyatongMapper orderReturnReasonYiyatongMapper;


    /**
     * 根据售后原因类型查询可选的售后原因列表。
     *
     * @param orderReturnReasonType 售后原因类型编号，用于筛选特定类型的售后原因。
     * @return 返回包含符合条件的售后原因列表的响应结果对象。
     */
    @Override
    public ResponseResult listCancelReasonByYiyatong(Integer orderReturnReasonType) {
        // 根据售后原因类型查询对应的售后原因列表
        // 售后原因
        List<OrderReturnReasonYiyatongEntity> returnReason = orderReturnReasonYiyatongMapper.selectList(new LambdaQueryWrapper<OrderReturnReasonYiyatongEntity>()
                .eq(OrderReturnReasonYiyatongEntity::getOrderReturnReasonType, orderReturnReasonType));
        // 构建并返回成功响应结果，包含查询到的售后原因列表
        return ResponseResult.success(returnReason);
    }
}




