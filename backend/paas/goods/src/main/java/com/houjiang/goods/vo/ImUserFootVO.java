package com.houjiang.goods.vo;


import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单使用 不可修改
 *
 * @author Amos
 */
@Data
public class ImUserFootVO {

    /**
     * 商品goods_id
     */
    private String goodsId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 商品价格
     */
    private BigDecimal commonPrice;
    /**
     * 商品主图
     */
    private String commonImage;

}
