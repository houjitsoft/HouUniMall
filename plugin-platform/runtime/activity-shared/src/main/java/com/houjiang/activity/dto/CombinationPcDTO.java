package com.houjiang.activity.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CombinationPcDTO extends PageHelper {

    /**
     * 价格排序 1 降序 2 升序
     */
    private Integer priceDesc;

    /**
     * 时间排序 1 降序 2 升序
     */
    private Integer timeDesc;

    /**
     * 组合活动状态  0-未开始;1-已开始;2-已结束；3-已失效
     */
    private Integer status;

}
