package com.houjiang.commons.vo.third;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 用户信息
 * @CreateDate: 2025/7/8 23:14
 * @author Amos
 */
@Data
public class ThirdUserVO implements Serializable {

    /**
     * 用户id
     */
    private Integer id;

    /**
     * 用户昵称
     */
    private String userNickName;
    /**
     * 用户姓名
     */
    private String userRealName;

    /**
     * 用户手机号
     */
    private String userPhone;
    /**
     * 岗位身份(dict表userIdentity)，1.餐厅老板 2.餐厅员工 3.厨师 4.采购 5.其他餐饮从业者 6.批发商
     */
    private Integer userIdentity;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 用户性别：0-未知；1-男；2-女
     */
    private Integer userGender;
    /**
     * 用户生日
     */
    private String userBirthday;


    /**
     * 用户注册时间，格式：2024-02-28 20:39:18
     */
    private String createTime;

    /**
     * 用户状态，0-失效；1-生效
     */
    private Integer status;

    /**
     * 是否黑名单，0-不是；1-是（黑名单的用户登录不了518小程序）
     */
    private Integer isBlack;

    /**
     * 会员等级
     */
    private String memberLevel;

    /**
     * 会员等级
     */
    private String memberLevelRuleDetailId;

    /**
     * 扫码冻结状态，0-冻结；1-正常
     */
    private String scanStatus;

    /**
     * 消耗积分冻结状态，0-冻结；1-正常
     */
    private Integer placeOrderStatus;

    /**
     * 客户类型ID(区分批发还是用家)
     */
    private Integer customerTypeId;
}
