package com.houjiang.mall.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 店铺信息实体类
 * 表名: hou_mall_shop
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_shop")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺编号
     */
    private String shopNo;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺Logo
     */
    private String shopLogo;

    /**
     * 店铺类型: 1-自营 2-入驻商家
     */
    private Integer shopType;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 商家名称
     */
    private String merchantName;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 店铺等级ID
     */
    private Long levelId;

    /**
     * 所在省份
     */
    private String province;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 所在区域
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * 营业执照号
     */
    private String licenseNo;

    /**
     * 法人代表
     */
    private String legalPerson;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 身份证正面
     */
    private String idCardFront;

    /**
     * 身份证反面
     */
    private String idCardBack;

    /**
     * 银行账户
     */
    private String bankAccount;

    /**
     * 开户行
     */
    private String bankName;

    /**
     * 账户名
     */
    private String accountName;

    /**
     * 结算费率%
     */
    private BigDecimal settlementRate;

    /**
     * 质保金金额
     */
    private BigDecimal qualityDeposit;

    /**
     * 已缴质保金
     */
    private BigDecimal paidDeposit;

    /**
     * 经营类目JSON
     */
    private String businessCategories;

    /**
     * 店铺简介
     */
    private String description;

    /**
     * 状态: 0-待审核 1-正常 2-冻结 3-关闭
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 开店时间
     */
    private LocalDateTime openTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
