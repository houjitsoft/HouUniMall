package com.houjiang.merchant.validate.mall;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/** 平台后台开通商家店铺及主账号参数。 */
@Data
public class MallShopProvisionCreateValidate implements Serializable {

    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100, message = "商家名称不能超过100个字符")
    private String name;

    @NotNull(message = "请选择商家类型")
    @Min(value = 0, message = "商家类型不正确")
    @Max(value = 1, message = "商家类型不正确")
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

    /** 地图选点纬度。 */
    private String lat;

    /** 地图选点经度。 */
    private String lng;

    @NotNull(message = "请选择商家状态")
    @Min(0)
    @Max(1)
    private Integer shopStatus;

    @NotNull(message = "请选择营业状态")
    @Min(0)
    @Max(1)
    private Integer businessStatus;

    /** existing=绑定统一账号，new=手机号或邮箱注册账号。 */
    private String accountMode;

    private String platformUserId;

    private String accountIdentifier;

    private String accountPassword;

    private String accountPasswordAgain;

    /** 兼容旧调用方传入的店铺后台登录账号。 */


}
