package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName ManageSpecNameResult
 * @Description
 * @Author Amos
 * @Date 2023/4/7 14:09
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageSpecNameDTO {

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 规格名称值
     */
    private String specValueName;
}
