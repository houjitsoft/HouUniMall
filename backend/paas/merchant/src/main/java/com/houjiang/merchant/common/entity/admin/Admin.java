package com.houjiang.merchant.common.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type=IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "是否超级管理员 0-否 1-是")
    private Integer root;

    @Schema(description = "用户账号")
    private String name;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "用户昵称")
    private String account;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "多端登录: [0=否, 1=是]")
    private Integer multipointLogin;

    @Schema(description = "是否禁用: [0=否, 1=是]")
    private Integer disable;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    private Long loginTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

}
