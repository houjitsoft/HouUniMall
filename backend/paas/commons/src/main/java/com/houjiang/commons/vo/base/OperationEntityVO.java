package com.houjiang.commons.vo.base;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class OperationEntityVO {
    /**
     * 百分比
     */
    private Integer amount;
    /**
     * 最高赔
     */
    private Integer highest;
    /**
     * 最低赔
     */
    private Integer minimum;
}
