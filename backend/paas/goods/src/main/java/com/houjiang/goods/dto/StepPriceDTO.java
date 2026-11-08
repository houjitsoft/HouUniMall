package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;

/**
 * 商品价格
 * @author Amos
 */
@Data
public class StepPriceDTO {

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
