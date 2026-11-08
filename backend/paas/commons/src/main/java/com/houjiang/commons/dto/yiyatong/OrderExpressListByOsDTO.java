package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

/**
 * @ClassName tOrderExpressListByOsDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 10:09
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderExpressListByOsDTO {

    /**
     * 订单编号（如果是售后单，传售后单号）
     */
    private String orderSn;

    /**
     * 物流单号
     */
    private String deliverySn;



}
