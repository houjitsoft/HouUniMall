package com.houjiang.merchant.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class SystemAuthAdminSelvesVo {

    @Schema(description = "用户信息")
    private Object user;

    @Schema(description = "权限集合: [[*]=>所有权限, ['article:add']=>部分权限]")
    private Object permissions;

}
