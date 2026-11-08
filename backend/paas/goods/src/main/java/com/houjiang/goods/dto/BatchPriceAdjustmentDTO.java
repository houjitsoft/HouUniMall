package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName BatchPriceAdjustmentDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/21 14:25
 * @Version 1.0
 * @author Amos
 */
@Data
public class BatchPriceAdjustmentDTO {

    /**
     * 类型1:按比例,2:按输入的价格调整
     */
    private Integer adjustmentType;

    /**
     * 类型1:上涨,2:下调
     */
    private Integer type;

    /**
     * 比例
     */
    private String proportion;

    /**
     * 积分支付比例
     */
    private Double pointsRate;

    private Integer catId;

    private List<Integer> commonIds;
}
