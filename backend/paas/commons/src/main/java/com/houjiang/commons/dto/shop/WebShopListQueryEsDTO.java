package com.houjiang.commons.dto.shop;

import com.houjiang.commons.dto.goods.GoodsListQueryEsDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 不能随意修改
 *
 * @author Amos
 */
@Data
public class WebShopListQueryEsDTO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺粉丝数
     */
    private Integer shopCollect;

    /**
     * 轮播图
     */
    private String shopSlide;

    /**
     * 店铺logo
     */
    private String wapShopLogo;

    /**
     * 店铺logo
     */
    private String shopLogo;

    /**
     * 区域id
     */
    private Integer districtId;

    /**
     * 店铺地址
     */
    private String shopAddress;

    /**
     * 商品信息集合
     */
    private List<GoodsListQueryEsDTO> commonList;

    /**
     * 店铺货品数量
     */
    private Integer shopGoodsSum;

    /**
     * 店铺导航
     */
    private List<MerchantShopNavQueryDTO> shopNav;

    /**
     * 自营店铺
     */
    private String shopSelfSupport;

    /**
     * 店铺百分比
     */
    private ShopPercentDTO shopPercentDTO;

    /**
     * 评分对象
     */
    private ShopEvaluationDTO shopEvaluationDTO;
    /**
     * 是否开通会员 0 过期 1 开 2 未开
     */
    private Integer plusMember;
    /**
     * plus会员折扣
     */
    private BigDecimal plusDiscount;

    /**
     * 店铺列表优先展示
     */
    private Integer shopListFirstShow;

    /**
     * 等级id
     */
    public Integer levelId;
    /**
     * 用户手机号
     */
    public String userMobile;
}
