package com.houjiang.order.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitPlatformRefundDTO {

    /**
     * 订单id
     */
    @NotEmpty(message = "订单id不能为空")
    private String orderId;

    /**
     * 平台退款凭证
     */
    @NotEmpty(message = "平台退款凭证不能为空")
    private List<String> returnPaymentVoucherList;

}
