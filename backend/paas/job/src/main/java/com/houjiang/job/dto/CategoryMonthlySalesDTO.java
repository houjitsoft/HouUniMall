package com.houjiang.job.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class CategoryMonthlySalesDTO extends PageHelper {

    /**
     * 类目ID
     */
    private Integer firstCatId;

    /**
     * 类目名称
     */
    private String firstCatName;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 分类交易合计
     */
    private BigDecimal catTurnoverAll;

    /**
     * 分类自身交易
     */
    private BigDecimal catTurnoverSelf;

    /**
     * 分类真实交易
     */
    private BigDecimal catTurnoverReal;

    /**
     * 统计数据月份
     */
    private String catStatisticsTime;


}
