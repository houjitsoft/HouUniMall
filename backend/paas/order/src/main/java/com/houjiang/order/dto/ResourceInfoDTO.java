package com.houjiang.order.dto;

import lombok.Data;

/**
 * 资源信息
 * @author Amos
 */
@Data
public class ResourceInfoDTO {

    /**
     * 卖家id
     */
    private String sellerUserId;

    /**
     * 买家id
     */
    private String buyerUserId;

}
