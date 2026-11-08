package com.houjiang.job.vo;

import lombok.Data;

/**
 * @ClassName DcenterOrderCancelReasonVO
 * @Description
 * @Author Amos
 * @Date 2025/1/14 10:56
 * @Version 1.0
 * @author Amos
 */

@Data
public class DcenterOrderCancelReasonVO {

    /**
     *
     */
    private Integer cancelReasonId;

    /**
     * 取消订单的原因
     */
    private String cancelReasonContent;
}
