package com.houjiang.goods.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单可售库存预占明细（唯一写方：goods）。
 * 表名保留 hou_mall_stock_reservation；uk(order_no,warehouse_id,sku_id) 支撑幂等。
 */
@Data
@TableName("hou_mall_stock_reservation")
public class SaleableStockReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long warehouseId;
    private Long skuId;
    private Integer quantity;
    /** PENDING / RESERVED / COMMITTED / RELEASED / RETURNED */
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
