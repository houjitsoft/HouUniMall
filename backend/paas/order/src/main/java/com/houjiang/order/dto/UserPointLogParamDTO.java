package com.houjiang.order.dto;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName UserPointLogParamDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/26 9:44
 * @Version 1.0
 * @author Amos
 */
@Data
public class UserPointLogParamDTO {

    /**
     * 点日志id
     */
    private Integer pointsLogId;

    /**
     * 类型 1获取积分2积分消费
     */
    private Integer pointsLogType;

    /**
     * 分类id
     */
    private Integer classId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 管理员名称
     */
    private String adminName;

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
     * 操作类型
     */
    private Integer operationType;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * token
     */
    private String token;
}
