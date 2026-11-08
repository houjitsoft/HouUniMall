package com.houjiang.commons.vo.shop;

import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import com.houjiang.commons.enums.GoodsOperateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 不能随意修改
 *
 * @author Amos
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ListQueryResultVO {

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 商品id1
     */
    private Integer goodsIdOne;

    /**
     * 商品公共名称
     */
    private String commonName;

    /**
     * 商品广告词
     */
    private String commonPromotionTips;

    /**
     * 商品价格
     */
    private BigDecimal commonPrice;

    /**
     * 商品销量
     */
    private Integer commonSalenum;

    /**
     * 市场价
     */
    private BigDecimal commonMarketPrice;

    /**
     * 商品主图
     */
    private String commonImage;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 评论数
     */
    private Integer commonEvaluate;

    /**
     * 收藏量
     */
    private Integer commonCollect;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品分类
     */
    private String catName;

    /**
     * 商品价格
     * 市场价
     * 成本价
     * 商品库存
     * 商品库存
     */
    private Integer commonStock;

    /**
     * 已售
     */
    private Integer goodsSalenum;

    /**
     * 拼团价格
     */
    private BigDecimal price;

    /**
     * 拼团单独购买价格
     */
    private BigDecimal priceOne;


    /**
     * 是否询价商品
     */
    private Integer isEnquiry;

    /**
     * 库存
     */
    private Integer goodsStock;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 1-上架 2-下架 0店铺关闭手动下架
     */
    private Integer goodsIsShelves;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 小宝价格
     */
    private BigDecimal commonBabyPrice;

    /**
     * 好评数
     */
    private Integer qty;

    /**
     * 平均发货时间
     */
    private String avgShippingTime;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer activityStatus;

    /**
     * 活动详情
     */
    private ActivityGoodsDetailDTO activityGoodsDetail;

    /**
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;

    /**
     * 虚拟商品有效期
     */
    private String periodOfValidity;

    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;

    /**
     * 积分商品 0 是 1 否
     */
    private Integer integralGoods;

    /**
     * 所需的积分
     */
    private Integer needIntegral;

    /**
     * 是否需要经销审核 1是 2否
     */
    private Integer sellCheck;

    /**
     * 1一键上架 2立即购买
     */
    private Integer sellDeliver;

    /**
     * 操作类型 1一键上架  2立即购买 3申请经销商 4已经销
     */
    private Integer operateType;

    /**
     * 上架id
     */
    private Integer putawayId;


    public void setOperateType(ListQueryResultVO goods) {
        if (goods.getSellCheck().intValue() == 1) {
            this.operateType = GoodsOperateEnum.APPLY.getType();
        } else {
            this.operateType = goods.getSellDeliver();
        }
    }

    public void setOperateType(Integer operateType) {
        this.operateType = operateType;
    }
}
