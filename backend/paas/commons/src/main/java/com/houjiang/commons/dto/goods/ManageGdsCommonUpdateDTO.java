package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import com.houjiang.commons.vo.goods.SpecParamJsonBeanDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName ManageGdsCommonUpdateDTO
 * @Description 商品公共信息修改
 * @Author Amos
 * @Date 2023/6/27 10:11
 * @Version 1.0
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ManageGdsCommonUpdateDTO extends UserTokenBeanDTO {
    /**
     * 自增
     */
    private Integer commonId;
    private String catIds;
    /**
     * 商品名称
     */
    private String commonName;
    /**
     * 副标题，即商品广告词
     */
    private String commonPromotionTips;
    /**
     * 分类id，前台传入
     */
    private Integer catId;
    /**
     * 分类名称，前台传入
     */
    private String catName;
    /**
     * 前台传入
     */
    private String shopId;
    /**
     * 前台传入
     */
    private String shopName;
    /**
     * 归属店铺分类id
     */
    private String shopCatId;
    /**
     * 店铺是否自营，前台传入
     */
    private Integer shopSelfSupport;
    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料 0:关闭',前台传入
     */
    private Integer shopStatus;
    /**
     * 商品属性
     */
    private String commonProperty;
    /**
     * 规格名称
     */
    private String commonSpecName;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date commonAddTime;
    /**
     * 上架时间,发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date commonSellTimeStr;
    /**
     * 上架时间,发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date commonSellTime;
    private Integer commonVerify;

    // 其他信息
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

    // 物流信息
    /**
     * 固定运费 非零：transport_type_id 运费类型
     */
    private Integer transportTypeId;
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
     * 商品所在地 json,目前为空
     */
    private String commonLocation;

    // 店铺信息
    /**
     * 所在地,从店铺中同步，冗余检索使用
     */
    private Integer districtId;
    /**
     * 商家编号
     */
    private String commonCode;
    /**
     * 平台货号
     */
    private String commonPlatformCode;

    // 获取第一个规格商品相关参数
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
     * 每人限购 0 代表不限购
     */
    private Integer commonLimit;
    /**
     * 商品库存警报线
     */
    private Integer commonAlarm;
    /**
     * 商品重量
     */
    private BigDecimal commonCubage;

    // 封装商品
    /**
     * 不同规格商品
     */
    private List<ManageGdsBaseAddParamvDTO> gdsBases;
    /**
     * 商品详情
     */
    private GdbMerchantGoodsCommonDetailDTO goodsCommonDetail;
    //private List<GdbMerchantGoodsImages> goodsImages; // 图片
    /**
     * 图片
     */
    private List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages;

    // 默认值
    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
     * 规格：需求 规格跟着商品走 ，新增加字段
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
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;

    /**
     * 虚拟商品有效期
     */
    private String periodOfValidity;
    /**
     * 虚拟商品有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date periodOfValiditys;
    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;

    /**
     * 是否支持门店自提 1是 2否
     */
    private Integer commonIsShopTake;

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
     * 1开启爆品  2不是爆品
     */
    private Integer hotState;

    /**
     * 建议最低零售价
     */
    private BigDecimal goodsRecommendedMinPrice;
    /**
     * 建议最高零售价
     */
    private BigDecimal goodsRecommendedMaxPrice;

    private Integer buteId;

    private List<AttributeParamDTO> attribuParam;

    private Integer attribuId;

    private Integer deliveryTime;

    /**
     * 售后服务JSON
     */
    private String afterService;

    /**
     * 售后服务数组
     */
    private List<String> afterServiceList;

    //称重商品参数
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
     * 库存
     */
    private Integer goodsStock;

    /**
     * 预警库存
     * }
     */
    private Integer goodsAlarm;

    /**
     * 商品类目属性集合
     */
    private List<GoodsCategoryAttributesDTO> categoryAttributesList;
}
