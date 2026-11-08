package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

/**
 * @ClassName FreightSpuInfoDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 16:32
 * @Version 1.0
 * @author Amos
 */

@Data
public class FreightSpuInfoDTO {

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品spuId
     */
    private String spuId;
}
