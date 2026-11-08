package com.houjiang.bffweb.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息VO
/**
 * 
 * @author Amos
 * @since 2025-01-01
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = -40356785423868312L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 国际区号
     */
    private String internationalDialingCode;

    /**
     * 验证码
     */
    private String code;

    /**
     * sign
     */
    private String sign;

    /**
     * 状态(0:未激活,1:未认证,2:已认证,3:锁定)
     */
    private Integer userState;

    /**
     * 登录时间
     */
    private Integer actionTime;

    /**
     * 登录ip
     */
    private String actionIp;

    /**
     * 缓存id
     */
    private String sessionId;

    /**
     * 上级推广员ID
     */
    private String userParentId;

    /**
     * 注册用户来源 1.pc;2.h5;3.小程序; 4:app
     */
    private Integer userPlatform;

    /**
     * 客户端token
     */
    private String clientToken;

    /**
     * 商户token
     */
    private String merchantToken;

    /**
     * 手机号码
     */
    private String userMobile;

    /**
     * token
     */
    private String token;

    /**
     * 头像地址
     */
    private String userAvatar;

    /**
     * 性别 0保密1男2女
     */
    private Integer userGender;

    /**
     * 卖家id
     */
    private Integer sellerId;

    /**
     * 卖家用户名
     */
    private String sellerName;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 门店ID
     */
    private String shopGateId;

    /**
     * 1卖家店铺 2供应商店铺
     */
    private Integer shopType;

    /**
     * 是否管理员(0-不是 1-是)
     */
    private Integer sellerIsAdmin;

    /**
     * 权限
     */
    private String limits;

    /**
     * 是否为新用户 0 不是  1是
     */
    private String userIsNew;

    /**
     * 是否已手机认证 0 否 1是
     */
    private Integer userMobileVerify;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 第三方用户身份认证编号
     */
    private String bindOpenid;

    /**
     * 是否商家账号: 1是 2否
     */
    private Integer isBusiness;

    /**
     * 是否是商户 0否 1是
     */
    private Integer isShop;

    /**
     * 是否微客 1是 2否
     */
    private Integer vkState;

    /**
     * 是否plus会员 0过期 1是 2否
     */
    private Integer plusMember;

    /**
     * 是否plus会员 套餐类型
     */
    private Integer thaliType;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 是否删除0:否;1:是
     */
    private Integer isDelete;

    /**
     * 是否是企业员工 0否 1是
     */
    private Integer isEmployee;

    /**
     * 未读消息数量
     */
    private Integer unreadMessageNum;

    /**
     * 上次登录IP
     */
    private String userLastloginIp;

    /**
     * 创建者
     */
    private String createUser;

    /**
     * 修改人
     */
    private String updateUser;

    /**
     * 登录类型:1:PC,2:后台
     */
    private Integer loginPlatform;

    /**
     * 登录类型:1:商家app
     */
    private String platForm;

    /**
     * 用户帐号(后台系统)
     */
    private String userAccount;

    /**
     * 密码(后台系统)
     */
    private String userPassword;

    /**
     * 用户Key(后台系统)
     */
    private String userKey;

    /**
     * 是否被封禁，0：未封禁，1：封禁(后台系统)
     */
    private Integer userDelete;

    /**
     * 用户权限组id(后台系统)
     */
    private Integer rightsGroupId;

    /**
     * 权限组名称(后台系统)
     */
    private String rightsGroupName;

    /**
     * 创建时间(后台系统)
     */
    private String rightsGroupAddTime;

    /**
     * 逗号分隔所有权限(后台系统)
     */
    private String rightsLimitsAll;

    /**
     * 用户头像(后台系统)
     */
    private String headPortrait;

    /**
     * 真实姓名(后台系统)
     */
    private String userRealname;

    /**
     * 用户积分
     */
    private Integer userIntegral;

    /**
     * 登录类型是否关联shopId:1.是,0.否
     */
    private Integer loginType;

    /**
     * 店铺属性
     */
    private Integer shopGradeId;
}

