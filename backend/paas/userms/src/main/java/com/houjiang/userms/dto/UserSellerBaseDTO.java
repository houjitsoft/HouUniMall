package com.houjiang.userms.dto;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName UserSellerBaseDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/20 15:11
 * @Version 1.0
 * @author Amos
 */
@Data
public class UserSellerBaseDTO {

    /**
     * 商家ID
     */
    private Integer sellerId;
    /**
     * 商家名称
     */
    private String sellerName;
    /**
     * 商铺ID
     */
    private Integer shopId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 权限组ID
     */
    private Integer rightsGroupId;
    /**
     * 商家是否管理员，0为非管理员，1为管理员
     */
    private Integer sellerIsAdmin;
    /**
     * 商家登录时间
     */
    private Date sellerLoginTime;
    /**
     * 商家所属权限组ID
     */
    private Integer sellerGroupId;
    /**
     * 分组ID
     */
    private Integer groupId;
    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 访问令牌
     */
    private String token;
    /**
     * 用户手机号
     */
    private String userMobile;
    /**
     * 密码
     */
    private String password;

    /**
     * 1卖家店铺 2供应商店铺 3 品牌店铺
     */
    private Integer shopType;
}
