package com.houjiang.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.houjiang.order.entity.OrderCancelReasonEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * @Entity com.houjiang.order.entity.OrderCancelReason
 * @author Amos
 */
@Mapper
public interface OrderCancelReasonMapper extends BaseMapper<OrderCancelReasonEntity> {
    /**
     * 添加售后原因
     */
    Integer addCancelreason(Map<String, String> paraMap);
    /**
     * 添加售后原因
     */
    Integer updateCancelreason(Map<String, String> paraMap);
}




