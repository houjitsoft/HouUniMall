package com.houjiang.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class SellerOrderInvoiceListVO {
    /**
     * 供应商订单信息
     */
    List<SellerOrderInvoiceVO> list;
    /**
     * 总数
     */
    Integer totalCount;
}
