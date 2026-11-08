package com.houjiang.merchant.validate.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SystemAdminSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "用户昵称")
    private String name;

    @Schema(description = "角色ID")
    private Integer role_id;

    @Schema(description = "导出信息")
    private Integer export;
    @Schema(description = "file_name")
    private String file_name;

    @Schema(description = "page_start")
    private Integer page_start;

    @Schema(description = "page_end")
    private Integer page_end;

    @Schema(description = "page_size")
    private Integer page_size;

    @Schema(description = "page_type")
    private Integer page_type;

}
