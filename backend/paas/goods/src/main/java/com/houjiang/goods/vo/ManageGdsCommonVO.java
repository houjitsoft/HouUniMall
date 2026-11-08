package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.goods.ManageSpecNameVO;
import com.houjiang.commons.vo.goods.ManageTransportAreaVO;
import com.houjiang.commons.vo.goods.ManageTransportTemplateVO;
import com.houjiang.commons.vo.goods.SpecParamJsonBeanVO;
import com.houjiang.goods.entity.GoodsCategoryAttributesEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName ManageGdsCommonVO
 * @Author Amos
 * @Date 2023/6/27 14:33
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsCommonVO {
    private String catIds;

    /**
     * 自增
     */
    private Integer commonId;


    /**
     * 发货时效
     */
    private Integer deliveryTime;

    /**
     * 商品名称
     */
    private String commonName;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

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
    private Integer shopId;

    /**
     * 前台传入
     */
    private String shopName;

    /**
     * 归属店铺分类id
     */
    private String shopCatId;

    /**
     * 自定义分类名称
     */
    private List<ManageGoodsSelfCatVO> shopSelfCat;

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
    private List<ManageSpecNameVO> commonSpecNameList;
    /**
     * 规格值
     */
    private String commonSpecValue;
    /**
     * 规格值集合
     */
    private List<ManageSpecValueResultEditVO> commonSpecValueList;

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
    private Date commonAddTime;
    /**
     * 上架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonSellTime;

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
     * 规格名称
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

    private ManageTransportTemplateVO transportTemplates;
    /**
     * 国内售卖区域
     */
    private ManageTransportAreaVO transportAreas;
    /**
     * 国际售卖区域
     */
    private ManageTransportAreaVO internationalTransportAreas;

    private ManageTransportTemplateVO internationalTransportTemplates;

    // 获取第一个规格商品相关参数
    /**
     * 第一个商品的规格及规格名称
     */
    private Integer goodsId;
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
     * 商品图片
     */
    private List<String> commonImages;
    /**
     * 商品库存
     */
    private Integer commonStock;
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
    private Integer type;

    /**
     * 视频
     */
    private String commonVideo;
    /**
     * 商品规格
     */
    private String specParam;

    private List<SpecParamJsonBeanVO> specParamList;
    /**
     * 直播卖点
     */
    private String sellingPoint;
    /**
     * 是否寄样
     */
    private Integer goodsSendBack;

    /**
     * 1普通商品   2供应商商品
     */
    private Integer goodsType;
    /**
     * 建议最高零售价
     */
    private BigDecimal goodsRecommendedMaxPrice;

    /**
     * 建议最低零售价
     */
    private BigDecimal goodsRecommendedMinPrice;

    /**
     * 0正常  1只能修改商品内容 2不可修改
     */
    private Integer commonDistributorFlag;
    /**
     * 是否是虚拟商品 0是 1否
     */
    private Integer virtualGoods;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date periodOfValidity;

    /**
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;
    /**
     * 1爆品  2非爆品
     */
    private Integer hotState;

    /**
     * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    private Integer dealerGoodsFlag;

    /**
     * 是否是门店商品 1是 2否
     */
    private Integer commonIsShopTake;

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
     * 仓库商品库存
     */
    private Integer storageGoodsStock;

    /**
     * 商品类目属性集合
     */
    private List<GoodsCategoryAttributesEntity> categoryAttributesList;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operateTime;

    /**
     * 更新人
     */
    private String commonUpdateBy;

    /**
     * 商品来源:0.商家自营商品,1:代发平台供应链商品,2:怡亚通
     */
    private Integer supplierSource;
}
