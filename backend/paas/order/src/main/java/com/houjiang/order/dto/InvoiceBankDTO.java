package com.houjiang.order.dto;

import lombok.Data;

/**
 * 开户银行
 * @author Amos
 */
@Data
public class InvoiceBankDTO {

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 开户银行
     */
    private String openingBank;

}
