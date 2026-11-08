package com.houjiang.commons.vo.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 * @date 2021-06-0413:50
 * @Description: 分销商品列表
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistributionGoodsListVO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 销售数量
     */
    private Integer goodsSalenum;

    /**
     * 库存
     */
    private Integer goodsStock;

    /**
     * 推广佣金比例
     */
    private String tgRate;

    /**
     * 分销佣金比例
     */
    private String fxRate;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 推广佣金
     */
    private BigDecimal tgCommission;

    /**
     * 分享佣金
     */
    private BigDecimal fxCommission;

    /**
     * 总佣金
     */
    private BigDecimal totalCommission;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品是否参加促销0不参加,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;

}
