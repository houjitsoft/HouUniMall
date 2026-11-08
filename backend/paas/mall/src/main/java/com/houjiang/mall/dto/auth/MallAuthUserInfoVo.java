package com.houjiang.mall.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Mall 管理员信息 VO（对齐 like-admin AdminMySelfVo 中 user + permissions 结构）
 * @author Amos
 */
@Data
public class MallAuthUserInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> user;

    private List<String> permissions;

    /** 兼容 mall/admin 前端 perms 字段读取 */
    @JsonProperty("perms")
    public List<String> getPerms() {
        return permissions;
    }
}
