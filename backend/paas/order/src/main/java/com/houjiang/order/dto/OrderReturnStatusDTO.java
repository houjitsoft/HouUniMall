package com.houjiang.order.dto;

import lombok.Data;

/**
 * 订单返回状态
 * @author Amos
 */
@Data
public class OrderReturnStatusDTO {

    /**
     * 售后单号
     */
    private String returnCode;

    /**
     * 售后状态
     */
    private Integer returnState;

    /**
     * 用户修改次数
     */
    private Integer returnChangeNum;

    /**
     * 用户重新提交次数
     */
    private String resubmissionsNumber;
}
