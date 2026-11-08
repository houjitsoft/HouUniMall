package com.houjiang.payment.dto.channel;

import lombok.Data;

/**
 * 补偿任务列表查询请求
 * @author Amos
 */
@Data
public class CompensateListRequest {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String taskNo;
    private String bizNo;
    private String channelCode;
    private Integer taskStatus;
}
