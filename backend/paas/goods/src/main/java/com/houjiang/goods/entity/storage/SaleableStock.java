package com.houjiang.goods.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 可售库存事实表（唯一写方：goods）。
 * 表名保留 hou_mall_stock，与订单预占状态机共用。
 */
@Data
@TableName("hou_mall_stock")
public class SaleableStock implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long warehouseId;
    private Long goodsId;
    private Long skuId;
    private String goodsName;
    private String skuSpec;
    private Integer quantity;
    private Integer availableQty;
    private Integer lockedQty;
    private Integer warningQty;
    private BigDecimal costPrice;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
