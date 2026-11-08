package com.houjiang.goods.dto;

import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class GoodsListQueryDTO extends PageUserTokenBeanDTO {
    /**
     * 商品名
     */
    private String commonName;
    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
     */
    private Integer commonIsXian;
}
