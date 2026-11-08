package com.houjiang.commons.vo.shop;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ShopJoinInitVO {

    /**
     * 以前的joinId
     */
    private String oldJoinId;

    /**
     * 新的joinId
     */
    private String newJoinId;

    /**
     * 店铺类型
     */
    private Integer shopType;

    /**
     * 是否存在
     */
    private Integer isExist;

    /**
     * 入驻信息审核状态
     */
    private Integer baseAuditStatus;

    /**
     * 入驻支付凭证审核状态
     */
    private Integer payAuditStatus;

    /**
     * 账号店铺状态
     */
    private Integer shopBaseExist;

}
