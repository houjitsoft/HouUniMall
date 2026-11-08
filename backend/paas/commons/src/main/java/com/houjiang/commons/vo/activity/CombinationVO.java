package com.houjiang.commons.vo.activity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * (ActivityCombinationBase)表实体类
 *
 * @author Amos
 * @since 2024-04-16 11:26:52
 */
@Data
public class CombinationVO implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
    * 组合活动id
    */
    private Integer combinationId;

    /**
     * 组合活动id
     */
    private Integer shopId;
        
    /**
    * 组合活动名称
    */
    private String combinationName;
        
    /**
    * 组合活动类型 1固定组合 2搭配组合 
    */
    private Integer combinationType;
        
    /**
    * 开始时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
        
    /**
    * 结束时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
        
    /**
    * 组合活动状态  0-未开始;1-已开始;2-已结束；3-已失效
    */
    private Integer combinationStatus;
        
    /**
    * 组合活动价格
    */
    private BigDecimal combinationPrice;
        
    /**
    * 组合活动主图
    */
    private String combinationImage;
        
    /**
    * 运费模板id
    */
    private Integer transportTemplateId;

    /**
     * 售卖区域id
     */
    private Integer transportAreaId;
        
    /**
    * 创建时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 组合活动原价
     */
    private BigDecimal combinationOriginalPrice;

}
