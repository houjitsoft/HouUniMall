package com.houjiang.base.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class BaseCurrencySymbolVO {

    /**
     * 货币符号主键
     */
    private Integer id;

    /**
     * 货币符号
     */
    private String currencySymbol;

    /**
     * 备注
     */
    private String remark;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;
    /**
     * 货币代码
     */
    private String currencyCode;
    /**
     * 是否使用
     */
    private Integer status;
}
