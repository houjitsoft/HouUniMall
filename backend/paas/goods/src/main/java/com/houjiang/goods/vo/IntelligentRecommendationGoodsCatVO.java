package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName IntelligentRecommendationVO
 * @Author Amos
 * @Date 2025/5/14 16:49
 * @Version 1.0
 * @author Amos
 */
@Data
public class IntelligentRecommendationGoodsCatVO {
    /**
     * 商品commonId
     */
    private Integer commonId;
    /**
     * 商品分类id
     */
    private Integer catId;
    /**
     * 商品查询数量
     */
    private Integer queryNum;
}
