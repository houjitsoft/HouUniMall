package com.houjiang.userms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 站内信
 * @author Amos
 */
@Data
public class UserPlatformMessageDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 接受者id
     */
    private String userId;

    /**
     * 消息标题
     */
    private String messageTitle;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 0:未读；1:已读
     */
    private Integer isReader;

    /**
     * 创建者
     */
    private String creater;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新人
     */
    private String updater;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户密钥
     */
    private String userKey;

    /**
     * 用户删除标志
     */
    private Integer userDelete;

    /**
     * 权限组ID
     */
    private Integer rightsGroupId;

    /**
     * 子站点ID
     */
    private Integer subSiteId;

    /**
     * 省份ID
     */
    private Integer provinceId;

    /**
     * 城市ID
     */
    private Integer cityId;

    /**
     * 区域ID
     */
    private Integer districtId;

    /**
     * 地址
     */
    private String address;

    /**
     * 邀请子站点ID
     */
    private int inviteSubsiteId;

    /**
     * 用户父店铺ID
     */
    private int userParentShopId;

    /**
     * 权限组名称
     */
    private String rightsGroupName;

    /**
     * 权限组权限ID列表
     */
    private String rightsGroupRightsIds;

    /**
     * 权限组添加时间
     */
    private String rightsGroupAddTime;

    /**
     * 权限限制（全部）
     */
    private String rightsLimitsAll;

    /**
     * 权限限制（状态）
     */
    private String rightsLimitsState;

    /**
     * 头像
     */
    private String headPortrait;

    /**
     * 用户真实姓名
     */
    private String userRealname;
}