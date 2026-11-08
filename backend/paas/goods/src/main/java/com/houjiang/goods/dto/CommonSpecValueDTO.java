package com.houjiang.goods.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName CommonSpecValueDTO
 * @Author Amos
 * @Date 2023/6/27 9:58
 * @Version 1.0
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonSpecValueDTO {
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品规格id
     */
    private Integer goodsSpecId;
    /**
     *
     */
    private Integer specNameId;
}
