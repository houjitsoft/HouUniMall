package com.houjiang.commons.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 用户响应信息
 * @author Amos
 */
@Data
public class MemberMessageDetailVO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 注册时间
     */
    private String userRegTime;

    /**
     * 上次登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date userLastloginTime;

    /**
     * 上次登录IP
     */
    private String userLastloginIp;

    /**
     * 当前的会员积分
     */
    private Integer memberIntegral;

    /**
     * 真实名称
     */
    private String userTruename;

    /**
     * 生日
     */
    private String userBirth;

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
     * 性别 0保密1男2女
     */
    private Integer userGender;

    /**
     * 用户经验
     */
    private Integer userGrade;

    /**
     * 电话
     */
    private String userMobile;

    /**
     * 地区
     */
    private String area;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否是联营商，1-是，0-不是，默认0，不是'
     */
    private int isManagementBusiness;

    /**
     * 会员
     */
    private List<TagInfoVO> tagInfos;

    /**
     * 是否为企业用户
     */
    private Integer isBusiness;

    /**
     * 企业详情
     */
    private BusinessUserInfoDetailVO businessDetail;

    /**
     *
     */
    private Integer thaliStatus;


}
