package com.houjiang.commons.dto.goods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsAttrDTO {

    /**
     * 商品id集合
     */
    private List<Integer> commonIdList;

    /**
     * 属性名称
     */
    private String attrName;

    /**
     * 属性名称集合
     */
    private List<String> attrNameList;
}
