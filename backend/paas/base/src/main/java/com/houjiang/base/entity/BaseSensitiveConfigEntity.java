package com.houjiang.base.entity;


import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
/**
 * @ClassName
 * @Description  敏感词配置
 * @Author Amos
 * @Date 2024-02-02 09:44:24
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_base_sensitive_config")
@Data
public class BaseSensitiveConfigEntity  implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    /**
     * 敏感词开关 1 开 0 关
     */
    private Integer sensitiveStart;
    /**
     * 敏感词服务 1 平台服务  2 第三方服务
     */
    private Integer sensitiveServer;
    /**
     * 阿里云AccessKey ID
     */
    private String accessKeyId;
    /**
     * 阿里云AccessKey Secret
     */
    private String accessKeySecret;
    /**
     * 阿里云服务地域ID
     */
    private String regionId;
    /**
     * 会员信息 是否开启敏感词 1 是 0 否
     */
    private Integer memberInformationStart;
    /**
     * 产品评价 是否开启敏感词 1 是 0 否
     */
    private Integer productEvaluationStart;
    /**
     * 供应商信息 是否开启敏感词 1 是 0 否
     */
    private Integer supplierInformationStart;
    /**
     * 产品信息 是否开启敏感词 1 是 0 否
     */
    private Integer productInformationStart;
    /**
     * 促销活动 是否开启敏感词 1 是 0 否
     */
    private Integer salesPromotionStart;
    /**
     * 装修模块 是否开启敏感词 1 是 0 否
     */
    private Integer decorationModuleStart;
    /**
     * 原因管理 是否开启敏感词 1 是 0 否
     */
    private Integer causeManagementStart;
    /**
     * 文章管理 是否开启敏感词 1 是 0 否
     */
    private Integer articleManagementStart;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
