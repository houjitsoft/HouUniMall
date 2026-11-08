package com.houjiang.merchant.common.entity.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class AdminJobs implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "管理员id")
    private Integer adminId;

    @Schema(description = "岗位id")
    private Integer jobsId;


}
