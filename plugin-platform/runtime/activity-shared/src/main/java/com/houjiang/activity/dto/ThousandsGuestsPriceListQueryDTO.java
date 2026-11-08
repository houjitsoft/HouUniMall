package com.houjiang.activity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 千客千价价格策略列表查询 DTO
 * @author Amos
 */
@Data
public class ThousandsGuestsPriceListQueryDTO implements Serializable {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    private Long shopId;

    private Long goodsId;

    private Integer segmentType;

    private Integer status;
}
