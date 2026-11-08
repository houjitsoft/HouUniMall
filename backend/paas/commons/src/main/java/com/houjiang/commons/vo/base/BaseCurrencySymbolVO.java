package com.houjiang.commons.vo.base;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * (BaseCurrencySymbol)表实体类
 *
 * @author Amos
 * @since 2024-05-24 17:39:20
 */
@Data
public class BaseCurrencySymbolVO  {
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
