package com.houjiang.commons.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopContractDTO {

    /**
     * 合同id
     */
    private Integer contractId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 卖方
     */
    private String seller;

    /**
     * 销售方代表
     */
    private String sellerRepresentative;

    /**
     * 卖方地址
     */
    private String sellerAddress;

    /**
     * 卖方电话
     */
    private String sellerTel;

    /**
     * 开户行
     */
    private String bankDeposit;

    /**
     * 账号
     */
    private String accountNumber;

    /**
     * 公章
     */
    private String chapterImage;

    /**
     * 条款
     */
    private String clause;

    /**
     * logo
     */
    private String logo;

    /**
     * token
     */
    private String token;

}
