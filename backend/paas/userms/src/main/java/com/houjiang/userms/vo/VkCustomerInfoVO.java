package com.houjiang.userms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VkCustomerInfoVO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 用户性别
     */
    private Integer userSex;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 推广人数
     */
    private Integer tgNum;

    /**
     * 分销订单数
     */
    private Integer fxOrderNum;

    /**
     * 分销金额
     */
    private BigDecimal fxMoney;

    /**
     * 创建时间
     */
    private Date createTime;

}
