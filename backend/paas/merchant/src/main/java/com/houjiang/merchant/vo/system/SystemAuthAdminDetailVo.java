package com.houjiang.merchant.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SystemAuthAdminDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "是否超级管理员 0-否 1-是")
    private Integer root;

    @Schema(description = "角色ID")
    private List<Integer> roleId;

    @Schema(description = "部门ID")
    private List<Integer> deptId;

    @Schema(description = "岗位ID")
    private List<Integer> jobsId;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "昵称")
    private String name;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "是否禁用: [0=否, 1=是]")
    private Integer disable;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    private String loginTime;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "是否支持多处登录：1-是；0-否；")
    private Integer multipointLogin;

}
