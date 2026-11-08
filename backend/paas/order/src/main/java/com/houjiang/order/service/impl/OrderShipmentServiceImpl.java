package com.houjiang.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.order.mapper.OrderShipmentMapper;
import com.houjiang.order.entity.OrderShipmentEntity;
import com.houjiang.order.service.OrderShipmentService;
import org.springframework.stereotype.Service;

/**
 * 订单发货表(OrderShipment)表服务实现类
 *
 * @author Amos
 * @since 2024-01-30 15:46:44
 */
@Service
public class OrderShipmentServiceImpl extends ServiceImpl<OrderShipmentMapper, OrderShipmentEntity> 
    implements OrderShipmentService {

}
