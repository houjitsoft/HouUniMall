package com.houjiang.activity.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
 /**
 * @ClassName
 * @Description  红包表
 * @Author Amos
 * @Date 2023-05-23 16:42:51
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_addon_activity_redpacket_base")
@Data
public class ActivityRedpacketBaseEntity  implements Serializable {
    /**
     * 红包编号
     */
    @TableId(type = IdType.AUTO)
    private Integer redpacketId;
    /**
     * 红包模版编号
     */
    private Integer redpacketTId;
    /**
     * 红包标题
     */
    private String redpacketTitle;
    /**
     * 红包有效期开始时间
     */
    private Date redpacketStartDate;
    /**
     * 红包有效期结束时间
     */
    private Date redpacketEndDate;
    /**
     * 红包面额
     */
    private Integer redpacketPrice;
    /**
     * 红包使用时的订单限额
     */
    private Double redpacketTOrderlimit;
    /**
     * 红包状态(1-未用,2-已用,3-过期)
     */
    private Integer redpacketState;
    /**
     * 红包发放日期
     */
    private Date redpacketActiveDate;
    /**
     * 红包所有者id
     */
    private String redpacketOwnerId;
    /**
     * 红包所有者名称
     */
    private String redpacketOwnerName;
    /**
     * 使用该红包的订单编号
     */
    private String redpacketOrderId;
    /**
     * 红包来源：1-用户自己领取，2-其它用户转增
     */
    private Integer redpacketFrom;
    /**
     * 红包类型，1-新人注册红包，2-普通红包，3-多品红包 4-店铺红包 5-单品新人红包 6-专属红包 默认2  7 plus红包
     */
    private Integer redpacketType;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

