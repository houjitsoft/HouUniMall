package com.houjiang.userms.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PlusRuleEntityVO {
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
     * 状态 0 关闭 1 开启
     */
    private Integer plusStatus;

    /**
     * 修改时间
     */
    private Date plusUpdatetime;

    /**
     * token
     */
    private String token;
}
