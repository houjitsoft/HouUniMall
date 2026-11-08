package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 仓库库存流水表(StorageInventoryFlow)表实体类
 *
 * @author Amos
 * @since 2024-09-20 16:51:29
 */
@Data
@TableName("storage_inventory_flow")
public class StorageInventoryFlowEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * 流水编号，RK开头为入库单，CK开头为出库单，DB开头为调拨单
     */
    @TableId
    private String inventoryFlowId;
    
    /**
     * 店铺id
     */
    private Integer shopId;
    
    /**
     * 入库仓库id
     */
    private Integer putStorageId;
    
    /**
     * 出库仓库id
     */
    private Integer outStorageId;
    
    /**
     * 入库类型 1为入库单，2为出库单，3为调拨单
     */
    private Integer type;
    
    /**
     * 入库类型详情 1采购入库 2供应商采购入库 3销售退库入库 4调拨入库 5销售出库 6调拨出库 7库存预警 8满仓调拨
     */
    private Integer typeDetails;
    
    /**
     * 入库时间/调拨申请时间
     */
    private Date startTime;
    
    /**
     * 出库时间/期望到货日期
     */
    private Date endTime;

    /**
     * 操作商品数量
     */
    private Integer inventoryFlowNum;

    /**
     * 操作人id
     */
    private String operatorId;
    
    /**
     * 操作人名称
     */
    private String operatorName;
    
    /**
     * 操作时间
     */
    private Date operatorTime;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 备注
     */
    private String notes;

}
