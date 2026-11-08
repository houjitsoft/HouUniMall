package com.houjiang.activity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinationBaseQueryVO {

    /**
     * 组合活动id
     */
    @TableId(type = IdType.AUTO)
    private Integer combinationId;


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
     * 店铺名称
     */
    private String shopName;

}
