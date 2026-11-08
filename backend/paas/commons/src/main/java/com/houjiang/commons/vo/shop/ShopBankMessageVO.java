package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @ClassName ShopBankMessageVO
 * @Description 开户银行信息
 * @Author Amos
 * @Date 2023/7/18 15:57
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopBankMessageVO {

    /**
     * 银行开户名
     */
    private String bankAccountName;

    /**
     * 公司银行账号
     */
    private String bankAccountNumber;

    /**
     * 开户银行支行名称
     */
    private String bankName;

    /**
     * 开户银行详细地址
     */
    private String bankAddress;
    /**
     * 银行所在省份
     */
    private Integer bankProvince;

    /**
     * 银行所在城市
     */
    private Integer bankCity;

    /**
     * 银行所在区域
     */
    private Integer bankArea;
}
