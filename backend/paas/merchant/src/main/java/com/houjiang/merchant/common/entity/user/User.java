package com.houjiang.merchant.common.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "编号")
    private Integer sn;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户账号")
    private String account;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "用户电话")
    private String mobile;

    @Schema(description = "用户性别: [1=男, 2=女]")
    private Integer sex;

    @Schema(description = "生日")
    private Integer birthday;

    @Schema(description = "注册渠道")
    private Integer channel;

    @Schema(description = "是否禁用: [0=否, 1=是]")
    private Integer isDisable;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    private Long loginTime;

    @Schema(description = "是否为新用户: [0=否, 1=是]")
    private Integer isNewUser;

    @Schema(description = "用户钱包")
    private BigDecimal userMoney;

    @Schema(description = "使用的余额")
    private BigDecimal totalUseMoney;

    @Schema(description = "订单金额")
    private BigDecimal totalOrderMoney;

    @Schema(description = "订单数")
    private BigDecimal totalOrderNum;

    @Schema(description = "累计充值")
    private BigDecimal totalRechargeMoney;

    @Schema(description = "最近下单时间")
    private Long orderLastTime;

    @Schema(description = "邀请人id")
    private Integer inviteId;

    @Schema(description = "后台已更改推荐人 0否 1是")
    private Integer adminUpdateLeader;

    @Schema(description = "祖级id")
    private Integer gid;

    @Schema(description = "父级id")
    private Integer pid;

    @Schema(description = "粉丝数")
    private Integer nextNums;

    @Schema(description = "用户等级id")
    private Integer userLevelId;

    @Schema(description = "是否分销商")
    private Integer isDistribution;

    @Schema(description = "成为分销商时间")
    private Long distributionTime;

    @Schema(description = "分销商状态 1正常 0冻结")
    private Integer distributionStatus;

    @Schema(description = "分销商等级id")
    private Integer distributionLevelId;

    @Schema(description = "分销佣金")
    private BigDecimal distributionMoney;

    @Schema(description = "总收入佣金")
    private BigDecimal distributionTotalMoney;

    @Schema(description = "分享码")
    private String distributionCode;

    @Schema(description = "绑定时间")
    private Long distributionBindTime;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;


}
