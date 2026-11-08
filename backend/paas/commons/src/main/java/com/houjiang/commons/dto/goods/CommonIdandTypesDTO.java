package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName CommonIdandTypesDTO
 * @Description 商品id和类型
 * @Author Amos
 * @Date 2024/7/23 13:45
 * @Version 1.0
 * @author Amos
 */

@Data
public class CommonIdandTypesDTO {
    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 1普通商品   2供应商商品',
     */
    private Integer goodsType;

}
