package com.houjiang.order.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class QukVocherImagesDTO {

    /**
     * 自定义注释，表示这是一个token变量
     */
    private String token;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 买家用户ID
     */
    private String buyerUserId;

    /**
     * 评论状态，可能是一个枚举类型，用于表示评论是否被通过等状态
     */
    private Integer reviewStatus;

    /**
     * 评论消息，用于存储买家对卖家的评论内容
     */
    private String reviewMessage;

}
