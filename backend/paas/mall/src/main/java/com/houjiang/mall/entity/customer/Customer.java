package com.houjiang.mall.entity.customer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户信息实体类
 * 表名: hou_mall_customer
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_customer")
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID */
    private Long userId;

    /** 客户编号 */
    private String customerNo;

    /** 客户名称 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像 */
    private String avatar;

    /** 性别: 0-未知 1-男 2-女 */
    private Integer gender;

    /** 生日 */
    private LocalDate birthday;

    /** 省份 */
    private String province;

    /** 城市 */
    private String city;

    /** 区县 */
    private String district;

    /** 详细地址 */
    private String address;

    /** 客户分组ID */
    private Long groupId;

    /** 客户等级 */
    private Integer level;

    /** 积分 */
    private Integer points;

    /** 余额 */
    private BigDecimal balance;

    /** 累计消费金额 */
    private BigDecimal totalAmount;

    /** 订单数 */
    private Integer orderCount;

    /** 最后消费时间 */
    private LocalDateTime lastConsumeTime;

    /** 来源渠道 */
    private String source;

    /** 备注 */
    private String remark;

    /** 状态: 0-禁用 1-正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除 */
    @TableLogic
    private Integer deleted;
}
