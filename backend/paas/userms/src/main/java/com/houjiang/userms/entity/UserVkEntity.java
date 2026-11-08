package com.houjiang.userms.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 店铺微客会员表(UserVk)表实体类
 *
 * @author Amos
 * @since 2024-07-10 11:34:23
 */
@Data
@TableName("hou_user_vk")
public class UserVkEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 推广人数（下级微客数）
     */
    private Integer tgNum;

    /**
     * 分享人数
     */
    private Integer shareNum;

    /**
     * 来源  1平台自己添加  2.通过成为微客条件
     */
    private Integer source;

    /**
     * 推广佣金总额
     */
    private String totalTgCommission;

    /**
     * 分享佣金总额
     */
    private String totalShareCommission;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 已提现佣金总额
     */
    private String withdrawnCommission;

    /**
     * 待提现佣金总额
     */
    private String pendingWithdrawnCommission;

}
