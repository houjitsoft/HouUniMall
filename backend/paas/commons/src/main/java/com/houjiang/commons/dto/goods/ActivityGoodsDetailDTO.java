package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.activity.AppDiscountGoodsQueryResultVO;
import com.houjiang.commons.vo.activity.AppSeckillGoodsQueryResultVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单使用 不能修改
 *
 * @author Amos
 */
@Data
public class ActivityGoodsDetailDTO {

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
     * 活动id
     */
    private Integer activityId;
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
    private List<ManjianzengzheGoodsRuleQueryDTO> ruleList;

    /**
     * 满减赠折活动Id
     */
    private Integer promotionId;

    /**
     * 满减赠折价格
     */
    private BigDecimal mjzzPrice;

    /**
     * 活动名称
     */
    private String discountTitle;

    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date activityStartTime;

    /**
     * 折扣
     */
    private BigDecimal discountAmount;

    /**
     * 活动限购天数
     */
    private Integer discountUpperTime;
    /**
     * 1开始 2未开始
     */
    private Integer activityStartStatus;
    /**
     * 当前商品在购物车的数量
     */
    private Integer currentCartQuantity;
    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;
    /**
     * 赠品skuId
     */
    private Integer goodsId;

    /**
     * 当前规格活动库存
     */
    private Integer goodsStock;

    /**
     * 活动商品总库存
     */
    private Integer commonStock;

    /**
     * 秒杀活动
     */
    private AppSeckillGoodsQueryResultVO seckillGoods;

    /**
     * 限制折扣活动
     */
    private AppDiscountGoodsQueryResultVO discountGoods;

}
