package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.util.List;

/**
 * @ClassName OpenFindSkuStockDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 16:53
 * @Version 1.0
 * @author Amos
 */

@Data
public class OpenFindSkuStockDTO {

    /**
     *  分销商ID
     */
    private Integer distriId;

    /**
     * skuIdList
     */
    private List<String> skuIdList;
}
