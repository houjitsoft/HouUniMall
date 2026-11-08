package com.houjiang.payment.dto.division;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 查询分账订单响应DTO
 * 接口：17.查询分账订单
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryDivisionResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 原收款订单的通企付支付订单号(必填)
     */
    private String payOrderId;
    
    /**
     * 原收款订单的商户订单号(必填)
     */
    private String mchOrderNo;
    
    /**
     * 商户系统分账订单号(必填)
     */
    private String mchAssureOrderNo;
    
    /**
     * 通企付分账订单号(必填)
     */
    private String assureOrderId;
    
    /**
     * 分账批次号(可选)
     */
    private String batchNo;
    
    /**
     * 分账订单状态(必填)
     * 0-订单生成
     * 1-进行中
     * 2-分账成功
     * 3-分账失败
     */
    private Integer state;
    
    /**
     * 本次解冻的订单金额,单位:分(可选)
     */
    private Long orderAmount;
    
    /**
     * 累计解冻的订单金额,单位:分(必填)
     */
    private Long totalOrderAmount;
    
    /**
     * 抽佣金额,单位:分(可选)
     */
    private Long couponAmount;
    
    /**
     * 商户扩展参数(可选)
     */
    private String extParam;
    
    /**
     * 订单创建时间,13位时间戳(必填)
     */
    private Long createdAt;
    
    /**
     * 订单分账成功时间,13位时间戳(可选)
     */
    private Long successTime;
    
    /**
     * 渠道错误描述(可选)
     */
    private String errMsg;
    
    /**
     * 分账明细列表(可选)
     * 如果不存在分账则没有
     */
    private List<DivisionDetailDTO> divisionDetails;
}
