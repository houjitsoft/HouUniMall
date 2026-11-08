package com.houjiang.payment.dto.division;

import lombok.Data;

import java.util.List;

/**
 * 待分账信息DTO
 * 
 * @author Amos
 */
@Data
public class BatchDivisionInfoDTO {
    
    /**
     * 商户分账订单号(必填)
     */
    private String mchAssureOrderNo;
    
    /**
     * 原收款订单的商户订单号(可选)
     * 与通企付支付订单号二选一,yunst2yhzh不需要传递
     * 针对统一下单接口且支付方式为微信预消费和微信B2B门店助手的订单,建议使用商户订单号发起分账
     */
    private String mchOrderNo;
    
    /**
     * 原收款订单的通企付支付订单号(可选)
     * 与商户订单号二选一,yunst2yhzh不需要传递
     */
    private String payOrderId;
    
    /**
     * 本次解冻订单金额,单位:分(可选)
     * 该金额减去抽佣和分账金额总和之后,剩余的金额则分给收款方
     * 1.allinpay渠道不可用
     * 2.yunst2isv渠道,单次分账时不需要上送,多次分账时该字段必需上送,解冻总金额必须小于等于订单剩余未分账金额
     * 3.yunst2yhzh渠道需要上送
     */
    private Long orderAmount;
    
    /**
     * 平台抽佣金额,单位:分(可选)
     * 不抽佣不填,如果选了分账模板编号此字段无效
     * 仅yunst2isv渠道可用
     */
    private Long couponAmount;
    
    /**
     * 分账模板编号(可选)
     * 如果选了分账模板编号,平台抽佣金额和分账信息字段无效
     * allinpay渠道不可用
     */
    private Long receiverGroupId;
    
    /**
     * 分账出金方商户会员编号(可选)
     * 仅yunst2yhzh可填写,不填默认为平台收款方
     */
    private String sendMemNo;
    
    /**
     * 分账信息列表(可选)
     * 1.allinpay渠道则必填,且仅允许一组信息
     * 2.yunst2isv和yunst2yhzh渠道如果不分账则不填(如果选了分账模板编号此字段无效)
     * 3.若couponAmount、receiverGroupId和divisionDetails都为空时,则表示将字段"本次解冻订单金额"的全部金额确认给收款方
     */
    private List<DivisionDetailDTO> divisionDetails;
    
    /**
     * 商户拓展参数(可选)
     * 最多100个字符,用于透传给商户,不可包含竖线特殊字符
     */
    private String extParam;
}
