package com.houjiang.commons.vo.goods;

import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 基础详情
 * @author Amos
 */
@Data
public class BaseDetailVO {

    /**
     * 商品公共表id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品id
     */
    private String goodsIdStr;
    /**
     * 供应商商品id
     */
    private Integer supplierGoodsId;

    /**
     * 商品名
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
     * 品牌id
     */
    private Integer brandId;

    /**
     * 品牌名
     */
    private String brandName;

    /**
     * 品牌图片
     */
    private String brandPic;

    /**
     * 规格
     */
    private String goodsSpec;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 市场价
     */
    private BigDecimal goodsMarketPrice;

    /**
     * 库存
     */
    private Integer goodsStock;
    /**
     * 是否有库存
     */
    private Boolean goodsStockBool;

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
    private Boolean goodsIsRecommend;

    /**
     * 商品点击数量
     */
    private Integer goodsClick;

    /**
     * 已售
     */
    private Integer goodsSalenum;

    /**
     * 收藏数量
     */
    private Integer goodsCollect;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 好评星级
     */
    private Byte goodsEvaluationGoodStar;

    /**
     * 评价数
     */
    private Integer goodsEvaluationCount;

    /**
     * 最大购买数量
     */
    private Integer goodsMaxSale;

    /**
     * 最小购买数量
     */
    private Integer goodsMinSale;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 是否上架1:上架
     */
    private Integer goodsIsShelves;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 用于显示活动商品原价
     */
    private BigDecimal commodityPrice;

    /**
     * 活动价
     */
    private BigDecimal activityPrice;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架
     */
    private Integer commonState;

    /**
     * 是否是门店商品 1是 2否
     */
    private Integer commonIsShopTake;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer activityStatus;

    /**
     * 活动详情
     */
    private ActivityGoodsDetailDTO activityGoodsDetail;

    /**
     * 增加小宝价格
     */
    private BigDecimal goodsBabyPrice;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 小宝价格
     */
    private BigDecimal commonBabyPrice;

    /**
     * 每人限购 0代表不限购1不限购
     */
    private Integer commonLimit;

    /**
     * 是否为聚宝1是0不是
     */
    private Integer commonIsTreasure;

    /**
     * 上传类型1:图片2视频
     */
    private Integer type;

    /**
     * 视频
     */
    private String commonVideo;

    /**
     * 规格值
     */
    private String specValue;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer commonIsXian;

    /**
     * 积分商品所需积分
     */
    private Integer needIntegral;

    /**
     * 满减赠折活动内容
     */
    private String promotionContent;

    /**
     * 满减赠折活动类型
     */
    private String promotionType;

    /**
     * 含税价格
     */
    private BigDecimal taxInclusiveAmount;

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
     * 含税价格
     */
    private BigDecimal memberAmount;

    /**
     * 现货or订货
     */
    private Integer deliveryDate;

    /**
     * 订货日
     */
    private Integer goodsOrderDate;

    /**
     * 会员折
     */
    private Double hyZheKou;

    /**
     * 是否是询价商品
     */
    private Integer isEnquiry;

    /**
     * 图片
     */
    private List<ImageDetailVO> goodsImages;

    /**
     * 状态
     */
    private Integer zhuangTai;

    /**
     * 操作类型 1一键上架  2立即购买 3申请经销商 4已经销
     */
    private Integer operateType;

    /**
     * 1非聚宝商品2聚宝商品
     */
    private Integer goodsType;

    /**
     * 规格值集合
     */
    private List<GoodBaseSpecValuesVO> specValueList;

    /**
     * 商品图片
     */
    private String commonImage;

    /**
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;

    /**
     * 商品评论
     */
    private Integer commonEvaluate;

    /**
     * 商品销量
     */
    private Integer commonSalenum;

    /**
     * 是否加入对比
     */
    private Integer isAddContrast;

    /**
     * 商品列表优先展示
     */
    private Integer goodsListFirstShow;

    /**
     * 等级id
     */
    public Integer levelId;
    /**
     * 是否经销商品  0普通商品  1 一键代发(商家一键代发供应商商品)  2商家购买供应商商品 3 一键铺货
     */
    private Integer dealerGoodsFlag;
    /**
     * 利润率
     */
    private BigDecimal profitMargin;
    /**
     * 是否 已铺货
     */
    private Boolean productListingStart = false;
    /**
     * 是否已代发
     */
    private Boolean dropShippingStart = false;
    /**
     * 商品含税金额 未算经销折扣
     */
    private BigDecimal taxInclusiveNoDiscount;
    /**
     * 经销折扣金额
     */
    private BigDecimal dealerDiscountAmout;

    /**
     * 是否参加千客千价活动
     */
    private Integer isThousandsGuestsPrice;

    /**
     * 商品来源:0.商家自营商品,1:代发平台供应链商品,2:怡亚通 3 京东VOP
     */
    private Integer supplierSource;

    /**
     * 建议零售价
     */
    private BigDecimal msrp;
    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;
}
