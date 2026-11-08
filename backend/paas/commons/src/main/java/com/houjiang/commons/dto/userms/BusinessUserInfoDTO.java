package com.houjiang.commons.dto.userms;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName BusinessUserInfoDetail
 * @Description
 * @Author Amos
 * @Date 2022/5/7 14:49
 * @Version 1.0
 * @author Amos
 */
@Data
public class BusinessUserInfoDTO implements Serializable {

    private static final long serialVersionUID = -40356785423868312L;

    /**
     * 公司编号
     */
    private Integer businessId;

    /**
     * 公司税号
     */
    private String taxId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 公司地址
     */
    private String companyAddress;

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
     * 邮箱地址
     */
    private String mailAddress;

    /**
     * 关联用户ID
     */
    private String userId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 上次更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdateTime;

    /**
     * 审核状态:0-待审核,1-审核通过,2-审核不通过
     */
    private Integer isApproved;

    /**
     * 审核备注
     */
    private String auditComment;

    /**
     * 经营类目 0未选择 1贸易商 2生产企业 3工贸一体
     */
    private Boolean businessCategory;

    /**
     * 生产许可证
     */
    private String productionLicense;

    /**
     * 注册地址
     */
    private String registeredAddress;

    /**
     * 审批流状态 0关闭 1开启
     */
    private Integer approvalProcessStatus;

    /**
     * 0企业注册 1升级企业 2变更主体
     */
    private Integer isRegister;


}
