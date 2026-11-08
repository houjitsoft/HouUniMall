package com.houjiang.payment.dto.mch;

import lombok.Data;

import java.io.Serializable;

/**
 * 平台商户列表分页查询请求 DTO
 * @author Amos
 */
@Data
public class MerchantListRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer pageNo = 1;

    private Integer pageSize = 20;

    /** FE 兼容：pageNum 映射到 pageNo */
    public void setPageNum(Integer pageNum) {
        if (pageNum != null) {
            this.pageNo = pageNum;
        }
    }

    /** 商户号（精确查询） */
    private String mchNo;

    /** 商户类型 */
    private Integer mchType;

    /** 商户名称（模糊） */
    private String companyName;

    /** 状态 */
    private Integer status;

    /** 法人手机号（精确） */
    private String legalPhone;

    /** 创建开始时间 yyyy-MM-dd */
    private String startDate;

    /** 创建结束时间 yyyy-MM-dd */
    private String endDate;
}
