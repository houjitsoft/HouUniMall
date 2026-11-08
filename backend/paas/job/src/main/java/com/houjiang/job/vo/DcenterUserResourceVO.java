package com.houjiang.job.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 数据大屏用户资源表
 * @TableName ${database.prefix.datav}hou_datav_user_resource
 * @author Amos
 */

@Data
public class DcenterUserResourceVO  {
    /**
     * 用户id
     */

    private String userId;

    /**
     * 用户资金-预付款充值、转账、结算后的款项等等
     */
    private BigDecimal userMoney;

    /**
     * 充值卡余额-只能从来购物
     */
    private BigDecimal userRechargeCard;


}