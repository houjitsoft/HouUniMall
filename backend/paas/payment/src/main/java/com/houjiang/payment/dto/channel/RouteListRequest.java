package com.houjiang.payment.dto.channel;

import lombok.Data;

/**
 * 路由规则列表查询请求
 * @author Amos
 */
@Data
public class RouteListRequest {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String routeNo;
    private Integer bizType;
    private String payMethod;
    private String terminal;
    private Integer status;
}
