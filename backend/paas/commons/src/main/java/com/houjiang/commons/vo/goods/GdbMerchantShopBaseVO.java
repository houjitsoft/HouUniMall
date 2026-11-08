package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigInteger;
import java.util.Date;

/**
 * @ClassName GdbMerchantShopBaseVO
 * @Description shopbase表
 * @Author Amos
 * @Date 2023/6/26 16:07
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbMerchantShopBaseVO {
    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺等级
     */
    private Integer shopGradeId;

    /**
     * 店铺绑定分类，如果是自营店铺就为0.
     */
    private Integer shopClassId;

    /**
     * 是否自营
     */
    private String shopSelfSupport;

    /**
     * 0:个人入驻 1:企业入驻 2:门店厂商 3:供应商  4  品牌店铺
     */
    private Integer shopBusiness;

    /**
     * 开店时间
     */
    private Date shopCreateTime;

    /**
     * 有效期截止时间
     */
    private Date shopEndTime;

    /**
     * 结算周期-天为单位-如果您想设置结算周期为一个月，则可以输入30
     */
    private Integer shopSettlementCycle;

    /**
     * 店铺上次结算时间，若是新开店铺没有结算单，则是开店日期
     */
    private Date shopSettlementLastTime;

    /**
     * 店铺logo
     */
    private String shopLogo;

    /**
     * 店铺banner
     */
    private String shopBanner;

    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭
     */
    private Boolean shopStatus;

    /**
     * 店铺粉丝数
     */
    private Integer shopCollect;

    /**
     * 卖家电话
     */
    private String shopTel;

    /**
     * 免运费额度
     */
    private BigInteger shopFreeShipping;

    /**
     * 店铺印章-将出现在打印订单的右下角位置
     */
    private String shopStamp;

    /**
     * 店铺类型: 1-卖家店铺; 2:供应商店铺 3 品牌店铺
     */
    private Byte shopType;

    /**
     * 所在地，使用最后一级分类
     */
    private Integer districtId;

    /**
     * 店铺默认排序
     */
    private Integer shopDisplayOrder;

    /**
     * 店铺轮播图（PC端）
     */
    private String shopSlide;


    /**
     * wap 店铺 logo
     */
    private String wapShopLogo;

    /**
     * 店铺图片(wap中的店铺首页banner)
     */
    private String shopImage;
}
