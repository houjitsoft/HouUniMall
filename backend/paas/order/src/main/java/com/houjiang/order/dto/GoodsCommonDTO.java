package com.houjiang.order.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description:
 * @Author Amos
 * @CreateDate: 2025/7/4 15:08
 * @author Amos
 */
@Data
public class GoodsCommonDTO implements Serializable {

    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 商品广告词
     */
    private String commonPromotionTips;
    /**
     * 商品分类
     */
    private Integer catId;
    /**
     * 商品分类
     */
    private String catName;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺商品分类id  -- json
     */
    private String shopGoodsCatId;
    /**
     * 商品goods_id
     */
    private String goodsId;
    /**
     * 是否自营商品 1 不是 2 是
     */
    private Integer shopSelfSupport;
    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭
     */
    private Integer shopStatus;
    /**
     * 规格值
     */
    private String commonSpecValue;
    /**
     * 品牌id
     */
    private Integer brandId;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * 商品主图
     */
    private String commonImage;
    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架,3商品删除
     */
    private Integer commonState;
    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;
    /**
     * 商品添加时间
     */
    private Date commonAddTime;
    /**
     * 上架时间
     */
    private Date commonSellTime;
    /**
     * 商品价格
     */
    private BigDecimal commonPrice;

    /**
     * 市场价
     */
    private BigDecimal commonMarketPrice;

    /**
     * 成本价
     */
    private BigDecimal commonCostPrice;

    /**
     * 商品价格
     * 市场价
     * 成本价
     * 商品库存
     * 商品库存
     */
    private Integer commonStock;
    /**
     * 商品库存警报线
     */
    private Integer commonAlarm;
    /**
     * 商品重量
     */
    private BigDecimal commonCubage;

    /**
     * 商品收藏量
     */
    private Integer commonCollect;
    /**
     * 商品评论数
     */
    private Integer commonEvaluate;
    /**
     * 商品销量
     */
    private Integer commonSalenum;
    /**
     * 商品推荐
     */
    private Integer commonIsRecommend;
    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;
    /**
     * 所在地,从店铺中同步，冗余检索使用
     */
    private Integer districtId;
    /**
     * 商品来源-供应商店铺id
     */
    private Integer supplyShopId;
    /**
     * 分销原产品
     */
    private Integer commonParentId;
    /**
     * 建议最低零售价
     */
    private BigDecimal goodsRecommendedMinPrice;

    /**
     * 建议最高零售价
     */
    private BigDecimal goodsRecommendedMaxPrice;

    /**
     * 是否为分销商品 0-自有商品
     */
    private Integer productDistributorFlag;
    /**
     * 售卖区域id,和hou_transport_area中的id对应
     */
    private Integer transportAreaId;
    /**
     * 国际售卖区域
     */
    private Integer internationalTransportArea;
    /**
     * 运费模板
     */
    private Integer transportTemplate;
    /**
     * 国际运费模板
     */
    private Integer internationalTransportTemplate;
    /**
     * 更新人
     */
    @TableField(select = false)
    private String commonUpdateBy;
    /**
     * 更新时间
     */
    private Date commonUpdateTime;
    /**
     * 锁定 1 锁定 2 不锁定
     */
    private Integer commonIsLocked;
    /**
     * 限购次数
     */
    private Integer restrictionTimes;
    /**
     * 限购天数
     */
    private Integer restrictionHours;
    /**
     * 操作人
     */
    private String orderOperator;
    /**
     * 操作时间
     */
    private Date orderOperatorTime;
    /**
     * 商品点击数量
     */
    private Integer commonClick;
    /**
     * 主图类型 1图片2视频
     */
    private Integer type;
    /**
     * 视频
     */
    private String commonVideo;
    /**
     * 商品是否屏蔽：1-否；2-是
     */
    private Integer commonIsScreen;
    /**
     * 提交审核时间
     */
    private Date commonVerifyTime;
    /**
     * 提交审核时间
     */
    private Date submitVerifyTime;
    /**
     * 商品规格
     */
    private String specParam;
    /**
     * 导入时保存图片名字
     */
    private String importNumber;
    /**
     * 创建者
     */
    private String createUser;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 是否删除0:否;1:是
     */
    private Integer isDelete;
    /**
     * 时间戳
     */
    private Date ts;
    /**
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;
    /**
     * 虚拟商品有效期
     */
    private Date periodOfValidity;
    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;
    /**
     * 积分商品 0是 1否
     */
    private Integer integralGoods;
    /**
     * 是否是门店自提商品 1是 2否
     */
    private Integer commonIsShopTake;
    /**
     * 1普通商品   2供应商商品  3 品牌店铺商品
     */
    private Integer goodsType;
    /**
     * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    private Integer dealerGoodsFlag;
    /**
     * 是否是plus商品 0 不是 1 是
     */
    private Integer plusGoods;
    /**
     * plus商品创建时间
     */
    private Date plusTime;

    /**
     * plu
     */
    private String plu;
    /**
     * 商品单位
     */
    private String unit;
    /**
     * 货架号
     */
    private String shelf;
    /**
     * 产地
     */
    private String region;
    /**
     * 备注
     */
    private String remark;
    /**
     * 商品图片库
     */
    private String imageList;

    /**
     * 是否是礼包商品
     */
    private Integer isGiftProducts;
    /**
     * 固定奖励
     */
    private BigDecimal fixedRewards;

    /**
     * 供应商来源:2:怡亚通 3 京东VOP
     */
    private Integer supplierSource;

    /**
     * 积分支付比例
     */
    private Double pointsRate;

}
