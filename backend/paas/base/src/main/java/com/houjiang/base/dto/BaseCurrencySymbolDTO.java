package com.houjiang.base.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class BaseCurrencySymbolDTO extends PageHelper {

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
     * 货币代码
     */
    private String currencyCode;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 是否使用
     */
    private Integer status;
}
