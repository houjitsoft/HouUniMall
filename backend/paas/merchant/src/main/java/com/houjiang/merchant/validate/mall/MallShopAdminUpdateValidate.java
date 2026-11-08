package com.houjiang.merchant.validate.mall;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class MallShopAdminUpdateValidate implements Serializable {
    @IDMust private Integer id;
    private Integer shopId;
    private Long userId;
    @JsonProperty("super") private Integer isSuper;
    @NotEmpty private String name;
    private String face;
    @NotNull private Integer status;
    @NotNull private Integer loginAll;
    @NotNull private List<Integer> roleId;
    private List<Integer> deptId;
    private List<Integer> jobsId;
}
