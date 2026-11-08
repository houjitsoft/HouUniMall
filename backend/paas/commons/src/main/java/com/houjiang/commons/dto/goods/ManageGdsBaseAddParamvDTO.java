package com.houjiang.commons.dto.goods;

import com.houjiang.commons.vo.goods.GdbMerchantGoodsImagesVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ManageGdsBaseAddParamvDTO
 * @Description 商家后台商品添加
 * @Author Amos
 * @Date 2023/6/26 17:18
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsBaseAddParamvDTO {
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 促销提示
     */
    private String goodsPromotionTips;
    /**
     * 分类id
     */
    private Integer catId;
    /**
     * 商品规格-JSON存储
     */
    private String goodsSpec;

    /**
     * 商品规格值
     */
    private String goodsSpecValue;
    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;
    /**
     * 市场价
     */
    private BigDecimal goodsMarketPrice;
    /**
     * 商品库存
     */
    private Integer goodsStock;
    /**
     *  门店库存总和最大值
     */
    private Integer goodsShopGateStock;
    /**
     * 库存预警值
     */
    private Integer goodsAlarm;
    /**
     * 商家编号货号
     */
    private String goodsCode;
    /**
     * 商品二维码
     */
    private String goodsBarcode;
    /**
     * 商品推荐 1是，0否 默认0'
     */
    private Integer goodsIsRecommend;
    /**
     * 商品主图
     */
    private String goodsImage;
    /**
     * 单人最大购买数量
     */
    private Integer goodsMaxSale;
    /**
     * 1-上架 2-下架
     */
    private Integer goodsIsShelves;
    /**
     * 品牌id
     */
    private Integer brandId;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;

    /**
     * 商品价格
     */
    private BigDecimal goodsBabyPrice;

    /**
     * 最小限购数
     */
    private Integer goodsMinSale;

    /**
     * 规格类型 多规格需求新增
     */
    private String specId;

    /**
     * 类型id
     */
    private String typeId;
    /**
     * 类型id
     */
    private List<GdbMerchantGoodsImagesVO> img;

    /**
     * 规格
     */
    private List<GoodBaseSpecValuesJsonBeanDTO> specValues;
    /**
     * 是否使用(0:未使用;1:已使用)
     */
    private Integer isUse;
    /**
     * 商家编号
     */
    private String merchantCode;
    /**
     * 商品条形码
     */
    private String commodityBarTypeCode;
    /**
     * 规格
     */
    private String specValue;

    /**
     * 体积
     */
    private BigDecimal goodsVolume;
    /**
     * 成本价
     */
    private BigDecimal goodsCostPrice;
    /**
     * 直播卖点
     */
    private String sellingPoint;
    /**
     * 是否寄样
     */
    private Integer goodsSendBack;
    /**
     * 佣金
     */
    private BigDecimal commission;

    /**
     * 经销商类型
     */
    private Integer dealerType;

    /**
     * 爆品指导价
     */
    private BigDecimal hotPrice;

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
     * 起订量
     */
    private Integer moq;		

    /**
     * 是否可询价
     */
    private Boolean isEnquiry;
    /**
     * 是否询价商品
     */
    private Integer enquiry;

    public ManageGdsBaseAddParamvDTO(Integer goodsId, Integer commonId, Integer goodsStock, Integer goodsIsShelves,Integer dealerType) {
        this.goodsId = goodsId;
        this.commonId = commonId;
        this.goodsStock = goodsStock;
        this.goodsIsShelves = goodsIsShelves;
        this.dealerType = dealerType;
    }
    public ManageGdsBaseAddParamvDTO() {

    }


    /**
     * plu码
     */
    private String commodityPluCode;

    /**
     * 店铺一级分类
     */
    private String shopGateFirstType;

    private String shopGateType;
}
