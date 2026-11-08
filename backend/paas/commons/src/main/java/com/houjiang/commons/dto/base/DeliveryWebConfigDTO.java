package com.houjiang.commons.dto.base;

import lombok.Data;

import java.util.List;

/**
 * @ClassName DeliveryWebConfigDTO
 * @Description 配置入参
 * @Author Amos
 * @Date 2024/4/29 11:04
 * @Version 1.0
 * @author Amos
 */
@Data
public class DeliveryWebConfigDTO {

    /**
     * token
     */
    private String token;

    /**
     * key
     */
    private String configKey;

    /**
     * 平台补贴
     */
    private String subsidy;

    /**
     * 配送距离
     */
    private String distance;

    /**
     * 值
     */
    private List<DeliveryWebConfigItemDTO> list;
}
