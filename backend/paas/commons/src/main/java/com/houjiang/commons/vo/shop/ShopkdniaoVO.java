package com.houjiang.commons.vo.shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopkdniaoVO {
    /**
     * ID
     */
    private Integer id;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 电子商务ID
     */
    private String ebussinessid;

    /**
     * 应用密钥
     */
    private String appkey;

    /**
     * 状态
     */
    private Integer status;

    private String instructCode;


}