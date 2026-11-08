package com.houjiang.commons.vo.order;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class TraceVO {

    /**
     * 同 StateEx
     */
    private String Action;

    /**
     * 轨迹描述
     */
    private String AcceptStation;

    /**
     * 轨迹发生时间，示例：
     * 2021-01-01 09:00:00
     */
    private String AcceptTime;

    /**
     * 历史节点所在城市
     */
    private String Location;

    /**
     * 轨迹备注
     */
    private String Remark;

}
