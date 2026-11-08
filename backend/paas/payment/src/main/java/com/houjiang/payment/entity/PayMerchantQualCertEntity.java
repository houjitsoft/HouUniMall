package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商户资质证书子表（hou_pay_merchant_qual_cert）
 * <p>
 * 一个资质对应多张证书（如支付宝需要 3 张证书：alipayCertPublicKey / alipayRootCert / appCertPublicKey）。
 *
 * @author Amos
 * @since 2026-05-13
 */
@Data
@TableName("hou_pay_merchant_qual_cert")
public class PayMerchantQualCertEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 资质主表 ID */
    @TableField("qual_id")
    private Long qualId;

    /** 证书类型：WX_CERT_P12/ALI_PUBLIC/ALI_ROOT/APP_PUBLIC/UNIONPAY_PFX 等 */
    @TableField("cert_type")
    private String certType;

    /** 证书服务器路径 */
    @TableField("cert_path")
    private String certPath;

    /** 证书序列号 */
    @TableField("cert_serial_no")
    private String certSerialNo;

    /** 证书过期时间 */
    @TableField("expire_time")
    private Date expireTime;

    /** 上传时间 */
    @TableField("create_time")
    private Date createTime;
}
