package com.houjiang.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PromotionDTO
 * @Description 晋升
 * @Author Amos
 * @Date 2023/5/17 9:56
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDTO {

    /**
     * 店铺会员等级id
     */
    private String userId;

    /**
     * 买家id
     */
    private Integer userIntegral;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺消费金额
     */
    private Double totalConsumption;
}
