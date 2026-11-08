package com.houjiang.userms.dto;


import com.houjiang.commons.util.querypage.PageUtils;
import lombok.Data;

/**
 * 积分信息
 * @author Amos
 */
@Data
public class IntegralDetailDTO {

    /**
     * 登录送积分
     */
    private Integer loginIntegral;

    /**
     * 册送积分
     */
    private Integer registerIntegral;

    /**
     * 消费与赠送积分比例
     */
    private Integer ruleConsume;

    /**
     * 会员积分
     */
    private Integer countIntegral;
    /**
     * 冻结积分
     */
    private Integer frozenIntegra;

    /**
     * 交易明细
     */
    private PageUtils pageInfoS;


}
