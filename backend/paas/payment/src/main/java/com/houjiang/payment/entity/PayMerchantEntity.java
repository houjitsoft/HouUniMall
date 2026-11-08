package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 支付商户主表（hou_pay_merchant）
 * 对标通企付企业/个人商户基本信息入驻
 *
 * @author Amos
 */
@Data
@TableName("hou_pay_merchant")
public class PayMerchantEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 平台分配商户号（唯一） */
    private String mchNo;

    /** 应用ID（商户侧） */
    private String appId;

    /** 商户类型：1-企业 2-个体工商户 3-个人 */
    private Integer mchType;

    /** 企业/商户全称 */
    private String companyName;

    /** 企业简称 */
    private String mchShortName;

    /** 统一社会信用代码 */
    private String uniCredit;

    /** 营业执照图片URL */
    private String licenseImg;

    /** 法人/负责人姓名 */
    private String legalName;

    /** 证件类型：1-身份证 2-港澳居民来往内地通行证 3-护照 */
    private Integer legalIdType;

    /** 法人证件号 */
    private String legalIdNo;

    /** 法人手机号 */
    private String legalPhone;

    /** 法人邮箱 */
    private String legalEmail;

    /** 法人证件正面URL */
    private String legalIdFront;

    /** 法人证件背面URL */
    private String legalIdBack;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人手机 */
    private String contactPhone;

    /** 联系人邮箱 */
    private String contactEmail;

    /** 省份代码 */
    private String provinceCode;

    /** 城市代码 */
    private String cityCode;

    /** 区县代码 */
    private String districtCode;

    /** 详细地址 */
    private String address;

    /** 经营范围 */
    private String businessScope;

    /** 行业编码（MCC） */
    private String industryCode;

    /** 商户后台登录账号 */
    private String loginUsername;

    /** 收银宝商户号 */
    private String cusid;

    /** 拓展方/代理商编号 */
    private String agencyNo;

    /** 服务商ID */
    private String orgId;

    /** 商户状态：0-待审核 1-审核中 2-已生效 3-已冻结 4-已注销 5-审核失败 */
    private Integer status;

    /** 状态备注 */
    private String statusRemark;

    /** 审核通过时间 */
    private Date approvedTime;

    /** 审核人ID */
    private String approvedBy;

    /** 冻结时间 */
    private Date frozenTime;

    /** 冻结原因 */
    private String frozenReason;

    /** 结算账户类型：1-对公 2-对私 */
    private Integer settleType;

    /** 结算银行编码 */
    private String settleBankCode;

    /** 结算银行名称 */
    private String settleBankName;

    /** 开户支行名称 */
    private String settleBranchName;

    /** 结算账户户名 */
    private String settleAccountName;

    /** 结算账号 */
    private String settleAccountNo;

    /** 银行预留手机号 */
    private String settleReservedMobile;

    /** 结算周期：0-D0 1-T1 2-T2 3-周结 4-月结 */
    private Integer settleCycle;

    /** 创建人ID */
    private String creatorId;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
