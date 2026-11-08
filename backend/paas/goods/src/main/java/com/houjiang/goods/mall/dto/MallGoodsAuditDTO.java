package com.houjiang.goods.mall.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品审核参数
 * 对应 bbc MallGoodsAuditValidate
 * @author Amos
 */
@Data
public class MallGoodsAuditDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Integer id;
    /** 1通过 2拒绝 */
    private Integer auditStatus;
    /** 拒绝原因 */
    private String auditContent;
}
