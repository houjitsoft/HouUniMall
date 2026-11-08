package com.houjiang.order.dto;

import lombok.Data;

/**
 * 网关详细信息
 * @author Amos
 */
@Data
public class GateDetailDTO {

    /**
     * 门店ID
     */
    private Integer shopGateId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 门店名字
     */
    private String shopGateName;

    /**
     * 门店描述
     */
    private String shopGateDesc;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区/县
     */
    private String county;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 联系电话
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
     * 实况照片URL
     */
    private String gatePic;


}
