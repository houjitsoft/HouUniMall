package com.houjiang.merchant.vo.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class AdminAccountDetailVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    private String createTime;
    @Schema(description = "部门关联")
    @JsonProperty(value = "dept_arr")
    private List<String> deptArr;
    @Schema(description = "部门关联ID")
    @JsonProperty(value = "dept_id")
    private List<Integer> deptId;
    @Schema(description = "管理员头像")
    private String face;
    @Schema(description = "职位")
    @JsonProperty(value = "job_arr")
    private List<String> jobArr;
    @Schema(description = "职位ID")
    @JsonProperty(value = "job_id")
    private List<Integer> jobId;
    @Schema(description = "允许多处登录 1支持 0支持")
    @JsonProperty(value = "login_all")
    private Integer loginAll;
    @Schema(description = "最后登录IP")
    @JsonProperty(value = "login_last_ip")
    private String loginLastIp;
    @Schema(description = "最后登录时间")
    @JsonProperty("login_last_time")
    private String loginLastTime;
    @Schema(description = "登录账号")
    @JsonProperty("login_account")
    private String loginAccount;
    private Long userId;
    private String userMobile;
    @Schema(description = "管理员名称")
    private String name;
    @Schema(description = "角色")
    @JsonProperty(value = "role_arr")
    private List<String> roleArr;
    @Schema(description = "角色ID")
    @JsonProperty(value = "role_id")
    private List<Integer> roleId;

    @Schema(description = "店铺id")
    @JsonProperty(value = "shop_id")
    private Integer shopId;
    @Schema(description = "状态 1正常 0停用")
    private Integer status;

    @Schema(description = "超级管理员 1是 0否")
    @JsonProperty("super")
    private Integer isSuper;

    @Schema(description = "修改时间")
    @JsonProperty("update_time")
    private String updateTime;
}
