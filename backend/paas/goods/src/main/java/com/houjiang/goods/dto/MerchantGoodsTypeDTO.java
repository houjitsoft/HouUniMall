package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 商品类型
 * @author Amos
 */
@Data
public class MerchantGoodsTypeDTO extends PageHelper {

    /**
     * token
     */
    private String token;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 排序
     */
    private Integer typeDisplayorder;
}
