package com.houjiang.payment.dto.merchantQual;

import lombok.Data;

import java.io.Serializable;

/**
 * 商户资质 - 分页查询请求
 *
 * @author Amos
 * @since 2026-05-13
 */
@Data
public class MerchantQualQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 终端筛选（可空） */
    private String terminal;

    /** 渠道筛选（可空） */
    private String channel;

    /** 环境筛选（可空） */
    private String env;

    /** 状态筛选（可空 0/1） */
    private Integer status;

    /** 资质名称模糊查询（可空） */
    private String qualName;

    /** 页码，默认 1 */
    private Integer pageNum = 1;

    /** 每页大小，默认 10 */
    private Integer pageSize = 10;
}
