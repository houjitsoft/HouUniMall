package com.houjiang.merchant.validate.shop;

import com.houjiang.merchant.validate.BaseParam;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotEmpty;

@Data
public class MallShopAccountUpdateValidate extends BaseParam {
    public interface upInfo {}
    @NotEmpty private String face;
    @NotEmpty @Length(min = 2, max = 30) private String name;
    @Length(min = 6, max = 32, groups = {create.class}) private String updateCurrentPassword;
    @NotEmpty(groups = {create.class}) @Length(min = 6, max = 32) private String updateNewPassword;
    @NotEmpty(groups = {create.class}) @Length(min = 6, max = 32) private String updateConfirmPassword;
}
