package com.houjiang.userms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName ImUserInfoVO
 * @Author Amos
 * @Date 2024/11/5 17:12
 * @Version 1.0
 * @author Amos
 */
@Data
public class ImUserInfoVO {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 手机号
     */
    private String userMobile;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 性别 0保密1男2女
     */
    private Integer userGender;
    /**
     * 注册时间
     */
    private Date userRegTime;
    /**
     * 会员等级
     */
    private String memberName;
    /**
     * 消费金额
     */
    private BigDecimal consumeAmount = new BigDecimal(0);
    /**
     * 下单数
     */
    private Integer orderNum = 0;

}
