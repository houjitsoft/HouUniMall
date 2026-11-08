package com.houjiang.payment.dto.mch;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 平台商户信息更新请求 DTO
 * 仅支持更新联系人、地址、经营范围等非核心字段
 * @author Amos
 */
@Data
public class MerchantUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "商户号不能为空")
    private String mchNo;

    private String mchShortName;

    private String licenseImg;

    private String legalEmail;

    private String legalIdFront;

    private String legalIdBack;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String provinceCode;

    private String cityCode;

    private String districtCode;

    private String address;

    private String businessScope;

    private String industryCode;
}
