package com.houjiang.mall.entity.market;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 插件开发者收款账户（hou_plugin_seller_account）。
 *
 * <p><b>账号字段是敏感数据</b>：接口回显时必须脱敏（只留后四位），
 * 完整值只在结算打款流程内部使用。数据库列宽留足 255 以便生产环境改为密文存储。
 *
 * @author Amos
 * @since 2026-10-27
 */
@Data
@TableName("hou_plugin_seller_account")
public class PluginSellerAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 0=待审核 1=可用 2=停用 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ACTIVE = 1;
    public static final int STATUS_DISABLED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long developerId;

    /** BANK / ALIPAY / WECHAT */
    private String settleType;

    private String accountName;

    /** 收款账号（生产环境应加密存储） */
    private String accountNo;

    private String bankName;

    /** 0=待审核 1=可用 2=停用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
