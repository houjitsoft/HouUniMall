package com.houjiang.commons.vo.shop;

import lombok.Data;

import java.util.List;

/**
 * 店铺
 * @author Amos
 */
@Data
public class ManageShopBaseVO {

    /**
     * 分区ID
     */
    private Integer districtId;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 是否自营商品 1 不是 2 是
     */
    private String shopSelfSupport;

    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料 0:关闭',前台传入
     */
    private Integer shopStatus;

    /**
     * 店铺经营分类
     */
    private List<Integer> catIdList;

}
