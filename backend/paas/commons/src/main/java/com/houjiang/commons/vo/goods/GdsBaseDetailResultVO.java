package com.houjiang.commons.vo.goods;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.goods.ActivityGoodsDetailDTO;
import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * @ClassName GdsDetailPageResultVO
 * @Description GdsBaseDetailResultVO
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GdsBaseDetailResultVO {

    private Integer commonId;
    /**
     * 是否为plus商品
     */
    private Integer plusGoods;
    /**
     * 是否plus会员 0过期 1是 2否
     */
    private Integer plusMember;
    /**
     * 会员折扣-plus折扣
     */
    private BigDecimal plusDiscount;

    private Integer goodsId;

    private String goodsName;
    /**
     * 促销提示
     */
    private String goodsPromotionTips;

    private Integer catId;

    private Integer brandId;

    private String goodsSpec;

    private BigDecimal goodsPrice;

    private BigDecimal goodsMarketPrice;

    private Integer goodsStock;

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
     * 是否推荐
     */
    private Boolean goodsIsRecommend;
    /**
     * 商品点击数量
     */
    private Integer goodsClick;
    /**
     * 销售数量
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
     * 单人最大购买数量
     */
    private Integer goodsMaxSale;
    /**
     * 最小限购数据
     */
    private Integer goodsMinSale;

    private Integer shopId;

    private String shopName;
    /**
     * 1-上架 2-下架
     */
    private Integer goodsIsShelves;

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
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;
    /**
     * 是否支持门店自提 1是 2否
     */
    private Integer commonIsShopTake;
    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer activityStatus;
    /**
     * 活动详情
     */
    private ActivityGoodsDetailDTO activityGoodsDetailDTO;
    /**
     * 活动详情
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
     * 当前商品在购物车的数量
     */
    private Integer currentCartQuantity;
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
     * sku图片
     */
    private List<GdbMerchantGoodsImagesVO> goodsImages;
    /**
     * 操作类型 1一键上架  2立即购买 3申请经销商 4已经销
     */
    private Integer operateType;

    private List<GoodBaseSpecValuesJsonBeanVO> specValueList;
    /**
     * 1普通商品  2供应商商品
     */
    private Integer goodsType;
    /**
     * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    private Integer dealerGoodsFlag;
    /**
     * 发货时效
     */
    private Integer deliveryTime;

    /**
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;

    /** 活动详情 */
    private ActivityGoodsDetailDTO activityGoodsDetail;


    /**
     * 积分支付比例
     */
    private Double pointsRate;




    /**
     * 现金价格
     */
    private BigDecimal cashPrice;


    public void setOperateType2(BaseGoodsConfigVO manageGdsGoodsConfig) {
        if (manageGdsGoodsConfig.getSellCheck().intValue() == 1) {
            this.operateType = CommonType.supplierGoodsStatus.APPLY.getCode();
        } else {
            this.operateType = manageGdsGoodsConfig.getSellDeliver();
        }
    }


}
