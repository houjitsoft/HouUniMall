package com.houjiang.mall.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存盘点单实体类
 * 表名: hou_mall_stock_inventory
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_stock_inventory")
public class StockInventory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 盘点单号
     */
    private String inventoryNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 盘点类型 1-全盘 2-抽盘 3-动态盘点
     */
    private Integer inventoryType;

    /**
     * 盘点范围 1-全部商品 2-指定分类 3-指定商品
     */
    private Integer inventoryScope;

    /**
     * 盘点商品数量
     */
    private Integer totalQuantity;

    /**
     * 盘盈数量
     */
    private Integer profitQuantity;

    /**
     * 盘亏数量
     */
    private Integer lossQuantity;

    /**
     * 状态 0-待盘点 1-盘点中 2-待审核 3-已完成 4-已取消
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
