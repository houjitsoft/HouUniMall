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
 * 商户资质主表（hou_pay_merchant_qual）
 * <p>
 * 按"租户 + 终端 + 渠道 + 环境"四元组唯一，承载 16 终端 × 多支付渠道的统一资质。
 *
 * @author Amos
 * @since 2026-05-13
 */
@Data
@TableName("hou_pay_merchant_qual")
public class PayMerchantQualEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 租户 ID（单租户填 0） */
    @TableField("tenant_id")
    private Long tenantId;

    /** 终端编码（TerminalTypeEnum：WX_MINI/ALI_MINI/HARMONY/...） */
    @TableField("terminal")
    private String terminal;

    /** 支付渠道：WECHAT/ALIPAY/UNIONPAY/APPLE/GOOGLE/PAYPAL/STRIPE/HUAWEI */
    @TableField("channel")
    private String channel;

    /** 资质名称 */
    @TableField("qual_name")
    private String qualName;

    /** 环境：S 沙箱 P 生产 */
    @TableField("env")
    private String env;

    /** 平台 App ID */
    @TableField("app_id")
    private String appId;

    /** 平台 App Secret（库内 AES 加密） */
    @TableField("app_secret")
    private String appSecret;

    /** 渠道商户号 */
    @TableField("mch_id")
    private String mchId;

    /** 渠道 API 密钥 / RSA 私钥（库内 AES 加密） */
    @TableField("api_key")
    private String apiKey;

    /** 渠道 API 公钥 / 平台公钥 */
    @TableField("api_public_key")
    private String apiPublicKey;

    /** 异步回调地址 */
    @TableField("notify_url")
    private String notifyUrl;

    /** 同步跳转地址 */
    @TableField("return_url")
    private String returnUrl;

    /** 扩展配置（JSON） */
    @TableField("ext_config")
    private String extConfig;

    /** 状态：0 禁用 1 启用 */
    @TableField("status")
    private Integer status;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 创建时间 */
    @TableField("create_time")
    private Date createTime;

    /** 更新时间 */
    @TableField("update_time")
    private Date updateTime;

    /** 创建人 */
    @TableField("create_by")
    private String createBy;

    /** 更新人 */
    @TableField("update_by")
    private String updateBy;

    /** 逻辑删除：0 未删 1 已删 */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;
}
