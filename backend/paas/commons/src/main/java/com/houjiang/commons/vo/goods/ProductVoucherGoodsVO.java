package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ProductVoucherGoodsVO
 * @Description
 * @Author Amos
 * @Date 2024/9/6 16:03
 * @Version 1.0
 * @author Amos
 */
@Data
public class ProductVoucherGoodsVO {
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 商品图片
     */
    private String commonImage;
    /**
     * 商品广告词
     */
    private String commonPromotionTips;
    /**
     * 商品库存
     */
    private Integer commonStock;
    /**
     * 商品是否参加促销0不参加,2秒杀,3限时折扣,4拼团,5满减赠6满减折
     */
    private Integer commonIsXian;
    /**
     * 商品价格
     */
    private BigDecimal commonPrice;
    /**
     * 规格价格
     */
    private BigDecimal goodsPrice;
    /**
     * 商品 id
     */
    private Integer goodsId;
    /**
     * 商品规格
     */
    private String specParam;
    /**
     * 规格参数商品
     */
    private List<SpecParamJsonBeanVO> specParamList;
    /**
     * 规格参数商品
     */
    private List<GoodBaseSpecValuesJsonBeanVO> specValueList;
    /**
     * 商品库存
     */
    private Integer goodsStock;
    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除
     */
    private Integer commonState;
    /**
     * 虚拟商品 0 是 1 否 3 称重商品
     * */
    private Integer virtualGoods;
}
