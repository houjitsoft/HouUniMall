package com.houjiang.commons.vo.user;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 会员等级
 * @author Amos
 */
@Data
public class MembershipGradeVO {

    /**
     * id
     */
    private Integer id;

    /**
     * 等级名称
     */
    private String gradeName;

    /**
     * 等级规则
     */
    private Integer gradeRule;

    /**
     * 折扣
     */
    private BigDecimal gradeDiscount;

    /**
     * token
     */
    private String token;

    /**
     * 1正常 2 删除
     */
    private Integer gradeStart;


}
