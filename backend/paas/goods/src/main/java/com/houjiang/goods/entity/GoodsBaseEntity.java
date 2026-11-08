package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * 商品表(GoodsBase)
 *
 * @since 2023-05-19 13:52:02
 * @author Amos
 */
@TableName("goods_base")
@Data
@Accessors(chain = true)
public class GoodsBaseEntity  implements Serializable {
    /**
     * 商品id
     */
    @TableId(type = IdType.AUTO)
    private Integer goodsId;
    /**
     * 商品公共表id
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
     * 供应商店铺id
     *
     * <p>与 shopId 指向同一物理列（hou_supplier_goods_base.supplier_shop_id）。
     * 兼容视图已将 supplier_shop_id 映射为 shop_id，若本字段再参与 SQL，
     * 同一基表列会在视图中被引用两次，导致视图不可写入，故标记为非数据库字段。</p>
     */
    @TableField(exist = false)
    private Integer supplierShopId;
    /**
     * 供应商商品id
     *
     * <p>与 goodsId 指向同一物理列（hou_supplier_goods_base.supplier_goods_id），
     * 同上原因标记为非数据库字段。</p>
     */
    @TableField(exist = false)
    private Integer supplierGoodsId;
    /**
     * 商品名称（+规格名称）
     */
    private String goodsName;
    /**
     * 促销提示
     */
    private String goodsPromotionTips;
    /**
     * 商品分类id
     */
    private Integer catId;
    /**
     * 品牌id
     */
    private Integer brandId;
    /**
     * 商品规格-JSON存储
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
     * 商家编号货号
     */
    private String goodsCode;
    /**
     * 商品推荐 1是，0否 默认0
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
     * 1-上架 2-下架 0店铺关闭手动下架
     */
    private Integer goodsIsShelves;
    /**
     * 商品重量
     */
    private BigDecimal goodsCubage;
    /**
     * 最小限购数据
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
     * 规格值
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
     * 是否删除(0:否;1:是)
     */
    private Integer isDelete;
    /**
     * 创建者
     */
    private String createUser;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改人
     */
    private String updateUser;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 时间戳
     */
    private Date ts;
    /**
     * 购买积分商品所需积分
     */
    private Integer needIntegral;
    /**
     * 建议零食价
     */
    private String msrp;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

