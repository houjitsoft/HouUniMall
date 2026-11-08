package com.houjiang.goods.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import com.houjiang.commons.vo.goods.SpecParamJsonBeanDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 商品公共信息添加
 *
 * @author Amos
 */
@Data
public class IntegralCommonDTO extends UserTokenBeanDTO {

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 分类id
     */
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
     * 分类id
     */
    private Integer catId;

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
     * 商品属性
     */
    private String commonProperty;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonAddTime;

    /**
     * 上架时间,发布时间
     */
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date commonSellTimeStr;

    /**
     * 上架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonSellTime;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;


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
     * 商品所在地 json,目前为空
     */
    private String commonLocation;

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

    /**
     * 不同规格商品
     */
    private List<BaseAddParamDTO> gdsBases;

    /**
     * 商品详情
     */
    private GoodsCommonDetailDTO goodsCommonDetail;

    /**
     * 图片
     */
    private List<GoodsImagesDTO> goodsImages;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonUpdateTime;

    /**
     * 更新人
     */
    private Integer commonUpdateBy;

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
     * 过期退款 0 是 1 否
     */
    private Integer pastDue;

    /**
     * 是否是积分商品
     */
    private Integer integralGoods;


}
