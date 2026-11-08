package com.houjiang.userms.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户邀请人表(UserInviter)表实体类
 *
 * @author Amos
 * @since 2024-07-10 11:36:19
 */
@Data
@TableName(value ="hou_user_inviter")
public class UserInviterEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 一级邀请人
     */
    private String oneInviter;

    /**
     * 二级邀请人
     */
    private String twoInviter;

    /**
     * 是否有上级
     */
    private Integer isSuperior;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 一级邀请人是否是微客
     */
    @TableField(exist = false)
    private Boolean oneVkFlag;

    /**
     * 一级邀请人用户名
     */
    @TableField(exist = false)
    private String oneUserName;

    /**
     * 一级邀请人手机号
     */
    @TableField(exist = false)
    private String oneUserMobile;

    /**
     * 一级邀请人头像
     */
    @TableField(exist = false)
    private String oneUserAvatar;

    /**
     * 二级邀请人是否是微客
     */
    @TableField(exist = false)
    private Boolean twoVkFlag;

    /**
     * 二级邀请人用户名
     */
    @TableField(exist = false)
    private String twoUserName;

    /**
     * 二级邀请人手机号
     */
    @TableField(exist = false)
    private String twoUserMobile;

    /**
     * 用户是否是微客
     */
    @TableField(exist = false)
    private Boolean userVkFlag;

    /**
     * 用户名
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 用户手机号
     */
    @TableField(exist = false)
    private String userMobile;

}
