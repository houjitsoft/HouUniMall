package com.houjiang.payment.dto.risk;

import lombok.Data;

import java.util.Date;

/**
 * 风控日志分页查询请求
 *
 * @author Amos
 */
@Data
public class RiskLogListRequest {

    private String logNo;

    private String bizNo;

    private Integer bizType;

    private String mchNo;

    private String userId;

    /** 最终动作过滤：1-放行 2-拦截 3-告警 4-待审核 */
    private Integer action;

    /** 风险等级：0-正常 1-低 2-中 3-高 */
    private Integer riskLevel;

    /** 审核状态过滤 */
    private Integer auditStatus;

    private Date startTime;

    private Date endTime;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}
