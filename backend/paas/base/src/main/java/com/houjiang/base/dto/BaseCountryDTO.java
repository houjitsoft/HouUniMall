package com.houjiang.base.dto;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.io.Serializable;


/**
 * @ClassName 
 * @Description  国家
 * @Author Amos
 * @Date 2024-10-22 15:59:38
 * @Version 1.0
 * @author Amos
 */ 
@Data
public class BaseCountryDTO extends PageHelper {

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
     * 国家首字母
     */
    private String countryInitial;
    /**
     * 是否常用 1 是 2 否
     */
    private Integer countryCommonUse;
}

