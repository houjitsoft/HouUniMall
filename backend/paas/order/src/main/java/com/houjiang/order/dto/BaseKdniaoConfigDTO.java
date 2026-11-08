package com.houjiang.order.dto;

import lombok.Data;


/**
 * 快递鸟 配置
 * @author Amos
 */
@Data
public class BaseKdniaoConfigDTO {

    /**
     * 自增主键
     */
    private Integer id;

    /**
     * 快递鸟Id
     */
    private String eBussinessID;

    /**
     * 快递鸟密钥
     */
    private String appKey;

    /**
     * 1正常 0冻结
     */
    private Integer status;

    /**
     * 快递鸟接口指令 购买套餐不一样 指令不一样
     */
    private Integer instructCode;


}
