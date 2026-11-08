package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户详细信息表
 * @TableName user_info_detail
 * @author Amos
 */
@TableName(value ="hou_user_info_detail")
@Data
public class UserInfoDetaileEntity implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;

    /**
     * 头像地址
     */
    private String userAvatar;

    /**
     * 性别 0保密1男2女
     */
    private Boolean userGender;

    /**
     * 真实姓名
     */
    private String userTruename;

    /**
     * 生日(YYYY-MM-DD)
     */
    private String userBirth;

    /**
     * 注册时间
     */
    private Long userRegTime;

    /**
     * 注册IP
     */
    private String userRegIp;
    /**
     * 是否已手机认证 0 否 1是
     */
    private Integer userMobileVerify;
    /**
     * 身份证
     */
    private String userIdcard;

    /**
     * 证件类型  1身份证 2护照 3军官证
     */
    private String userIdentityType;

    /**
     * 0未审核1待审核2审核成功3审核失败
     */
    private Integer userIdentityStatu;

    /**
     * 证件照反面
     */
    private String userIdentityFontLogo;

    /**
     * 证件照正面
     */
    private String userIdentityFaceLogo;

    /**
     * 身份证有效期开始时间
     */
    private Date userIdentityStartTime;

    /**
     * 身份证有效期结束时间
     */
    private Date userIdentityEndTime;

    /**
     * 上级推广员ID 
     */
    private String userParentId;

    /**
     * 注册用户来源 1.pc;2.h5;3.小程序; 4:微信app 5：ios
     */
    private Integer userPlatform;

    /**
     * 时间戳
     */
    private Date ts;

    /**
     * 用户绑定店铺id
     */
    private Integer userBindShopId;

    /**
     * 用户绑定门店id
     */
    private Integer userBindStoreId;

    /**
     * 是否长期有效:1-是；0-否
     */
    private Integer validateLongTerm;


    /**
     * 店铺id
     */
    private Integer outShopId;


    /**
     * 店铺名称
     */
    private String outShopName;
    /**
     * 店铺类型，1-餐厅/饭馆（用家）；2-批发/贸易商（批发）；3-超市/便利店（批发）；4-学校/工厂（批发）；5-电商（批发）
     */
    private Integer outShopType;

    /**
     * 智能云为客户编码
     */
    private String outShopCode;

    /**
     * 大区
     */
    private String regionName;
    /**
     * 办事处
     */
    private String officeName;
    /**
     * 片区
     */
    private String zoneName;
    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String area;

    /**
     * 销售队员
     */
    private String relationSaleName;
    /**
     * scrm店铺编码
     */
    private String bizShopCode;
    /**
     * 详细地址
     */
    private String shopDetailAddress;


    /**
     * 营业执照编号
     */
    private String businessLicenseRegNum;


    /**
     * 0-不是；1-是（黑名单的用户登录不了小程序）
     */
    private Integer isBlack;

    /**
     * 用户等级，1-一级决策者；2-二级扫码者
     */
    private Integer userLevel;

    /**
     * 消耗积分冻结状态，0-冻结；1-正常
     */
    private Integer placeOrderStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}