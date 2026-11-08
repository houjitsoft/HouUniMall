package com.houjiang.commons.dto.shop;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class GateStoreUpdateDTO {

    /**
     * 店铺ID
     */
    private int shopId;

    /**
     * 店铺门店ID (不能为空)
     */
    @NotEmpty
    private Integer shopGateId;

    /**
     * 店铺门店名称
     */
    private String shopGateName;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 县/区
     */
    private String county;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 经度（用于存储数据库VARCHAR类型）
     */
    private String longitude2;

    /**
     * 纬度（用于存储数据库VARCHAR类型）
     */
    private String latitude2;

    /**
     * 门店电话
     */
    private String gatePhone;

    /**
     * 营业时间
     */
    private String businessTime;

    /**
     * 交通路线
     */
    private String trafficRoute;

    /**
     * 门店图片
     */
    private String gatePic;

    /**
     * 令牌
     */
    private String token;


    private String phone;
    private String password;
    private String confirmPassword;
    private String gatestoreName;
    private Integer kilo;

}
