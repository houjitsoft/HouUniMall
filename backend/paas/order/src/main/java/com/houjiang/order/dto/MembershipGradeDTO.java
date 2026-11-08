package com.houjiang.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName MembershipGradeDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/27 13:48
 * @Version 1.0
 * @author Amos
 */
@Data
public class MembershipGradeDTO {

    /**
     * 用户ID
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
     * 折扣等级
     */
    private BigDecimal gradeDiscount;

    /**
     * 令牌
     */
    private String token;

    /**
     * 等级开始时间
     */
    private Integer gradeStart;
}
