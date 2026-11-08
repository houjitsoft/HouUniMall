package com.houjiang.payment.dto.merchantQual;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 商户资质 - 列表/详情响应 VO
 * <p>
 * appSecret/apiKey 在 VO 中默认脱敏（仅返回 ****）；
 * 详情页若需要明文，传入 plainSensitive=true 才返回真实明文。
 *
 * @author Amos
 * @since 2026-05-13
 */
@Data
public class MerchantQualVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long tenantId;
    private String terminal;
    private String channel;
    private String qualName;
    private String env;
    private String appId;
    private String appSecret;
    private String mchId;
    private String apiKey;
    private String apiPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String extConfig;
    private Integer status;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private String updateBy;

    /** 关联证书列表 */
    private List<MerchantQualSaveRequest.CertItem> certs;
}
