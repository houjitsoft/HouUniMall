package com.houjiang.commons.dto.mall;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallStockReservationRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private Long warehouseId;
    private Long skuId;
    private Integer quantity;
}
