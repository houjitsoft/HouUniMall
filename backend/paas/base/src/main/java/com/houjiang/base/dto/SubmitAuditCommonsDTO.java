package com.houjiang.base.dto;

import lombok.Data;

/**
 * @author Amos
 * @version 1.0.0
 * @ClassName SubmitAuditCommonsParam.java
 * @Description TODO  商品提交审核小程序param
 * @createTime 2021年08月19日 10:47:00
 */
@Data
public class SubmitAuditCommonsDTO {

    /**
     * id
     */
    private Integer id;

    /**
     * 审核状态 1 通过 2 审核中 3 不通过  4 未开始 5 一开始 6 已结束
     */
    private Integer isverify;

    /**
     * 审核原因
     */
    private String cause;

    /**
     * token
     */
    private String token;

}
