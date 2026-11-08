package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 仓库库存流水商品表(StorageInventoryFlowGoods)表实体类
 *
 * @author Amos
 * @since 2024-09-20 16:51:54
 */
@Data
@TableName("storage_inventory_flow_goods")
public class StorageInventoryFlowGoodsEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * 流水商品id
     */
    @TableId(type = IdType.AUTO)
    private Integer inventoryFlowGoodsId;

    /**
     * 流水编号，RK开头为入库单，CK开头为出库单，DB开头为调拨单
     */
    private String inventoryFlowId;
    
    /**
     * 商品id
     */
    private Integer goodsId;
    
    /**
     * 店铺id
     */
    private Integer shopId;
    
    /**
     * 入库数量/出库数量/调拨数量
     */
    private Integer goodsNum;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 采购数量
     */
    private Integer buyNum;

}
