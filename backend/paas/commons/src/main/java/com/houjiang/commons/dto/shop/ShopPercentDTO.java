package com.houjiang.commons.dto.shop;

import lombok.Data;

/**
 * 店铺同期百分比
 * @author Amos
 */
@Data
public class ShopPercentDTO {

    /**
     * 服务态度百分比
     */
    private String servicecreditPercent;

    /**
     * 发货速度百分比
     */
    private String deliverycreditPercent;

    /**
     * 描述相符百分比
     */
    private String desccreditPercent;

}
