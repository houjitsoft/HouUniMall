package com.houjiang.merchant.vo.auth;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class AuthMySelfVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    @Schema(description = "账号")
    private String account;

    @Schema(description = "用户头像")
    private String avatar;
    @Schema(description = "部门关联ID")
    @JsonProperty(value = "dept_id")
    private List<Integer> deptId;
    @Schema(description = "当前管理员角色拥有的菜单")
    private Integer disable;
    @Schema(description = "当前管理员角色拥有的菜单")
    @JsonProperty(value = "jobs_id")
    private List<Integer> jobsId;

    @Schema(description = "当前管理员角色拥有的菜单")
    @JsonProperty(value = "multipoint_login")
    private Integer multipointLogin;
    @Schema(description = "当前管理员角色拥有的菜单")
    private String name;

    @Schema(description = "当前管理员角色拥有的菜单")
    @JsonProperty(value = "role_id")
    private List<Integer> roleId;

    @Schema(description = "当前管理员角色拥有的菜单")
    private Integer root;
}
