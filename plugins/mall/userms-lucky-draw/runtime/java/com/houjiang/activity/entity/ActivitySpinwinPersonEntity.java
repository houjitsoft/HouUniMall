package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 幸运大转盘抽奖记录
 * @TableName activity_spinwin_person
 * @author Amos
 */
@TableName(value ="activity_spinwin_person")
@Data
public class ActivitySpinwinPersonEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 奖项id
     */
    private Integer prizeId;

    /**
     * 活动id
     */
    private Integer activityId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 红包id
     */
    private Integer redpacketId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}