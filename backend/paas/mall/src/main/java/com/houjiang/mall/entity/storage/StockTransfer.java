package com.houjiang.mall.entity.storage;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存调拨单实体类
 * 表名: hou_mall_stock_transfer
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_stock_transfer")
public class StockTransfer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 调拨单号
     */
    private String transferNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 调出仓库ID
     */
    private Long fromWarehouseId;

    /**
     * 调入仓库ID
     */
    private Long toWarehouseId;

    /**
     * 调拨类型 1-普通调拨 2-紧急调拨
     */
    private Integer transferType;

    /**
     * 调拨商品数量
     */
    private Integer totalQuantity;

    /**
     * 状态 0-待审核 1-已审核 2-调拨中 3-已完成 4-已取消
     */
    private Integer status;

    /**
     * 预计完成时间
     */
    private LocalDateTime expectedTime;

    /**
     * 实际完成时间
     */
    private LocalDateTime actualTime;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

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
