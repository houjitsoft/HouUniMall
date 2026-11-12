package com.houjiang.merchant.validate.mall;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 平台后台编辑商家店铺资料参数。 */
@Data
public class MallShopProvisionUpdateValidate implements Serializable {

    @NotNull(message = "店铺ID不能为空")
    @Min(value = 1, message = "店铺ID不正确")
    private Integer id;

    @NotBlank(message = "店铺名称不能为空")
    @Size(max = 100, message = "店铺名称不能超过100个字符")
    private String name;

    @NotNull(message = "请选择店铺类型")
    @Min(value = 0, message = "店铺类型不正确")
    @Max(value = 1, message = "店铺类型不正确")
    private Integer type;

    private Integer shopCategoryId;

    @Min(value = 1, message = "入驻类型不正确")
    @Max(value = 2, message = "入驻类型不正确")
    private Integer entryType;

    @Min(value = 1, message = "店铺等级不正确")
    private Integer shopLevelId;

    @NotBlank(message = "联系人不能为空")
    @Size(max = 30, message = "联系人不能超过30个字符")
    private String contact;

    @NotBlank(message = "联系手机不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的11位手机号")
    private String mobile;

    @Size(max = 255, message = "详细地址不能超过255个字符")
    private String addressInfo;

    private String lat;

    private String lng;

    @NotNull(message = "请选择店铺状态")
    @Min(0)
    @Max(1)
    private Integer shopStatus;

    @NotNull(message = "请选择营业状态")
    @Min(0)
    @Max(1)
    private Integer businessStatus;
}
