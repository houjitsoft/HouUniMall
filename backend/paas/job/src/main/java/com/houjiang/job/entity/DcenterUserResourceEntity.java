package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 数据大屏用户资源表
 * @TableName ${database.prefix.datav}hou_datav_user_resource
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_user_resource")
@Data
public class DcenterUserResourceEntity implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;

    /**
     * 用户资金-预付款充值、转账、结算后的款项等等
     */
    private BigDecimal userMoney;

    /**
     * 充值卡余额-只能从来购物
     */
    private BigDecimal userRechargeCard;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}