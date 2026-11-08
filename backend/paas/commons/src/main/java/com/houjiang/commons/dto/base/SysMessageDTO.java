package com.houjiang.commons.dto.base;

import lombok.Data;

/**
 * @ClassName SysMessageDTO
 * @Description
 * @Author Amos
 * @Date 2025/2/24 15:37
 * @Version 1.0
 * @author Amos
 */

@Data
public class SysMessageDTO {

    /**
     * 消息订单ID
     * 用于唯一标识一条消息与特定订单的关联
     */
    public  String messageOrderId;

    /**
     * 卖家ID
     * 用于标识消息相关的卖家用户
     */
    public String sellerId;

    /**
     * 卖家名称
     * 卖家的可读名称，便于在消息中显示
     */
    public String sellerName;

    /**
     * 消息类型
     * 表示消息的种类，如促销、订单更新等
     */
    public Integer messageType;

    /**
     * 消息标题
     * 消息的简短标题，概括消息内容
     */
    public String messageTitle;

    /**
     * 消息内容
     * 消息的详细内容，包含具体的信息
     */
    public String messageContent;

    /**
     * 店铺ID
     * 消息相关的店铺标识，用于区分不同的店铺
     */
    public String shopId;

}
