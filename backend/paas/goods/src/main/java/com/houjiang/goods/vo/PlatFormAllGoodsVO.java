package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class PlatFormAllGoodsVO {

    /**
     * 商品公共id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 商品名称
     */
    private String commonName;

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
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品主图
     */
    private String commonImage;

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 副标题，即商品广告词
     */
    private String commonPromotionTips;

    /**
     * 商品状态 0下架（放入仓库），1正常，10违规（禁售）
     */
    private Integer commonState;

    /**
     * 商品审核 1通过，0未通过，10审核中
     */
    private Integer commonVerify;

    /**
     * 商品库存
     */
    private Integer commonStock;

    /**
     * 商品库存警报线
     */
    private Integer commonAlarm;

    /**
     * 商品添加时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonAddTime;

    /**
     * 是否允许退货 1 是，2否
     */
    private Integer commonIsReturn;

    /**
     * 上架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonSellTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonUpdateTime;

    /**
     * 商品是否被锁定，默认为1,不锁定；2锁定',
     */
    private Integer commonIsLocked;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;

    /**
     * 小宝价格
     */
    private BigDecimal commonBabyPrice;

    /**
     * 操作人
     */
    private String orderOperator;

    /**
     * 操作时间
     */
    private String orderOperatorTime;

    /**
     * 参与的专区列表
     */
    private List<String> noneNameList;

    /**
     * 商品是否屏蔽：1-否；2-是
     */
    private Integer commonIsScreen;

    /**
     * 商品销量
     */
    private Integer commonSalenum;

    /**
     * 营业执照号
     */
    private String businessId;

    /**
     * 营业执照电子版
     */
    private String businessLicenseElectronic;

    /**
     * 入驻类型 0:个人入驻 1:企业入驻 2:门店厂商 3:供应商 4:品牌店铺
     */
    private Integer shopBusiness;

    /**
     * 提交审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date commonVerifyTime;

    /**
     * 是否是虚拟商品
     */
    private Integer virtualGoods;

    /**
     * 店铺类型 1:卖家店铺; 2:供应商店铺 3:品牌店铺
     */
    private Integer shopType;

    /**
     * 是否自营
     */
    private String shopSelfSupport;

    /**
     * 爆品状态 0不是爆品 1待审核  2审核通过  3审核不通过 4删除
     */
    private Integer hotState = 0;

    /**
     * 主图类型 1图片2视频
     */
    private Integer type;

    /**
     * 1普通商品   2供应商商品',
     */
    private Integer goodsType;

    /**
     * 商品库存
     */
    private List<GoodsStockVO> goodsStockEntityList;

    /**
     * 经销商品描述
     */
    private String goodsDealerDesc;
    /**
     * 0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品
     */
    private Integer dealerGoodsFlag;

    /**
     * 仓库商品库存
     */
    private Integer storageGoodsStock;

    /**
     * 积分支付比例
     */
    private Double pointsRate;

    /**
     * 现金价格
     */
    private BigDecimal cashPrice;


}
