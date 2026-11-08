package com.houjiang.base.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 * @date 2021-05-0423:39
 * @Description: 经销商店铺
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DealerShopInfoVO {

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 加入id
     */
    private String joinId;

    /**
     * 店铺logo
     */
    private String shopLogo;

    /**
     * 创建时间
     */
    private String shopCreateTime;

    /**
     * 有效期截止时间
     */
    private String shopEndTime;

    private String shopCompanyAddress;

    /**
     * qq
     */
    private String shopQq;

    /**
     * 联系电话
     */
    private String shopTel;

}
