package com.houjiang.commons.vo.activity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ManjianzengzheGoodsRuleQueryVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManjianzengzheGoodsRuleQueryVO {

    /**
     * 活动ID
     */
    private Integer promotionId;

    /**
     * 是否有赠品
     */
    private Integer hasGiveaway;

    /**
     * 赠品ID
     */
    private Integer giveawayId;

    /**
     * 满足金额
     */
    private BigDecimal meetAmount;

    /**
     * 优惠内容
     */
    private String discountContent;

    /**
     * 折扣类型
     */
    private Integer discountType;

    /**
     * 赠品名称
     */
    private String giveawayName;

    /**
     * 赠品图片
     */
    private String giveawayImage;


}
