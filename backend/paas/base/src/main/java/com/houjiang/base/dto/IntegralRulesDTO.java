package com.houjiang.base.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * 积分规则
 * @author Amos
 */
@Data
public class IntegralRulesDTO extends UserTokenBeanDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 登录赠送积分
     */
    private Integer ruleLogin;

    /**
     * 注册赠送积分
     */
    private Integer ruleRegister;

    /**
     * 消费与赠送积分比例
     */
    private Integer ruleConsume;


}
