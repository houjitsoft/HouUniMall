package com.houjiang.commons.vo.shop;

import com.houjiang.commons.dto.shop.ShopPercentDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class ShopEvaluationVO {
    /** 评价Id */
    private Integer evaluationId;
    /** 店铺Id */
    private Integer shopId;
    /** 用户Id */
    private String userId;
    /** 订单Id */
    private String orderId;
    /** 服务态度评分 */
    private double avgServicecredit;
    /** 发货速度评分 */
    private double avgDeliverycredit;
    /** 描述相符评分 */
    private double avgDesccredit;
    /** 描述相符评分 */
    private double evaluationDesccredit;
    /** 服务态度评分 */
    private double evaluationServicecredit;
    /** 发货速度评分 */
    private double evaluationDeliverycredit;
    /** 平均发货时长 */
    private BigDecimal avgShippingTime;
    /** 评价时间 */
    private Date evaluationCreateTime;

    /** 店铺百分比 */
    private ShopPercentDTO shopPercent;


}
