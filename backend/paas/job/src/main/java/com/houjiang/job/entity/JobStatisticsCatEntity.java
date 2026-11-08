package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 按商品类型每月插入表
 * @TableName job_statistics_cat
 * @author Amos
 */
@TableName(value ="job_statistics_cat")
@Data
public class JobStatisticsCatEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 类目id
     */
    private Integer firstCatId;

    /**
     * 类目名称
     */
    private String firstCatName;

    /**
     * 自身交易
     */
    private BigDecimal catTurnoverSelf;

    /**
     * 真实交易
     */
    private BigDecimal catTurnoverReal;

    /**
     * 总交易额
     */
    private BigDecimal catTurnoverAll;

    /**
     * 统计数据 月份
     */
    private String catStatisticsTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}