package com.houjiang.merchant.validate.system;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SystemRoleUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "id", required = true)
    private Integer id;

    @NotNull(message = "缺少参数name")
    @Length(min = 1, max = 30, message = "角色名称必须在1~30个字符内")
    @Schema(description = "角色名称", required = true)
    private String name;

    @Length(max = 200, message = "描述信息不能超过200个字符")
    @Schema(description = "描述", required = true)
    private String desc;

    @DecimalMin(value = "0", message = "排序号值不能少于0")
    @Schema(description = "排序")
    private Integer sort = 0;


    @Schema(description = "菜单权限")
    private List<Integer> menuId;

}
