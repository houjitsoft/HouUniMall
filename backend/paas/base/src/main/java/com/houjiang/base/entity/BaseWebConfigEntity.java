package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 网站配置表
 * @TableName base_web_config
 * @author Amos
 */
@TableName(value ="hou_base_web_config")
@Data
public class BaseWebConfigEntity implements Serializable {
    /**
     * 配置键
     */
    @TableId
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 类型
     */
    private String configType;

    /**
     * 状态值，1可能，0不可用
     */
    private String configEnable;

    /**
     * 名称解释
     */
    private String configComment;

    /**
     * 数据类型
     */
    private String configDatatype;

    /**
     * 备注
     */
    private String configRemarks;

    /**
     * 颜色数组
     */
    @TableField(exist = false)
    private List<String> colorList;

    /**
     * 系统还是自定义数组
     */
    @TableField(exist = false)
    private List<Integer> setList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}