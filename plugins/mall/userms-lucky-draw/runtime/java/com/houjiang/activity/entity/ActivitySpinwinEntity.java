package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 幸运大转盘活动
 * @TableName activity_spinwin
 * @author Amos
 */
@TableName(value ="activity_spinwin")
@Data
public class ActivitySpinwinEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动开始时间
     */
    private Date activityStartTime;

    /**
     * 活动结束时间
     */
    private Date activityEndTime;

    /**
     * 参与人数是否显示：1-显示，2-隐藏
     */
    private Integer partakeNumIsDisplay;

    /**
     * 参与人数
     */
    private Integer partakeNum;

    /**
     * 参与人数限制是否限制：1-不限，2-限制
     */
    private Integer partakeNumIsLimit;

    /**
     * 参与人数限制
     */
    private Integer partakeNumLimit;

    /**
     * 图文说明
     */
    private String graphicExplanation;

    /**
     * 首屏广告
     */
    private String firstScreenAdvertise;

    /**
     * 首屏广告是否展示1-显示2-不显示
     */
    private Integer firstScreenIsDisplay;

    /**
     * 是否轮播获奖信息：1-开启，2关闭
     */
    private Integer isRoundAwardInformation;

    /**
     * 活动是否可用1-可用2不可用3-删除
     */
    private Integer activityIsEnable;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 总抽奖机会:是否限制：1-不限，2-限制
     */
    private Integer totalSweepstakesIsLimit;

    /**
     * 总抽奖机会
     */
    private Integer totalSweepstakesLimit;

    /**
     * 每人每日抽奖次数
     */
    private Integer winningTimesPerPerson;

    /**
     * 
     */
    private BigDecimal winningRate;

    /**
     * 预计参与人数
     */
    private Integer expectedNumberOfParticipants;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}