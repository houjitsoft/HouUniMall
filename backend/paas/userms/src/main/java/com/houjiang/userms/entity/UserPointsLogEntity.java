package com.houjiang.userms.entity;

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
 * 会员积分日志表
 * @TableName user_points_log
 * @author Amos
 */
@TableName(value ="hou_user_points_log")
@Data
public class UserPointsLogEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer pointsLogId;

    /**
     * 类型 1获取积分2积分消费
     */
    private Integer pointsLogType;

    /**
     * 积分类型1.会员注册,2.会员登录3.评价4.购买商品5.6.管理员操作7.积分换购商品8.积分兑换代金券9退货
     */
    private Integer classId;

    /**
     * 积分变更
     */
    private Integer pointsLogPoints;

    /**
     * 冻结积分
     */
    private Integer freezePoints;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pointsLogTime;

    /**
     * 操作 会员注册、会员登录、会员购买、换购商品，平台操作
     */
    private String pointsLogDesc;

    /**
     * 描述
     */
    private String pointsLogOperate;

    /**
     * 标记
     */
    private String pointsLogFlag;

    /**
     * 订单号-确认收货
     */
    private String orderId;


    /**
     * 时间戳
     */
    private Date ts;

    /**
     * 会员编号
     */
    private String userId;

    /**
     * 积分变更
     */
    @TableField(exist = false)
    private String integralChange;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", pointsLogId=").append(pointsLogId);
        sb.append(", pointsLogType=").append(pointsLogType);
        sb.append(", classId=").append(classId);
        sb.append(", pointsLogPoints=").append(pointsLogPoints);
        sb.append(", freezePoints=").append(freezePoints);
        sb.append(", pointsLogTime=").append(pointsLogTime);
        sb.append(", pointsLogDesc=").append(pointsLogDesc);
        sb.append(", pointsLogOperate=").append(pointsLogOperate);
        sb.append(", pointsLogFlag=").append(pointsLogFlag);
        sb.append(", orderId=").append(orderId);
        sb.append(", ts=").append(ts);
        sb.append(", userId=").append(userId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}