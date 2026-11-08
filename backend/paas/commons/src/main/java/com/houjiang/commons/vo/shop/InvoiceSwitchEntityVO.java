package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @ClassName InvoiceSwitchEntity
 * @Description 发票功能开关状态
 * @Author Amos
 * @Date 2022/7/7 10:08
 * @Version 1.0
 * @author Amos
 */
@Data
public class InvoiceSwitchEntityVO {

    /**
     * 发票开关的状态
     */
    private Integer invoiceSwitchStatus;

    /**
     * 发票审核的状态
     */
    private Integer invoiceAuditStatus;

    /**
     * 存储平台的评论信息
     */
    private String platformComment;
}
