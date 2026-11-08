package com.houjiang.commons.vo.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseGoodsConfigVO {
    /**
     * 公共ID
     */
    private Integer commonId;

    /**
     * 销售审核
     */
    private Integer sellCheck;

    /**
     * 销售编辑
     */
    private Integer sellEdit;

    /**
     * 销售编辑价格
     */
    private Integer sellEditPrice;

    /**
     * 销售发货
     */
    private Integer sellDeliver;

}