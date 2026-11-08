package com.houjiang.mall.entity.distribution;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分销员实体类
 * 表名: hou_mall_distributor
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_distributor")
public class Distributor implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 分销员编号
     */
    private String distributorNo;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 分销等级ID
     */
    private Long levelId;

    /**
     * 上级分销员ID
     */
    private Long parentId;

    /**
     * 累计佣金
     */
    private BigDecimal totalCommission;

    /**
     * 已提现佣金
     */
    private BigDecimal withdrawnCommission;

    /**
     * 待结算佣金
     */
    private BigDecimal pendingCommission;

    /**
     * 可提现佣金
     */
    private BigDecimal availableCommission;

    /**
     * 推广订单数
     */
    private Integer orderCount;

    /**
     * 下级分销员数量
     */
    private Integer subCount;

    /**
     * 状态 0-禁用 1-正常
     */
    private Integer status;

    /**
     * 申请备注
     */
    private String applyRemark;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 上级分销员邀请码（非表字段）：后台列表展示与编辑回填用
     */
    @TableField(exist = false)
    private String parentNo;
}
