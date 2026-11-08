package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 大转盘活动奖项
 * @TableName activity_spinwin_prize
 * @author Amos
 */
@TableName(value ="activity_spinwin_prize")
@Data
public class ActivitySpinwinPrizeEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer prizeId;

    /**
     * 奖项等级
     */
    private String prizeGrade;

    /**
     * 奖项名称
     */
    private String prizeName;

    /**
     * 礼品id
     */
    private Integer giftId;

    /**
     * 活动id
     */
    private Integer activityId;

    /**
     * 礼品数量
     */
    private Integer giftNum;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 给出的礼品数量
     */
    private Integer giveOutGiftNum;

    /**
     * 是否中奖 1中奖2不中奖
     */
    private String isWin;

    /**
     * 奖项排序
     */
    private Integer displayOrder;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}