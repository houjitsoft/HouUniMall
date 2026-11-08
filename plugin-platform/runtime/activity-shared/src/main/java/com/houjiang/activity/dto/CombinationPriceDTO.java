package com.houjiang.activity.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * (ActivityCombinationBase)表实体类
 *
 * @author Amos
 * @since 2024-04-16 11:26:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinationPriceDTO extends PageHelper {

    /**
     * 组合活动id
     */
    @NotNull(message = "活动id不能为空")
    private Integer combinationId;
        
    /**
    * 组合活动类型 1固定组合 2搭配组合 
    */
    @NotNull(message = "活动类型不能为空")
    @Range(min = 1,max = 2,message = "组合活动类型值范围1~2")
    private Integer combinationType;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 商品列表
     */
    @NotNull(message = "组合商品列表不能为空")
    private List<CombinationGoodsDTO> goodsList;

}
