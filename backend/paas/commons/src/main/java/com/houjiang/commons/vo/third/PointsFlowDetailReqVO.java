package com.houjiang.commons.vo.third;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 获取积分流水入参
 * @CreateDate: 2025/7/8 23:34
 * @author Amos
 */
@Data
public class PointsFlowDetailReqVO implements Serializable {

    /**
     * 用户id，唯一标识，跳转h5链接会带过来
     */
    private Integer userId;

    /**
     * 货币类型id
     */
    private Integer currencyId;

    /**
     * 当前页面，默认1
     */
    private Integer current;

    /**
     * 页面大小，默认10
     */
    private Integer pageSize;

    /**
     * 开始时间（填写了开始时间，必须填写结束时间），格式：2025-01-17
     */
    private String startTime;

    /**
     * 结束时间，格式：2025-01-18
     */
    private String endTime;

    /**
     * 流水类型，1-收入；2-支出
     */
    private Integer flowType;



}
