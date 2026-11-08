package com.houjiang.merchant.validate.mall;

import com.alibaba.fastjson2.JSONArray;
import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家店铺参数
 * @author Amos
 */
@Data
@Schema(description = "商家店铺更新参数")
public class MallShopUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

/** @IDMust(message = "id参数必传且需大于0") */
    @Schema(description = "ID")
    private Integer id;

    @NotNull(message = "name参数缺失")
    @Schema(description = "名称")
    private String name;

    @NotNull(message = "logo参数缺失")
    @Schema(description = "logo")
    private String logo;

    @Schema(description = "店铺简介")
    private String desc;

/**
 * @NotNull(message = "shopCategoryId参数缺失")
 * @Schema(description = "经营类目")
 * private Integer shopCategoryId;
 */

    @NotNull(message = "shopStatus参数缺失")
    @Schema(description = "店铺状态 1正常 0冻结")
    private Integer shopStatus;

    @NotNull(message = "businessStatus参数缺失")
    @Schema(description = "经营状态 1营业中 0暂停营业")
    private Integer businessStatus;

    @NotNull(message = "contact参数缺失")
    @Schema(description = "联系人")
    private String contact;

    @NotNull(message = "mobile参数缺失")
    @Schema(description = "联系手机")
    private JSONArray mobile;

    @NotNull(message = "provinceId参数缺失")
    @Schema(description = "省")
    private Integer provinceId;

    @NotNull(message = "cityId参数缺失")
    @Schema(description = "市")
    private Integer cityId;

    @NotNull(message = "districtId参数缺失")
    @Schema(description = "区")
    private Integer districtId;

    @NotNull(message = "addressInfo参数缺失")
    @Schema(description = "详细地址")
    private String addressInfo;

    @NotNull(message = "lng参数缺失")
    @Schema(description = "纬度")
    private String lng;

    @NotNull(message = "lat参数缺失")
    @Schema(description = "经度")
    private String lat;

/**
 * @NotNull(message = "homeRecommend参数缺失")
 * @Schema(description = "精选店铺 1是 0否")
 * private Integer homeRecommend;
 */


    @NotNull(message = "businessLicense参数缺失")
    @Schema(description = "营业执照")
    private String businessLicense;

    @Schema(description = "其他执照")
    private JSONArray otherLicense;

    @Schema(description = "退货联系人")
    private String returnContact;
    @Schema(description = "退货联系电话")
    private String returnMobile;
    @Schema(description = "退货地址")
    private String returnAddressInfo;

    @Schema(description = "退货省份")
    private Integer returnProvinceId;

    @Schema(description = "退货城市")
    private Integer returnCityId;

    @Schema(description = "街道")
    private Integer returnDistrictId;

//    @NotNull(message = "serviceRatio参数缺失")
//    @Schema(description = "服务费百分比")
//    private String serviceRatio;


}
