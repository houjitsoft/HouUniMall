package com.houjiang.commons.vo.user;

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
public class UserVkVO {

    private Integer id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 推广人数（下级微客数）
     */
    private Integer tgNum;

    /**
     * 分享人数
     */
    private Integer shareNum;

    /**
     * 来源  1平台自己添加  2.通过成为微客条件
     */
    private Integer source;

    /**
     * 推广佣金总额
     */
    private BigDecimal totalTgCommission;

    /**
     * 分享佣金总额
     */
    private BigDecimal totalShareCommission;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 已提现佣金总额
     */
    private BigDecimal withdrawnCommission;

    /**
     * 待提现佣金总额
     */
    private BigDecimal pendingWithdrawnCommission;

    /**
     * 微信openid
     */
    private String openId;
}
