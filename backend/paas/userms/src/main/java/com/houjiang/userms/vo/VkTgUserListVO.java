package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class VkTgUserListVO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 是否是微客
     */
    private Integer isVkUser;

    /**
     * 推广用户数
     */
    private Integer tgNum;

    /**
     * 分销订单数
     */
    private Integer fxOrderNum;

    /**
     * 分销佣金
     */
    private BigDecimal fxCommissionNum;

    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 成为微客时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date vkCreateTime;

}
