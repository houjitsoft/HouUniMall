package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName SpecValueDTO
 * @Description SpecValueDTO
 * @Author Amos
 * @Date 2024/7/24 14:53
 * @Version 1.0
 * @author Amos
 */
@Data
public class SpecValueDTO {
    /**
     * 规格名
     */
    private String paramName;
    /**
     * 规格值
     */
    private String paramValue;
    /**
     * 排序
     */
    private Integer sort;
}
