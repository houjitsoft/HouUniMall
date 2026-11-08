package com.houjiang.base.entity;



import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;



/**
 * @ClassName 
 * @Description  国家
 * @Author Amos
 * @Date 2024-10-22 15:59:38
 * @Version 1.0
 * @author Amos
 */ 
@TableName("hou_base_country")
@Data
public class BaseCountryEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer id;
        
     /**
     * 国家
     */        
    private String countryName;
        
     /**
     * 国际区号
     */        
    private String internationalDialingCode;
    /**
     * 是否允许删除 1 是  2 否
     */
    private Integer deleteType;
    /**
     * 国家首字母
     */
    private String countryInitial;
    /**
     * 是否常用 1 是 2 否
     */
    private Integer countryCommonUse;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

