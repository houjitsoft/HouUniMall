package com.houjiang.goods.vo;


import com.houjiang.goods.dto.StepPriceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 商品价格
 * @author Amos
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GoodsStepPriceVO {

    /**
     * 价格集合
     */
    private List<StepPriceDTO> gdsStepPrices;

    /**
     * ID
     */
    private Integer id;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 商品ID
     */
    private Integer goodsId;

    /**
     * 会员等级ID
     */
    private Integer gradeId;

    /**
     * 数量
     */
    private Integer numCount;

    /**
     * 价格
     */
    private Double price;

    /**
     * 会员等级名称
     */
    private String gradeName;

}
