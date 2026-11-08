package com.houjiang.order.controller;

import com.houjiang.order.service.OrderShipmentService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 订单发货表(OrderShipment)表控制层
 *
 * @author Amos
 * @since 2024-01-30 15:46:43
 */
@RestController
@RequestMapping("orderShipment")
public class OrderShipmentController {
    /**
     * 服务对象
     */
    @Resource
    private OrderShipmentService orderShipmentService;

    
}

