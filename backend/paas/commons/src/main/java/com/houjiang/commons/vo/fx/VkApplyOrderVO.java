package com.houjiang.commons.vo.fx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 * @date 2021-06-0819:55
 * @Description: 微客申请购买订单
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VkApplyOrderVO {

    private String orderId;
    private String shareUserId;
    private String buyerUserId;
    private Integer buyNum;
    private Integer shopId;

}
