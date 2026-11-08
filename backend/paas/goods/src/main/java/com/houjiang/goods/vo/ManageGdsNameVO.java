package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 管理名称
 * @author Amos
 */
@Data
public class ManageGdsNameVO {

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品spu名称
     */
    private String commonName;

    /**
     * 商品sku名称
     */
    private String goodsName;

    /**
     * 商家编号
     */
    private String commonCode;

    /**
     * 平台货号
     */
    private String commonPlatformCode;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 商品主图
     */
    private String commonImage;

    /**
     * 商品价格
     */
    private String commonPrice;

    /**
     * 库存
     */
    private Integer commonStock;

    /**
     * 商品价格
     */
    private String goodsPrice;

    /**
     * 库存
     */
    private Integer goodsStock;

    /**
     * 上架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonSellTime;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 锁定 2 锁定 1 不锁定
     */
    private Integer commonIsLocked;

    /**
     * 违规原因
     */
    private String commonStateRemark;

    /**
     * 审核失败原因
     */
    private String commonVerifyRemark;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 小宝价格
     */
    private BigDecimal commonBabyPrice;

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 主图类型 1图片2视频
     */
    private Integer type;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonAddTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonUpdateTime;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonVerifyTime;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 成本价
     */
    private BigDecimal commonCostPrice;

    /**
     * 商品销量
     */
    private Integer commonSalenum;

    /**
     * 参与的跨店满减活动
     */
    private List<VoucherMessageVO> voucherMes;

    /**
     * 0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品
     */
    private Integer dealerGoodsFlag;

    /**
     * 0正常  1只能修改商品内容 2不可修改
     */
    private Integer commonDistributorFlag;

    /**
     * 允许编辑内容 1是 2否
     */
    private Integer sellEdit;

    /**
     * 允许修改价格 1是 2否
     */
    private Integer sellEditPrice;

    /**
     * 每人限购 0 代表不限购
     */
    private Integer commonLimit;

    /**
     * 1普通商品 2供应商商品
     */
    private Integer goodsType;

    /**
     * 经销商用户店铺id
     */
    private Integer dealerShopId;

    /**
     * 是否询价商品
     */
    private Integer isEnquiry;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 商品含税价格
     */
    private BigDecimal taxInclusiveAmount;

    /**
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;

    /**
     * 经销商品是否允许上架
     */
    private Boolean detealToSell;

    private Integer supplierSource;

    /**
     * 积分支付比例
     */
    private Double pointsRate;

    private List<GoodscCatVO> catList;
}
