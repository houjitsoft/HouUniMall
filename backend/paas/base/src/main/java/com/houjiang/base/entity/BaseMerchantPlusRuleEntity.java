package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * plus会员规则表(BaseMerchantPlusRule)
 *
 * @since 2023-05-15 15:00:26
 * @author Amos
 */
@TableName("hou_base_merchant_plus_rule")
@Data
public class BaseMerchantPlusRuleEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer plusRuleid;
    /**
     * plus会员宣传语
     */
    private String plusPropaganda;
    /**
     * 推荐标识语
     */
    private String plusSigns;
    /**
     * 月卡logo
     */
    private String plusMonthcardlogo;
    /**
     * 季卡logo
     */
    private String plusSeasoncardlogo;
    /**
     * 年卡logo
     */
    private String plusYearcardlogo;
    /**
     * 开通界面背景图
     */
    private String plusBackdrop;
    /**
     * plus会员用户协议
     */
    private String plusMemberagreement;
    /**
     * 商家须知
     */
    private String plusMerchantnotice;
    /**
     * plus会员状态  0 关闭  1 开启
     */
    private Integer plusStatus;
    /**
     * 更新时间
     */
    private Date plusUpdatetime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

