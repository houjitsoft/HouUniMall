

package com.houjiang.userms.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 * @create 2018/9/6
 */
@Data
public class UserInfoDetailVO {

    /**
     * 用户id
     */
    private String userId;
    /**
     *用户名
     */
    private String userName;
    /**
     *密码
     */
    private String password;
    /**
     *状态(0:未激活,1:未认证,2:已认证,3:锁定)'
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
     *缓存id
     */
    private String sessionId;
    /**
     *上级推广员ID
     */
    private Integer userParentId;
    /**
     * 上级店铺ID
     */
    private Integer userParentShopId;
    /**
     * 邀请注册的城市分站ID
     */
    private Integer inviteSubsiteId;
    /**
     *是否是天九会员,1-是，0-不是，默认0，不是
     */
    private Integer isTjMember;
    /**
     * 注册用户来源 1.pc;2.h5;3.小程序; 4:app
     */
    private Integer userPlatform;
    /**
     *客户端token
     */
    private String clientToken;
    /**
     *商户token
     */
    private String merchantToken;
    /**
     *手机号码
     */
    private String userMobile;
    /**
     *最后登录时间
     */
    private Date userLastloginTime;
    /**
     *
     */
    private String token;
    /**
     *头像地址
     */
    private String userAvatar;
    /**
     *性别 0保密1男2女
     */
    private Integer userGender;

    /**
     *卖家id
     */
    private Integer sellerId;
    /**
     *卖家用户名
     */
    private String sellerName;
    /**
     *店铺ID
     */
    private Integer shopId;
    /**
     * 1卖家店铺 2供应商店铺
     */
    private Integer shopType;
    /**
     *是否管理员(0-不是 1-是)
     */
    private Integer sellerIsAdmin;
    /**
     *权限
     */
    private String limits;
    /**
     *是否为新用户 0 不是  1是
     */
    private String userIsNew;
    /**
     *是否已手机认证 0 否 1是
     */
    private Integer userMobileVerify;
    /**
     *用户昵称
     */
    private String userNickname;
    /**
     *第三方用户身份认证编号
     */
    private String bindOpenid;
    /**
     *真实姓名
     */
    private String userTruename;
    /**
     *绑定店铺id
     */
    private Integer userBindShopId;
    /**
     *绑定门店id
     */
    private Integer userBindStoreId;
    /**
     *绑定门店名
     */
    private String userBindStoreName;




}
