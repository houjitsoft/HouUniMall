package com.houjiang.mall.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Mall 菜单 VO（对齐 like-admin SystemAuthMenuVo，前端使用 snake_case 字段）
 * @author Amos
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MallAuthMenuVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer pid;

    /** M=目录, C=菜单, A=按钮 */
    private String type;

    private String name;

    private String icon;

    private Integer sort;

    private String perms;

    private String paths;

    private String component;

    private String selected;

    private String params;

    @JsonProperty("is_cache")
    private Integer isCache;

    @JsonProperty("is_show")
    private Integer isShow;

    @JsonProperty("is_disable")
    private Integer isDisable;

    private List<MallAuthMenuVo> children;
}
