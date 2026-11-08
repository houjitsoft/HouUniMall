package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @ClassName GoodsReleasedVO
 * @Description 商品规格值
 * @Author Amos
 * @Date 2023/6/27 11:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsReleasedVO {

    private String specValueId;
    /**
     * 规格值名称
     */
    private String specValueName;
    /**
     * 所属规格id
     */
    private String specId;
    /**
     * 类型ID
     */
    private String typeId;
}
