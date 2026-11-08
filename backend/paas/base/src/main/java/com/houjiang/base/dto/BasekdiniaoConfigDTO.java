package com.houjiang.base.dto;

import lombok.Data;

/**
 * 修改快递鸟配置
 * @author Amos
 */
@Data
public class BasekdiniaoConfigDTO {

    /**
     * token
     */
    private String token;

    /**
     * id
     */
    private Integer id;

    /**
     * 快递鸟Id
     */
    private String ebussinessid;

    /**
     * 快递鸟密钥
     */
    private String appkey;

    /**
     * 快递鸟接口指令 购买套餐不一样 指令不一样
     */
    private Integer instructCode;

    private Integer shopId;
}
