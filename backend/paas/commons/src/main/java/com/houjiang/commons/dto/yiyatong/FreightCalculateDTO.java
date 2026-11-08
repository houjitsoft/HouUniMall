package com.houjiang.commons.dto.yiyatong;

import lombok.Data;

import java.util.List;

/**
 * @ClassName FreightCalculateDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 16:31
 * @Version 1.0
 * @author Amos
 */

@Data
public class FreightCalculateDTO {

    /**
     * 订单商品及数量
     */
    List<FreightSpuInfoDTO> spuInfoList;

    /**
     * 收货城市ID（二级区域编码)
     */
    private String cityId;
}
