package com.houjiang.commons.vo.user;

import lombok.Data;

/**
 * 用户店铺
 * @author Amos
 */
@Data
public class UserPermissionsVO {

    /**
     * 目标
     */
    private Boolean flag;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺类型
     */
    private Integer shopType;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户电话
     */
    private String userMobile;

    /**
     * 最后登录时间
     */
    private String userLastloginTime;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 店铺logo
     */
    private String wapShopLogo;

    /**
     * 用户昵称
     */
    private String userNickname;

}
