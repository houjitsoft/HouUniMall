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
 * 出库单实体类
 * 表名: hou_mall_stock_outbound
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_stock_outbound")
public class StockOutbound implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 出库单号
     */
    private String outboundNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 出库类型 1-销售出库 2-调拨出库 3-盘亏出库 4-报损出库 5-其他出库
     */
    private Integer outboundType;

    /**
     * 关联单号（订单号/调拨单）
     */
    private String relatedNo;

    /**
     * 出库商品数量
     */
    private Integer totalQuantity;

    /**
     * 出库商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态 0-待出库 1-部分出库 2-已完成 3-已取消
     */
    private Integer status;

    /**
     * 预计出库时间
     */
    private LocalDateTime expectedTime;

    /**
     * 实际出库时间
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
