package com.houjiang.payment.dto.applet;

import com.houjiang.payment.dto.base.PaymentBaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关闭小程序链接响应DTO
 * 接口：21.关闭小程序链接
 * 
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CloseAppletUrlResponse extends PaymentBaseResponse {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 关闭状态(必填)
     * 0: 链接关闭失败
     * 1: 链接关闭成功
     */
    private Integer closeState;
}
