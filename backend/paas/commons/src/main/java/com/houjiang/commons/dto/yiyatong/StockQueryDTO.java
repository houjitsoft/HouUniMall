package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.util.List;

/**
 * @ClassName StockQueryDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 17:16
 * @Version 1.0
 * @author Amos
 */
@Data
public class StockQueryDTO {

    /**
     * 省份编码
     */
    private String areaRegionCode;

    /**
     * sku库存组合
     */
    private List<StockItemDTO> itemDTOList;
}
