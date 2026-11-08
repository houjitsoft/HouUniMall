package com.houjiang.merchant.vo.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SystemAuthRoleVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色备注")
    private String desc;

    @Schema(description = "关联菜单")
    private Object menusId;

    @Schema(description = "成员数量")
    private Integer num;

    @Schema(description = "角色排序")
    private Integer sort;

    @Schema(description = "是否禁用: [0=否, 1=是]")
    private Integer isDisable;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

}
