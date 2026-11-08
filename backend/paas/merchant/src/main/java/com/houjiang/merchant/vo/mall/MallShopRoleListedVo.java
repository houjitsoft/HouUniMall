package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallShopRoleListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "角色名")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "管理员人数")
    private Long num;
    @Schema(description = "菜单ID")
    private List<Integer> menuId;
}
