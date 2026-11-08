package com.houjiang.payment.dto.merchantQual;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商户资质 - 新增/编辑请求
 *
 * @author Amos
 * @since 2026-05-13
 */
@Data
public class MerchantQualSaveRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键（编辑时必填） */
    private Long id;

    /** 终端编码 */
    private String terminal;

    /** 支付渠道 */
    private String channel;

    /** 资质名称 */
    private String qualName;

    /** 环境 S/P */
    private String env;

    /** 平台 App ID */
    private String appId;

    /** 平台 App Secret（明文，后端入库前自动 AES 加密） */
    private String appSecret;

    /** 渠道商户号 */
    private String mchId;

    /** API 密钥/RSA 私钥（明文，后端入库前自动 AES 加密） */
    private String apiKey;

    /** API 公钥 */
    private String apiPublicKey;

    /** 异步回调地址 */
    private String notifyUrl;

    /** 同步跳转地址 */
    private String returnUrl;

    /** 扩展配置 JSON 字符串 */
    private String extConfig;

    /** 状态：0/1 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 证书列表（可选） */
    private List<CertItem> certs;

    /**
     * 证书条目
     */
    @Data
    public static class CertItem implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 证书子表 ID（编辑保留） */
        private Long id;
        /** 证书类型 */
        private String certType;
        /** 服务器路径 */
        private String certPath;
        /** 序列号 */
        private String certSerialNo;
    }
}
