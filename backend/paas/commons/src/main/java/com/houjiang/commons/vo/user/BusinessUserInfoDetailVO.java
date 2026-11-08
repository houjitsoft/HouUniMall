package com.houjiang.commons.vo.user;

import lombok.Data;

/**
 * 用户部门详情响应信息
 * @author Amos
 */
@Data
public class BusinessUserInfoDetailVO {

    /**
     * 营业执照号
     */
    private Integer businessId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 税号
     */
    private String taxId;

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * 银行卡号
     */
    private String bankCardNo;

    /**
     * 注册银行
     */
    private String registerBank;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactNumber;

    /**
     * 公司省份id
     */
    private String companyProvinceId;

    /**
     * 公司城市id
     */
    private String companyCityId;

    /**
     * 公司地区id
     */
    private String companyDistrictId;

    /**
     * 公司详细地址
     */
    private String companyDetailAddress;

    /**
     * 公司地址
     */
    private String companyAddress;

    /**
     * 邮箱地址
     */
    private String mailAddress;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 审核状态:0-待审核,1-审核通过,2-审核不通过
     */
    private Integer isApproved;

    /**
     * 审核状态:0-待审核,1-审核通过,2-审核不通过
     */
    private Integer businessCategory;

    /**
     * 注册地址
     */
    private String registeredAddress;

    /**
     * 生产许可证
     */
    private String productionLicense;
}


