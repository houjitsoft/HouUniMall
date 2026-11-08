package com.houjiang.mall.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 * 对应数据库表：hou_goods_base
 * 
 * @author Amos
 */
@Data
@TableName("hou_goods_base")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品 ID
     */
    @TableId(value = "goods_id", type = IdType.AUTO)
    private Integer goodsId;

    /**
     * 商品公共 ID
     */
    private Integer commonId;

    /**
     * 店铺 ID
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 供应商店铺 ID
     */
    private Integer supplierShopId;

    /**
     * 供应商商品 ID
     */
    private Integer supplierGoodsId;

    /**
     * 商品名称（+规格名称）
     */
    private String goodsName;

    /**
     * 商品分类 ID
     */
    private Integer catId;

    /**
     * 品牌 ID
     */
    private Integer brandId;

    /**
     * 商品规格 -JSON 存储
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
     * 门店库存总和最大值
     */
    private Integer goodsShopGateStock;

    /**
     * 库存预警值
     */
    private Integer goodsAlarm;

    /**
     * 订货编码
     */
    private String goodsCode;

    /**
     * 商品推荐 1 是，0 否 默认 0
     */
    private Integer goodsIsRecommend;

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
     * 商品主图
     */
    private String goodsImage;

    /**
     * 颜色 id
     */
    private Integer colorId;

    /**
     * 好评星级
     */
    private Integer goodsEvaluationGoodStar;

    /**
     * 评价数
     */
    private Integer goodsEvaluationCount;

    /**
     * 单人最大购买数量
     */
    private Integer goodsMaxSale;

    /**
     * 上下架状态：1-上架 2-下架 0-店铺关闭手动下架
     */
    private Integer goodsIsShelves;

    /**
     * 商品来源 id
     */
    private Integer goodsParentId;

    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;

    /**
     * 最小限购数量
     */
    private Integer goodsMinSale;

    /**
     * 商家编号
     */
    private String merchantCode;

    /**
     * 商品条型码
     */
    private String commodityBarTypeCode;

    /**
     * 是否使用 0：未使用；1：已使用
     */
    private Integer isUse;

    /**
     * 规格值
     */
    private String specValue;

    /**
     * 成本价
     */
    private BigDecimal goodsCostPrice;

    /**
     * 是否删除 (0:否;1:是)
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 时间戳
     */
    private LocalDateTime ts;

    /**
     * 购买积分商品所需积分
     */
    private Integer needIntegral;

    /**
     * 含税金额
     */
    private BigDecimal taxInclusiveAmount;

    /**
     * 税率
     */
    private BigDecimal taxRate;

    /**
     * 单位
     */
    private String dw;

    /**
     * 起订量
     */
    private Integer moq;

    /**
     * 是否询价，0 为不可询，1 为可询
     */
    private Integer isEnquiry;

    /**
     * 是否自动下架 0 否 1 是
     */
    private Integer isAutoOffShelf;

    /**
     * 建议零售价
     */
    private String msrp;

    /**
     * 样品申请的最大数量
     */
    private Integer sampleGoodsNum;

    /**
     * 供应商店铺名称
     */
    private String supplierShopName;

    /**
     * 外部链接
     */
    private String externalLink;
}
