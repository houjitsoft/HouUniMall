package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 快递100配置信息表
 * @TableName hou_base_kd100_config
 * @author Amos
 */
@TableName(value ="hou_base_kd100_config")
@Data
public class BaseKd100ConfigEntity implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 快递100 Customer
     */
    private String customer;

    /**
     * 快递100 Key
     */
    private String key;

    /**
     * 接口地址
     */
    private String apiUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
