package com.houjiang.commons.vo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 * @date 2021-05-2911:26
 * @Description: 经销商品购买更新
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerOrderGoodsVO {
    private String paymentNumber;


    private String userId;


    private Integer goodsId;


    private Integer commonId;


    private Integer orderGoodsNum;
}
