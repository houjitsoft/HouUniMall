package com.houjiang.base.vo;


import com.houjiang.commons.constant.CommonType;
import lombok.Data;

import java.math.BigDecimal;
/**
 * @ClassName GoodsIdsQueryVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsIdsQueryVO {

    /**
     * 商品公共表id
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称（+规格名称）
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 市场价
     */
    private BigDecimal goodsMarketPrice;

    /**
     * 商品主图
     */
    private String goodsImage;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 销售数量
     */
    private Integer goodsSalenum;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
     */
    private Integer commonIsXian;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer activityStatus;

    /**
     * 活动详情
     */
    private ActivityGoodsDetailVO activityGoodsDetail;

    /**
     * 角标图片
     */
    private String cornerUrl;

    /**
     * 单人最大购买数量
     */
    private Integer goodsMaxSale;

    /**
     * 最小限购数据
     */
    private Integer goodsMinSale;

    /**
     * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
     */
    private Integer dealerGoodsFlag;

    /**
     * 是否需要经销审核 1是 2否
     */
    private Integer sellCheck;

    /**
     * 1一键上架 2立即购买
     */
    private Integer sellDeliver;

    /**
     * 操作类型 1一键上架  2立即购买 3申请经销商
     */
    private Integer operateType;

    /**
     * 秒杀开始时间
     */
    private Long goodsStartTime;

    /**
     * 秒杀结束时间
     */
    private Long goodsEndTime;

    /**
     * 商品库存
     */
    private Integer goodsStock;


    public void setOperateType2(GoodsIdsQueryVO goods) {
        if (null != goods.getSellCheck()) {
            if (goods.getSellCheck().intValue() == 1) {
                this.operateType = CommonType.supplierGoodsStatus.APPLY.getCode();
            } else {
                this.operateType = goods.getSellDeliver();
            }
        }
    }
}
