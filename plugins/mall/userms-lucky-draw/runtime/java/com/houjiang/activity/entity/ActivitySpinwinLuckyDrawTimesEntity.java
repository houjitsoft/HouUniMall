package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 大转盘用户抽奖次数
 * @TableName activity_spinwin_lucky_draw_times
 * @author Amos
 */
@TableName(value ="activity_spinwin_lucky_draw_times")
@Data
public class ActivitySpinwinLuckyDrawTimesEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer timeId;

    /**
     * 活动id
     */
    private Integer activityId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 总抽奖次数
     */
    private Integer luckyDrawTimes;

    /**
     * 剩余抽奖次数
     */
    private Integer surplusDrawTimes;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 中奖次数
     */
    private Integer winningTimes;

    /**
     * 每日抽奖次数
     */
    private Integer numberOfDailyLottery;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 今日抽奖次数
     */
    private Integer numSweepstakesToday;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}