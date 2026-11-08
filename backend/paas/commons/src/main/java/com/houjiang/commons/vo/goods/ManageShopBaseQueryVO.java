package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @ClassName ManageShopBaseQueryVO
 * @Author Amos
 * @Date 2023/6/26 16:58
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageShopBaseQueryVO {

    /**
     * 所在地，使用最后一级分类
     */
    private Integer districtId;
    /**
     * 店铺名
     */
    private String shopName;
    /**
     * 是否自营
     */
    private String shopSelfSupport;
    /**
     * 店铺状态-3：开店成功 2:待审核付款 1:待审核资料  0:关闭
     */
    private Integer shopStatus;
}
