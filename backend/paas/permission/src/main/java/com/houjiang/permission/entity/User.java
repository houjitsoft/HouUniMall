package com.houjiang.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Cloud 平台会员（hou_user_info）
 * @author Amos
 */
@Data
@TableName("hou_user_info")
public class User {

    @TableId(value = "user_id", type = IdType.ASSIGN_ID)
    private Long userId;

    @TableField("user_name")
    private String username;

    @TableField("user_nickname")
    private String realName;

    @TableField("user_mobile")
    private String mobile;

    private String password;

    @TableField("user_state")
    private Integer status;

    @TableField("user_lastlogin_time")
    private LocalDateTime lastLoginTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 注册终端：mall / houcloud / hougeo
     */
    @TableField("register_terminal")
    private String registerTerminal;

    @TableField("is_delete")
    private Integer isDelete;
}
