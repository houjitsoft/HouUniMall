package com.houjiang.base.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName ActivityGoodsDetailVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:52
 * @Version 1.0
 * @author Amos
 */
@Data
public class ActivityGoodsDetailVO {

    /**
     * 活动价格
     */
    private BigDecimal activityPrice;

    /**
     * 剩余时间
     */
    private Long overplusTime;

    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date activityEndTime;

    /**
     * 购买上限0为不限制
     */
    private Integer goodsUpperLimit;

    /**
     * 购买下限，0为不限制
     */
    private Integer goodsLowerLimit;

    /**
     * 已购买数据
     */
    private Integer goodsBuyCount;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团
     */
    private Integer activityStatus;

    /**
     * 团购价格
     */
    private BigDecimal pintuanPrice;

    /**
     * 单独购买价格
     */
    private BigDecimal priceOne;

    /**
     * 是否是同一个团的，对应hou_pingtuan_mark表
     */
    private String markId;

    /**
     * 0拼团，1单独购买
     */
    private Integer type;

    /**
     * 满减赠折规则
     */
    private List<ManjianzengzheGoodsRuleQueryVO> ruleList;

    /**
     * 满减赠折活动Id
     */
    private Integer promotionId;

    /**
     * 满减赠折价格
     */
    private BigDecimal mjzzPrice;

    private String discountTitle;
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date activityStartTime;

    private BigDecimal discountAmount;

    private Integer discountUpperTime;

}
