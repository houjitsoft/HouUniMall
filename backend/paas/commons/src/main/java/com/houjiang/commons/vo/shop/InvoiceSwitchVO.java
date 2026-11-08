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
public class InvoiceSwitchVO {

    /**
     * 发票功能开关状态:0-关闭;1-开启
     */
    private Integer invoiceSwitchStatus;

    /**
     * 发票开关审核状态:0-待审核；1-通过；2-不通过；3-未申请
     */
    private Integer invoiceAuditStatus;

    /**
     * 平台审核备注
     */
    private String platformComment;
}
