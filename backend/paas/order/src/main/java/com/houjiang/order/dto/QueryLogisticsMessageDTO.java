package com.houjiang.order.dto;

import com.houjiang.commons.group.Select;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 多个物流公司实体类
 * @author Amos
 */
@Data
public class QueryLogisticsMessageDTO {

    /**
     * 快递名称
     */
    private String shippingName;

    /**
     * 物流单号
     */
    @NotEmpty(message = "物流单号不能为空",  groups = {Select.class})
    private String orderShippingCode;

    /**
     * 快递公司id
     */
    @NotNull(message = "快递公司id不能为空",  groups = {Select.class})
    private Integer orderShippingExpressId;

    /**
     * 快递手机号
     */
    private String orderShippingMobile;

    /**
     * 收货地址
     */
    private String orderShippingAddress;

    /**
     * 订单id
     */
    private String orderId;

}
