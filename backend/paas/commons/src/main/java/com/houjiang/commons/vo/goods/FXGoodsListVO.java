package com.houjiang.commons.vo.goods;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 * @date 2021-06-0413:50
 * @Description: 分销商品列表
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class FXGoodsListVO extends PageHelper {
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
    private  Integer goodsStock;
    /**
     * 推广佣金比例
     */
    private  String oneTgRate;
    /**
     * 分销佣金比例
     */
    private  String fxRate;
    /**
     * 商品图片
     */
    private  String goodsImage;
    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;
    /**
     * 一级推广佣金
     */
    private BigDecimal oneTgCommission;
    /**
     * 二级推广佣金
     */
    private BigDecimal twoTgCommission;
    /**
     * 分享佣金
     */
    private  BigDecimal fxCommission;

    /**
     * 总佣金
     */
    private BigDecimal totalCommission;

    private Integer commonId;
    /**
     * 商品是否参加促销0不参加,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;

    /**
     * 1 默认排序 2销量排序  3 价格排序
     */
    private Integer orderType;

    /**
     // 1降序，2升序
/**
     *
     */
    private Integer saleNum;

    /**
     * 价格排序 1降序，2升序
     */
    private Integer priceOrder;

    private Integer catId;

    private List<Integer> catIds;

    private String userId;
}
