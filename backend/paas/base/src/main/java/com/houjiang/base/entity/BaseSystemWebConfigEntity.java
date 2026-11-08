package com.houjiang.base.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 网站配置表(BaseSystemWebConfig)表实体类
 *
 * @author Amos
 * @since 2023-11-01 13:43:32
 */
@Data
@TableName("hou_base_system_web_config")
public class BaseSystemWebConfigEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
        
    /**
    *数组下标
    */
    @TableId
    private String configKey;
    /**
    *数组值
    */
    private String configValue;
    private String configType;
    /**
    *状态值，1可能，0不可用
    */
    private Integer configEnable;
    private String configComment;
    /**
    *数据类型
    */
    private String configDatatype;

}

