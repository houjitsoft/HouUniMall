package com.houjiang.payment.dto.risk;

import lombok.Data;

/**
 * 黑白名单分页查询请求
 *
 * @author Amos
 */
@Data
public class BlacklistListRequest {

    private Integer listType;

    private Integer targetType;

    private String targetValue;

    private Integer bizScope;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
