package com.houjiang.mall.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * Mall 管理员信息（对齐 like-admin 前端 user 字段读取习惯）
 * @author Amos
 */
@Data
public class MallAuthAdminVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer root;

    private String account;

    private String name;

    private String avatar;

    @JsonProperty("multipoint_login")
    private Integer multipointLogin;

    private Integer disable;
}
