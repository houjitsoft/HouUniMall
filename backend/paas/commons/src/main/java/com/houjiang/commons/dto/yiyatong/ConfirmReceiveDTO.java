package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

/**
 * @ClassName ConfirmReceiveDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/14 10:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class ConfirmReceiveDTO {

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 操作人
     */
    private String operator;
}
