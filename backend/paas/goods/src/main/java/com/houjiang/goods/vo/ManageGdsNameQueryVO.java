package com.houjiang.goods.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName ManageGdsNameQueryVO
 * @Author Amos
 * @Date 2023/6/26 10:04
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsNameQueryVO {

    private Integer commonId;
    private String commonName;


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
     * 市场价格
     */
    private String commonMarketPrice;

    /**
     * 库存
     */
    private Integer commonStock;
    /**
     * 称重商品库存
     */
    private Integer weighingGoodsStock;

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
    private Integer commonIsLocked;
    private String commonStateRemark;
    private String commonVerifyRemark;

    /**
     * 1:商城价2:小宝价格3:商城和小宝价组合
     */
    private Integer commonType;
    /**
     * 是否支持门店自提 1是 2否
     */
    private Integer commonIsShopTake;

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
     * 允许编辑内容 1是 2否
     */
    private Integer sellEdit;
    /**
     * 允许修改价格 1是 2否
     */
    private Integer sellEditPrice;
    /**
     * 1普通商品   2供应商商品',
     */
    private Integer goodsType;
    /**
     * 经销商id
     */
    private Integer dealerShopId;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;

    /**
     * plus商品创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date plusTime;
    /**
     * plus商品
     */
    private Integer plusGoods;
    /**
     * 折扣价格
     */
    private String discountPrice;
    /**
     * 商品信息
     */
    private List<ManageGoodsBaseListVO> goodsList;

    /**
     * 1爆品 2不是爆品
     */
    private Integer hotState;

    /**
     * 虚拟商品 0 是 1 否
     */
    private Integer virtualGoods;

    /**
     * 商品来源:0.商家自营商品,1:代发平台供应链商品,2:怡亚通
     */
    private Integer supplierSource;

}
