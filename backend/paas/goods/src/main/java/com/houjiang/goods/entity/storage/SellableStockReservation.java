package com.houjiang.goods.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 可售库存预占（原 mall 表 hou_mall_stock_reservation，由 goods 唯一写入）。
 */
@Data
@TableName("hou_mall_stock_reservation")
public class SellableStockReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long warehouseId;
    private Long skuId;
    private Integer quantity;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
