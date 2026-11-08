package com.houjiang.commons.dto.goods;

import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShopListQueryDTO extends PageHelper {

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
     * 店铺所在地址
     */
    private String shopCompanyAddress;

    /**
     * 商品
     */
    private List goods;
    /**
     * 1 渠道店铺 0正常店铺
     */
    private Integer shopOrigin;

    /**
     * 店铺类型: 1-卖家店铺; 2:供应商店铺
     */
    private Integer shopType;


    public Integer getShopType() {
        return (null == shopType || shopType.intValue() == 0) ? CommonType.ShopType.COMMON_SHOP.getCode() : shopType;
    }

    public void setShopName(String shopName) {
        if (null != shopName && !shopName.equals("")) {
            this.shopName = shopName.trim();
        } else {
            this.shopName = shopName;
        }
    }
}
