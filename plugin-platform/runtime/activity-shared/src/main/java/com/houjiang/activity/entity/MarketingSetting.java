package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 营销设置实体类
 * 对应表: hou_mall_marketing_setting
 *
 * @author Amos
 * @since 2026-05-22
 */
@Data
@TableName("hou_mall_marketing_setting")
public class MarketingSetting {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 配置键（如: enable_coupon）
     */
    private String settingKey;
    
    /**
     * 配置值（如: 1, true, 100）
     */
    private String settingValue;
    
    /**
     * 值类型: boolean/string/int
     */
    private String settingType;
    
    /**
     * 配置说明
     */
    private String description;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
