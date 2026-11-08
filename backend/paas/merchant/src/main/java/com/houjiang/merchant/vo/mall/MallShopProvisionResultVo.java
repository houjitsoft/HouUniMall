package com.houjiang.merchant.vo.mall;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/** 商家开通结果（不返回密码）。 */
@Data
@AllArgsConstructor
public class MallShopProvisionResultVo implements Serializable {
    private Integer shopId;
    private String loginAccount;
}
