package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @ClassName PcConfigShopVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class PcConfigShopVO {
    /**
     * 店铺名称
     */
    private String shopName;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 店铺商品主图 任意一个商品
     */
    private String commonImage;
    /**
     * 粉丝数
     */
    private Integer shopCollect;
    /**
     * 是否自营
     */
    private String shopSelfSupport;

}
