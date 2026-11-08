package com.houjiang.mall.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单实体类
 * 表名: hou_mall_stock_inbound
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_stock_inbound")
public class StockInbound implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 入库单号
     */
    private String inboundNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 入库类型 1-采购入库 2-退货入库 3-调拨入库 4-盘盈入库 5-其他入库
     */
    private Integer inboundType;

    /**
     * 关联单号（采购单/退货单/调拨单）
     */
    private String relatedNo;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 入库商品数量
     */
    private Integer totalQuantity;

    /**
     * 入库商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态 0-待入库 1-部分入库 2-已完成 3-已取消
     */
    private Integer status;

    /**
     * 预计入库时间
     */
    private LocalDateTime expectedTime;

    /**
     * 实际入库时间
     */
    private LocalDateTime actualTime;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

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
