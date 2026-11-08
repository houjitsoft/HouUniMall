package com.houjiang.goods.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.BaseEntityDTO;
import com.houjiang.commons.dto.goods.ManageGdsBaseAddPublishDTO;
import com.houjiang.commons.dto.goods.MerchantShopGoodsCatDTO;
import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import com.houjiang.commons.vo.goods.SpecParamJsonBeanDTO;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商品公共信息添加
 *
 * @author Amos
 */
@ToString
@Data
public class ManageGdsIntegralCommonPublishDTO extends BaseEntityDTO implements Serializable {

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 副标题，即商品广告词
     */
    private String commonPromotionTips;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 分类id
     */
    private String catIds;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 归属店铺分类id
     */
    private String shopCatId;

    /**
     * 店铺是否自营
     */
    private Integer shopSelfSupport;

    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料 0:关闭
     */
    private Integer shopStatus;

    /**
     * 规格名称
     */
    private String commonSpecName;

    /**
     * 规格值
     */
    private String commonSpecValue;

    /**
     * 类型id,用于获取属性及规格
     */
    private Integer typeId;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 商品添加时间
     */
    private String commonAddTimeStr;

    /**
     * 上架时间,发布时间
     */
    private String commonSellTimeStr;

    /**
     * 商品状态 0下架（放入仓库），1正常，10违规（禁售） 4 定时上架
     */
    private Integer commonState;

    /**
     * 是否允许退货 1 是，2否
     */
    private Integer commonIsReturn;

    /**
     * 商品推荐 1推荐 2 不推荐
     */
    private Integer commonIsRecommend;

    /**
     * 售后服务
     */
    private String commonService;

    /**
     * 包装清单
     */
    private String commonPackingList;

    /**
     * 顶部关联板式
     */
    private Integer commonFormatidTop;

    /**
     * 底部关联板式
     */
    private Integer commonFormatidBottom;

    /**
     * 固定运费 非零：transport_type_id 运费类型
     */
    private Integer transportTypeId;

    /**
     * 售卖区域id,和hou_transport_area中的id对应
     */
    private Integer transportAreaId;

    /**
     * 运费模板
     */
    private Integer transportTemplate;

    /**
     * 商家编号
     */
    private String commonCode;

    /**
     * 平台货号
     */
    private String commonPlatformCode;

    /**
     * 所在地,从店铺中同步，冗余检索使用
     */
    private Integer districtId;

    /**
     * 第一个商品的规格及规格名称
     */
    private String goodsId;

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
     * 商品主图
     */
    private String commonImage;

    /**
     * 商品库存
     */
    private Integer commonStock;

    /**
     * 商品库存最大值
     */
    private Integer commonShopGateStock;

    /**
     * 每人限购 0 代表不限购
     */
    private Short commonLimit;

    /**
     * 商品库存警报线
     */
    private Integer commonAlarm;

    /**
     * 商品重量
     */
    private BigDecimal commonCubage;

    /**
     * 不同规格商品
     */
    private List<ManageGdsBaseAddPublishDTO> gdsBases;

    /**
     * 商品详情
     */
    private GoodsCommonDetailDTO goodsCommonDetail;

    /**
     * 图片
     */
    private List<GoodsImagesDTO> goodsImages;

    /**
     * 2021-01-7 规格：需求 规格跟着商品走 ，新增加字段
     */
    private List<SpecParamJsonBeanDTO> specParam;

    /**
     * 将specParam数组转化成 json字符串
     */
    private String specParamJson;

    /**
     * 直播卖点
     */
    private String sellingPoint;

    /**
     * 是否寄样
     */
    private Integer goodsSendBack;

    /**
     * 商品添加时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonAddTime;

    /**
     * 上架时间,发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonSellTime;

    /**
     * 提交审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitVerifyTime;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 商品属性
     */
    private String commonProperty;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonVerifyTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonUpdateTime;

    /**
     * 更新人
     */
    private String commonUpdateBy;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 小宝价格
     */
    private BigDecimal commonBabyPrice;

    /**
     * 限购次数
     */
    private Integer restrictionTimes;

    /**
     * 限购小时数
     */
    private Long restrictionHours;

    /**
     * 主图类型 1图片2视频
     */
    private Integer type = 1;

    /**
     * 视频
     */
    private String commonVideo;

    /**
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;

    /**
     * 虚拟商品有效期
     */
    private String periodOfValidity;

    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;

    /**
     * 是否支持门店自提 1是 2否
     */
    private Integer commonIsShopTake;

    /**
     * 建议最低零售价
     */
    private BigDecimal goodsRecommendedMinPrice;

    /**
     * 建议最高零售价
     */
    private BigDecimal goodsRecommendedMaxPrice;

    /**
     * 1普通商品 2供应商商品
     */
    private Integer goodsType;

    /**
     * 门店字段
     */
    private String goodsAssignmentStore;

    /**
     * 商品发布到哪些门店
     */
    private List<Integer> storeId;

    /**
     * 经销商品配置
     */
    private BaseGoodsConfigVO manageGdsGoodsConfig;

    /**
     * 商品店铺分类
     */
    private List<MerchantShopGoodsCatDTO> shopGoodsCatList;

    /**
     * 商品分类id集合
     */
    private List<Integer> goodsCatIdList;

    /**
     * 1开启爆品  2不开启
     */
    private Integer hotState;

    /**
     * 单位
     */
    private String dw;

    /**
     * 现货 ? 订货
     */
    private Integer deliveryDate;

    /**
     * 订货预计发货时间
     */
    private Integer goodsOrderDate;

    /**
     * 自定义信息JSON
     */
    private String customInformation;

    /**
     * 自定义信息数组
     */
    private List<GoodsCustomInformationDTO> customInformationList;

    /**
     * 售后服务JSON
     */
    private String afterService;

    /**
     * 售后服务数组
     */
    private List<String> afterServiceList;

    /**
     * 是否是积分商品
     */
    private Integer integralGoods;

}
