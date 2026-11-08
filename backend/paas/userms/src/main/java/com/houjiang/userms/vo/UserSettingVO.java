package com.houjiang.userms.vo;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName UserSettingVO
 * @Description 账号信息
 * @Author Amos
 * @Date 2023/6/25 10:13
 * @Version 1.0
 * @author Amos
 */
@Data
public class UserSettingVO {

    /**
     * 用户昵称 不唯一
     */
    private String userName;

    /**
     * 手机号码
     */
    private String userMobile;

    /**
     * 省份id
     */
    private String userProvinceid;

    /**
     * 城市id
     */
    private String userCityid;

    /**
     * 地区id
     */
    private String userAreaid;

    /**
     * 身份证
     */
    private String userIdcard;

    /**
     * 真实姓名
     */
    private String userTruename;

    /**
     * 证件类型  1身份证 2护照 3军官证
     */
    private String userIdentityType;

    /**
     * 证件照反面
     */
    private String userIdentityFontLogo;

    /**
     * 证件照正面
     */
    private String userIdentityFaceLogo;

    /**
     * 上次登录IP
     */
    private String userLastloginIp;

    /**
     * 上次登录地址
     */
    private String UserLastloginArea;

    /**
     * 身份证有效期开始时间
     */
    private Date userIdentityEndTime;

    /**
     * 身份证有效期开始时间
     */
    private Date userIdentityStartTime;

    /**
     * 是否长期有效:1-是；0-否
     */
    private Integer validateLongTerm;
    /**
     * 国际区号
     */
    private String internationalDialingCode;

    @Override
    public String toString() {
        return "UcenterSettingInfo [userName=" + userName + ", userMobile=" + userMobile + ", userProvinceid="
                + userProvinceid + ", userCityid=" + userCityid + ", userAreaid=" + userAreaid + ", userIdcard="
                + userIdcard + ", userTruename=" + userTruename + ", userIdentityType=" + userIdentityType
                + ", userIdentityFontLogo=" + userIdentityFontLogo + ", userIdentityFaceLogo=" + userIdentityFaceLogo
                + ", userLastloginIp=" + userLastloginIp + ", UserLastloginArea=" + UserLastloginArea
                + ", userIdentityEndTime=" + userIdentityEndTime + ", userIdentityStartTime=" + userIdentityStartTime
                + "]";
    }
}
