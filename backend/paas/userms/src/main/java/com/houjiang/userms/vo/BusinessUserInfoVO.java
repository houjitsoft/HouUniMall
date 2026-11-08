package com.houjiang.userms.vo;

import lombok.Data;

/**
 * @ClassName BusinessUserInfoVO
 * @Description 企业用户信息详细表
 * @Author Amos
 * @Date 2023/6/25 9:45
 * @Version 1.0
 * @author Amos
 */
@Data
public class BusinessUserInfoVO {

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司税号
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
     * 开户行
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
     * 公司地址_省
     */
    private String companyProvinceId;

    /**
     * 公司地址_市
     */
    private String companyCityId;

    /**
     * 公司地址_区
     */
    private String companyDistrictId;

    /**
     * 公司地址_详细地址
     */
    private String companyDetailAddress;

    /**
     *
     */
    private String companyAddress;

    /**
     * 邮箱地址
     */
    private String mailAddress;

    /**
     * 关联用户ID
     */
    private String userId;

    /**
     * 审核状态:0-待审核,1-审核通过,2-审核不通过
     */
    private Integer isApproved;
}
