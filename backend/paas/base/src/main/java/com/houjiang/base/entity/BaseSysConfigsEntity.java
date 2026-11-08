package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 配置表(MerchantSysConfigs)
 *
 * @since 2023-05-10 14:00:51
 * @author Amos
 */
@TableName("hou_base_sys_configs")
@Data
public class BaseSysConfigsEntity implements Serializable {
    /**
     * 配置键
     */
    @TableId
    private String configKey;
    /**
     * 配置值:
     */
    private String configValue;
    /**
     * 配置类型
     */
    private String configType;
    /**
     * 配置内容描述
     */
    private String configComment;
    /**
     * 配置排序
     */
    private Integer configSort;

    /**
     * 操作类型
     */
    private String opType;

    /**
     * 操作类型值
     */
    private String opValue;
    /**
     * 状态值，1可用，0不可用
     */
    private Integer configEnable;
    /**
     * 终端选择，1pc，2移动端
     */
    private Integer terminalType;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateDate;

    @TableField(exist = false)
    private String commonName;
    @TableField(exist = false)
    private String shopName;
    @TableField(exist = false)
    private String catName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

