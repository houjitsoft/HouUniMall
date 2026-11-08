package com.houjiang.order.dto;

import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ReturnReportQueryDTO extends UserTokenBeanDTO {

    /**
     * 卖家店铺名称
     */
    public String shopName; 
    /**
     * 
     */
    public String token;
    /**
     *订单号
     */
    public String orderId; 
    /**
     *
     */
    public String orderNumber;
    /**
     *
     */
    public String returnType;
    /**
     *退款编号
     */
    public String returnCode; 
    /**
     *商家处理结果
     */
    public Integer returnState; 
    /**
     *添加时间
     */
    public String returnAddTimeStart; 
    /**
     *添加时间
     */
    public String returnAddTimeEnd; 
    /**
     *支付方式
     */
    public String payCode;
    /**
     *开始
     */
    public Integer start;
    /**
     *限度
     */
    public Integer limit;

    
}
