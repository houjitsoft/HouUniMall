package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopAdminDeptDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "管理员id")
    private Integer adminId;

    @Schema(description = "部门id")
    private Integer deptId;


}
