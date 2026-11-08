package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
 /**
 * @ClassName
 * @Description  代金券表
 * @Author Amos
 * @Date 2023-05-25 16:55:50
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_voucher_base")
@Data
public class ActivityVoucherBaseEntity  implements Serializable {
    /**
     * 代金券编号
     */
    @TableId(type = IdType.AUTO)
    private Integer voucherId;
    /**
     * 代金券模版编号
     */
    private Integer voucherTId;
    /**
     * 代金券标题
     */
    private String voucherTitle;
    /**
     * 代金券有效期开始时间
     */
    private Date voucherStartDate;
    /**
     * 代金券有效期结束时间
     */
    private Date voucherEndDate;
    /**
     * 代金券面额
     */
    private BigDecimal voucherPrice;
    /**
     * 代金券使用时的订单限额
     */
    private BigDecimal voucherLimit;
    /**
     * 代金券的店铺id
     */
    private Integer voucherShopId;
    /**
     * 代金券状态(1-未用,2-已用,3-过期,4-收回)
     */
    private Integer voucherState;
    /**
     * 代金券发放日期
     */
    private Date voucherActiveDate;
    /**
     * 代金券所有者id
     */
    private String voucherOwnerId;
    /**
     * 代金券所有者名称
     */
    private String voucherOwnerName;
    /**
     * 使用该代金券的订单编号
     */
    private String voucherOrderId;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
