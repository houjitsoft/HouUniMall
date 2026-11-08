package com.houjiang.commons.vo.order;


import com.houjiang.commons.util.excel.ExcelCell;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class StatisticsMonthVO implements Serializable {

    public static final long serialVersionUID = 1L;

    @ExcelCell(index = 1)
    /** 自身交易 */
    private BigDecimal turnoverSelf;
    @ExcelCell(index = 2)
    /** 真是·交易 */
    private BigDecimal turnoverReal;
    @ExcelCell(index = 3)
    private String statisticsTime;
    @ExcelCell(index = 4)
    /** 真是交易占比 */
    private BigDecimal proportion;



}
