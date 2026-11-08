package com.houjiang.commons.vo.third;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: 用户积分信息
 * @CreateDate: 2025/7/8 23:32
 * @author Amos
 */
@Data
public class UserPointsInfoVO implements Serializable {

    /**
     * 剩余货币数量
     */
    private BigDecimal balance;
    /**
     * 货币类型id
     */
    private Integer currencyId;
    /**
     * 货币类型名称
     */
    private String currencyName;
    /**
     * 客户类型名称
     */
    private String customerTypeName;

}
