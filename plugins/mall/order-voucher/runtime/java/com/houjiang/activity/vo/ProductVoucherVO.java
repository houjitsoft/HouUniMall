package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @ClassName ProductVoucherVO
 * @Description
 * @Author Amos
 * @Date 2024/9/6 13:34
 * @Version 1.0
 * @author Amos
 */
@Data
public class ProductVoucherVO {
    /**
     * 兑换券id
     */
    private Integer voucherTId;
    /**
     * 兑换券名称
     */
    private String voucherTTitle;
    /**
     * 状态 (1-有效,2-失效)
     */
    private Integer voucherTState;
    /**
     * 活动海报
     */
    private String  voucherTCustomimg;
    /**
     * 开始时间
     */
    private String voucherTStartDate;
    /**
     * 结束时间
     */
    private String voucherTEndDate;
    /**
     * 每人发放数量
     */
    private Integer voucherTEachlimit;
    /**
     * 创建时间
     */
    private String voucherTAddDate;
    /**
     * 活动链接
     */
    private String activityUrl;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 店铺名称
     */
    private String shopName;

}
