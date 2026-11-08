package com.houjiang.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName queryPurchaseOrderDTO
 * @Description
 * @Author Amos
 * @Date 2024/7/25 11:26
 * @Version 1.0
 * @author Amos
 */

@Data
public class queryInvoiceOrderDTO extends PageHelper {

    /**
     * 进货编号
     */
    private String purchaseNumber;
    /**
     * 创建开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 创建结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;


    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 发票状态，表示发票的开具状态，如未开具、已开具等。
     */
    private String invoiceStatus;
}
