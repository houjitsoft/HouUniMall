package com.houjiang.payment.dto.mch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台商户进件请求 DTO
 * 对标通企付商户入驻接口字段
 * @author Amos
 */
@Data
public class MerchantRegRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商户类型：1-企业 2-个体工商户 3-个人 */
    @NotNull(message = "商户类型不能为空")
    private Integer mchType;

    /** 企业/商户全称 */
    @NotBlank(message = "企业名称不能为空")
    private String companyName;

    /** 企业简称 */
    private String mchShortName;

    /** 统一社会信用代码（企业必填） */
    private String uniCredit;

    /** 营业执照图片URL */
    private String licenseImg;

    /** 法人姓名 */
    @NotBlank(message = "法人/负责人姓名不能为空")
    private String legalName;

    /** 证件类型：1-身份证 2-港澳居民来往内地通行证 3-护照 */
    private Integer legalIdType;

    /** 法人证件号 */
    @NotBlank(message = "法人证件号不能为空")
    private String legalIdNo;

    /** 法人手机号 */
    @NotBlank(message = "法人手机号不能为空")
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

    /** 收银宝商户号（可选，对接渠道时使用） */
    private String cusid;

    /** 拓展方/代理商编号 */
    private String agencyNo;

    /** 服务商ID */
    private String orgId;

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
}
