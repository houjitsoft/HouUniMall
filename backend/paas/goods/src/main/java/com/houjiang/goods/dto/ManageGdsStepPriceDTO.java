package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageGdsStepPriceDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/11 10:52
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsStepPriceDTO {

    /**
     * 价格集合
     */
    private List<StepPriceDTO> gdsStepPrices;
    /**
     * 会员等级
     */
    private Integer gradeId;   
    /**
     * 数量
     */
    private Integer mumCount;  
    /**
     * 价格
     */
    private Double price;      
    /**
     * id
     */
    private Integer id;        
}
