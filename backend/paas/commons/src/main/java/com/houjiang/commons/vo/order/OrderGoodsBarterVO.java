package com.houjiang.commons.vo.order;

import com.houjiang.commons.dto.goods.SpecDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class OrderGoodsBarterVO {

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 货品名称
     */
    private String goodsName;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 商品规格值
     */
    private String goodsSpecValue;

    /**
     * 货品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 库存
     */
    private Integer goodsStock;

    /**
     * 库存预警值
     */
    private Integer goodsAlarm;

    /**
     * 商家编号货号
     */
    private String goodsCode;

    /**
     * 商品主图
     */
    private String goodsImage;

    /**
     * 1-上架 2-下架
     */
    private Integer goodsIsShelves;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;

    /**
     * 规格名称
     */
    private String specName;

    /**
     * 规格值
     */
    private String specValue;

    /**
     * 起订量
     */
    private Integer moq;

    /**
     * 含税价格
     */
    private Double taxInclusiveAmount;

    /**
     * 税率
     */
    private Double taxRate;

    /**
     * 单位
     */
    private String dw;

    /**
     * 是否可询价
     */
    private Integer isEnquiry;

    /**
     * 仓库商品库存
     */
    private Integer storageGoodsStock;

    /**
     * 规格参数商品
     */
    private List<SpecDTO> specParamList;

    /**
     * 会员价
     */
    private BigDecimal memberAmount;

    /**
     * 含税单价
     */
    private BigDecimal goodsTaxPrice;

    /**
     * 未税单价
     */
    private BigDecimal goodsWeiPrice;

    /**
     * 供应商商品id
     */
    private Integer supplierGoodsId;

    /**
     * 商品来源:0.商家自营商品,1:代发平台供应链商品,2:怡亚通
     */
    private Integer supplierSource;

    /**
     * 0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品
     */
    private Integer dealerGoodsFlag;
}
