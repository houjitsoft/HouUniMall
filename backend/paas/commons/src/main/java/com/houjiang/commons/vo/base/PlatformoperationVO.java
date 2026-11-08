package com.houjiang.commons.vo.base;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class PlatformoperationVO {
    /**
     * 默认发货时间
     */
    private Integer deliveryTime;
    /**
     * 是否开启退款扣费  0 未开启  1 开启
     */
    private Integer configEnable;
    /**
     * 通用折扣
     */
    private Integer universalRatio;
    /**
     * 未按时发货
     */
    private OperationEntityVO sendGoods;
    /**
     * 虚假发货
     */
    private OperationEntityVO falseShipment;
    /**
     * 投诉商品
     */
    private Integer complaintGoods;
    /**
     * 投诉商家
     */
    private Integer complaintShop;
    /**
     * 质保金说明
     */
    private String explanation;
    private String configKey;

}
