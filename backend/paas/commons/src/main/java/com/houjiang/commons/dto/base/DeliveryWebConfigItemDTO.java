package com.houjiang.commons.dto.base;

import lombok.Data;

/**
 * @ClassName DeliveryWebConfigItemDTO
 * @Description 配置值入参
 * @Author Amos
 * @Date 2024/4/29 11:05
 * @Version 1.0
 * @author Amos
 */
@Data
public class DeliveryWebConfigItemDTO {

    /**
     * key
     */
    private String deliveryDistance;

    /**
     * 最大距离
     */
    private String maxDistance;

    /**
     * 值
     */
    private String fee;
}
