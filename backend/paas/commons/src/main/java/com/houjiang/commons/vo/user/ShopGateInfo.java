package com.houjiang.commons.vo.user;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ShopGateInfo {
    /**
     * 门店ID
     */
    private Long shopGateId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 所属店铺ID
     */
    private Long shopId;
    /**
     * 登录名
     */
    private String loginName;
    /**
     * 店铺状态：0.关闭；1.开启
     */
    private Integer state;
    /**
     * 门店名称
     */
    private String shopGateName;
    /**
     * 所属区域
     */
    private String region;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 门店联系电话
     */
    private String gatePhone;
    /**
     * 店铺联系方式
     */
    private String shopTel;
    /**
     * 营业时间
     */
    private String businessTime;
    /**
     * 交通路线
     */
    private String trafficRoute;
    /**
     * 权限
     */
    private String limits;
    /**
     * 店铺类型 1 商家店铺 2 供应商店铺 3 品牌店铺
     */
    private String shopType;
    /**
     * 实况照片URL
     */
    private String gatePic;
    /**
     * 所属店铺名称
     */
    private String shopName;
    /**
     * 店长姓名
     */
    private String gateManagerName;
    /**
     * 店长联系方式
     */
    private String gateManagerPhone;

    /** 经度 */
    private String longitude;

    /** 纬度 */
    private String latitude;

}
