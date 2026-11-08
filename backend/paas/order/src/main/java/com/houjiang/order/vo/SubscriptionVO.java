package com.houjiang.order.vo;

import com.houjiang.order.dto.ReceiverDTO;
import com.houjiang.order.dto.SenderDTO;
import lombok.Data;

/**
 * 物流订阅所需参数
 * @author Amos
 */
@Data
public class SubscriptionVO {

    /**
     * 快递的简称
     */
    private String ShipperCode;

    /**
     * 物流代码
     */
    private String LogisticCode;

    /**
     * 是否发送消息
     */
    private Integer IsSendMessage;

    /**
     * 收件人参数
     */
    private ReceiverDTO receiverPara;

    /**
     * 发件人参数
     */
    private SenderDTO senderPara;

}
