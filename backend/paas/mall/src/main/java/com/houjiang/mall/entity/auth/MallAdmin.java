package com.houjiang.mall.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * Mall 管理员实体类
 * 对应表: hou_mall_admin
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_admin")
public class MallAdmin implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 管理员ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 是否超级管理员: 0=否, 1=是 */
    private Integer root;

    /** 管理员名称 */
    private String name;

    /** 头像 */
    private String avatar;

    /** 登录账号 */
    private String account;

    /** 登录密码（MD5加密） */
    private String password;

    /** 最后登录时间（时间戳） */
    private Integer loginTime;

    /** 最后登录IP */
    private String loginIp;

    /** 是否支持多点登录: 0=否, 1=是 */
    private Integer multipointLogin;

    /** 是否禁用: 0=正常, 1=禁用 */
    private Integer disable;

    /** 创建时间（时间戳） */
    private Integer createTime;

    /** 更新时间（时间戳） */
    private Integer updateTime;

    /** 删除时间（时间戳，0=未删除） */
    private Integer deleteTime;
}
