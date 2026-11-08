package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

/**
 * @ClassName ReturnYiYaTongInfoDTO
 * @Description
 * @Author Amos
 * @Date 2024/12/11 9:33
 * @Version 1.0
 * @author Amos
 */
@Data
public class ReturnYiYaTongInfoDTO {

    /**
     * 订单编号，用于唯一标识一个订单
     */
    private String orderSn;

    /**
     * 原始订单状态，保存订单的初始状态代码
     */
    private String origStatus;

    /**
     * 原始订单状态名称，用于展示原始状态的描述性信息
     */
    private String origStatusName;

    /**
     * 退货单编号，用于唯一标识一个退货单
     */
    private String returnSn;

    /**
     * 当前订单状态，表示订单的当前状态代码
     */
    private String status;

    /**
     * 当前订单状态名称，用于展示订单当前状态的描述性信息
     */
    private String statusName;
}
