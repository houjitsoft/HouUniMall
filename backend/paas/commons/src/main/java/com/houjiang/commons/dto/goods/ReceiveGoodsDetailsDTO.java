package com.houjiang.commons.dto.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * fegin发送回来的参数
 *
 * @author Amos
 */

@Data
public class ReceiveGoodsDetailsDTO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品公共表id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 供应商商品指导价
     */
    private BigDecimal guidePrice;

    /**
     * 库存
     */
    private Integer goodsStock;

    /**
     * 规格名称
     */
    private String commonSpecName;

    /**
     * 规格
     */
    private String goodsSpec;

    /**
     * 1-上架 2-下架 0店铺关闭手动下架
     */
    private Integer goodsIsShelves;

    /**
     * 购物车商品状态
     */
    private Integer cartStatus;

    /**
     * 最大购买数量
     */
    private Integer goodsMaxSale;

    /**
     * 最小购买数量
     */
    private Integer goodsMinSale;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 商品状态 0下架，1正常，10违规（禁售）,2店铺下架商品下架
     */
    private Integer commonState;

    /**
     * 是否开具增值税发票 1是，0否
     */
    private Integer shopInvoices;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer commonIsXian;

    /**
     * 每人限购 0代表不限购
     */
    private Integer commonLimit;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer activityStatus;

    /**
     * 活动详情
     */
    private ActivityGoodsDetailDTO activityGoodsDetail;

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

    /**
     * 商品所需积分
     */
    private Integer needIntegral;

    /**
     * 是否是虚拟商品
     */
    private Integer commonIsVirtual;

    /**
     * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)
     * 2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    private Integer dealerGoodsFlag;

    /**
     * 商品类型
     */
    private Integer goodsType;

    /**
     * 起订量
     */
    private Integer moq;

    /**
     * 税率
     */
    private Double taxRate;

    /**
     * 是否是询价商品
     */
    private Integer isEnquiry;

    /**
     * 单位
     */
    private String dw;

    /**
     * 订货编码
     */
    private String goodsCode;

    /**
     * 发票
     */
    private Integer invoiceActive;

    /**
     * 活动状态
     */
    private Integer activityGoodsState;

    /**
     * 会员商品
     */
    private Integer plusGoods;
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date goodsStartTime;

    /**
     * 库存
     */
    private Integer commonStock;



    /**
     * 积分支付比例
     */
    private Double pointsRate;

    /**
     * 现金价格
     */
    private BigDecimal cashPrice;



}
