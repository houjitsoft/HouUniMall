package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 大转盘活动礼品
 * @TableName activity_spinwin_gift
 * @author Amos
 */
@TableName(value ="activity_spinwin_gift")
@Data
public class ActivitySpinwinGiftEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer giftId;

    /**
     * 礼品名称
     */
    private String giftName;

    /**
     * 红包模版编号
     */
    private Integer redpacketTId;

    /**
     * 礼品是否是红包1-是,2-否
     */
    private Integer giftIsRedpacket;

    /**
     * 礼品是否可用1-可用,2-不可用
     */
    private Integer giftIsEnable;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 礼品图片
     */
    private String giftImage;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}