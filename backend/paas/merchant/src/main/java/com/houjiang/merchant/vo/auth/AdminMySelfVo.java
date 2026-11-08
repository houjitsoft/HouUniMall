package com.houjiang.merchant.vo.auth;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class AdminMySelfVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "当前管理员角色拥有的菜单")
    private JSONArray menu;

    @Schema(description = "当前管理员橘色拥有的按钮权限")
    private List<String> permissions;

    @Schema(description = "user")
    private AuthMySelfVo user;
}
