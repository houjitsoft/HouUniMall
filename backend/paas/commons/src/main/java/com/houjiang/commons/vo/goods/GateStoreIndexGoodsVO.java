package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName GateStoreIndexGoodsVO
 * @Description GateStoreIndexGoodsVO
 * @Author Amos
 * @Date 2024/5/9 9:23
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateStoreIndexGoodsVO {
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 商品图片
     */
    private String commonImage;
    /**
     * 商品价格
     */
    private BigDecimal commonPrice;
    /**
     * 商品规格 goods_common
     */
    private String specParam;
    /**
     * 规格参数商品 goods_common
     */
    private List<SpecParamJsonBeanVO> specParamList;
    /**
     * 商品规格 goodsBase
     */
    private String specValue;
    /**
     * 门店商品库存
     */
    private Integer shopGateGoodsStock;
}
