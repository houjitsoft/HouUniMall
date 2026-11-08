package com.houjiang.commons.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class UserSellerBaseVO {

    /**
     * 卖家id
     */
    private Integer sellerId;

    /**
     * 卖家名称
     */
    private String sellerName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 权限组id
     */
    private Integer rightsGroupId;

    /**
     * 卖家管理员
     */
    private Integer sellerIsAdmin;

    /**
     * 卖家登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date sellerLoginTime;

    /**
     * 卖家分组id
     */
    private Integer sellerGroupId;

    /**
     * 分组id
     */
    private Integer groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * token
     */
    private String token;

    /**
     * 手机号码
     */
    private String userMobile;

    /**
     * 密码
     */
    private String password;


}
