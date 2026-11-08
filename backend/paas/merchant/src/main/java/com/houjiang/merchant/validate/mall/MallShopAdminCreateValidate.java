package com.houjiang.merchant.validate.mall;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class MallShopAdminCreateValidate implements Serializable {
    private Integer shopId;
    @NotNull private Long userId;
    @NotNull @JsonProperty("super") private Integer isSuper;
    @NotEmpty private String name;
    private String face;
    @NotNull private Integer status;
    @NotNull private Integer loginAll;
    @NotNull private List<Integer> roleId;
    private List<Integer> deptId;
    private List<Integer> jobsId;
}
