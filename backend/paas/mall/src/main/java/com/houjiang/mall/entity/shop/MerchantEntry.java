package com.houjiang.mall.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家入驻申请实体类
 * 表名: hou_mall_merchant_entry
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_merchant_entry")
public class MerchantEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请单号
     */
    private String applyNo;

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
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺类型
     */
    private Integer shopType;

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
     * 经营类目JSON
     */
    private String businessCategories;

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
     * 申请备注
     */
    private String remark;

    /**
     * 状态: 0-待审核 1-审核通过 2-审核拒绝 3-已入驻
     */
    private Integer status;

    /**
     * 审核人
     */
    private Long auditBy;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 关联店铺ID
     */
    private Long shopId;

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
