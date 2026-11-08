package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

import java.util.List;

/**
 * @ClassName PropertysVO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 10:58
 * @Version 1.0
 * @author Amos
 */

@Data
public class PropertysVO {

    private String propertyId;

    private String propertyName;

    private List<PropertysValueVO> items;
}
