package com.houjiang.mall.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Amos
 */
@Data
@TableName("hou_mall_stock_reservation")
public class StockReservation {
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
