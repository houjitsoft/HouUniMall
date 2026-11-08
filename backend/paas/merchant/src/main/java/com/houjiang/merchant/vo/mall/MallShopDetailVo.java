package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallShopDetailVo implements Serializable {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    private String createTime;

    @Schema(description = "修改时间")
    @JsonProperty("update_time")
    private String updateTime;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "logo")
    private String logo;

    @Schema(description = "店铺简介")
    @JsonProperty("desc")
    private String descStr;

    @Schema(description = "经营类目")
    @JsonProperty("shop_category_id")
    private Integer shopCategoryId;
    @Schema(description = "经营类目")
    @JsonProperty("shop_category")
    private JSONObject shopCategory;

    @Schema(description = "类型 0自营 1入驻")
    private Integer type;

    @Schema(description = "类型 0自营 1入驻")
    @JsonProperty("type_text")
    private String typeText;

    @Schema(description = "店铺状态 1正常 0冻结")
    @JsonProperty("shop_status")
    private Integer shopStatus;

    @Schema(description = "经营状态 1营业中 0暂停营业")
    @JsonProperty("business_status")
    private Integer businessStatus;

    @Schema(description = "联系人")
    private String contact;

    @Schema(description = "联系手机")
    private JSONArray mobile;

    @Schema(description = "省")
    @JsonProperty("province_id")
    private Integer provinceId;

    @Schema(description = "市")
    @JsonProperty("city_id")
    private Integer cityId;

    @Schema(description = "区")
    @JsonProperty("district_id")
    private Integer districtId;

    @Schema(description = "详细地址")
    @JsonProperty("address_info")
    private String addressInfo;

    @Schema(description = "纬度")
    private String lng;

    @Schema(description = "经度")
    private String lat;

    @Schema(description = "退货联系人")
    @JsonProperty("return_contact")
    private String returnContact;

    @Schema(description = "退货联系手机")
    @JsonProperty("return_mobile")
    private String returnMobile;

    @Schema(description = "退货省份")
    @JsonProperty("return_province_id")
    private Integer returnProvinceId;

    @Schema(description = "退货城市")
    @JsonProperty("return_city_id")
    private Integer returnCityId;

    @Schema(description = "退货区县")
    @JsonProperty("return_district_id")
    private Integer returnDistrictId;

    @Schema(description = "退货详细地址")
    @JsonProperty("return_address_info")
    private String returnAddressInfo;

    @Schema(description = "精选店铺 1是 0否")
    @JsonProperty("home_recommend")
    private Integer homeRecommend;

    @Schema(description = "产品审核 1需要审核 0无需审核")
    @JsonProperty("goods_audit")
    private Integer goodsAudit;

    @Schema(description = "营业执照")
    @JsonProperty("business_license")
    private String businessLicense;

    @Schema(description = "其他执照")
    @JsonProperty("other_license")
    private JSONArray otherLicense;

    @Schema(description = "服务费百分比")
    @JsonProperty("service_ratio")
    private BigDecimal serviceRatio;

    @Schema(description = "店铺评分")
    private BigDecimal score;

    @Schema(description = "店铺星级")
    private BigDecimal star;

    @Schema(description = "进店人数")
    @JsonProperty("visit_num")
    private Integer visitNum;

    @Schema(description = "店铺余额")
    private BigDecimal money;

    @Schema(description = "营业额")
    @JsonProperty("sale_money")
    private BigDecimal saleMoney;

    @Schema(description = "成交订单")
    @JsonProperty("sale_orders")
    private Integer saleOrders;

    @Schema(description = "在售商品数")
    @JsonProperty("sale_online_goods")
    private Integer saleOnlineGoods;

    @Schema(description = "采集余额")
    @JsonProperty("gather_money")
    private Integer gatherMoney;

    @Schema(description = "提现百分比")
    @JsonProperty("withdraw_ratio")
    private BigDecimal withdrawRatio;

}
