package com.houjiang.commons.vo.shop;


import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户店铺
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class ShopMemberVO extends UserTokenBeanDTO {

    /**
     * 等级名称
     */
    private String gradeName;

    /**
     * 等级id
     */
    private Integer gradeId;

    /**
     * 等级规则
     */
    private String gradeRule;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 店铺消费金额
     */
    private Double totalConsumption;

}
