package com.houjiang.goods.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.group.Update;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GiftGoodsDTO extends PageHelper {

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品spu名称
     */
    private String commonName;

    /**
     * 商品sku名称
     */
    private String goodsName;

    /**
     * 商家编号
     */
    private String commonCode;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 商品主图
     */
    private String commonImage;

    /**
     * 商品价格
     */
    private String commonPrice;

    /**
     * 库存
     */
    private Integer commonStock;

    /**
     * 上架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonSellTime;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 商品销量
     */
    private Integer commonSalenum;

    /**
     * 店铺Id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 是否是礼包商品
     */
    private Integer isGiftProducts;

    /**
     * 固定奖励
     */
    private BigDecimal fixedRewards;

    /**
     * 商品id
     */
    @NotEmpty(message = "商品id不能为空", groups = {Update.class})
    private List<Integer> commonIdList;

}
