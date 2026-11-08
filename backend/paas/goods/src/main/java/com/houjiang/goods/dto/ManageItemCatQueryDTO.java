package com.houjiang.goods.dto;

import lombok.Data;

/**
 * @ClassName ManageItemCatQueryDTO
 * @Author Amos
 * @Date 2023/6/28 9:39
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageItemCatQueryDTO extends UserTokenBeanDTO{

    /**
     * 父类
     */
    private Integer catParentId;

    /**
     *  1 2 3  对应分类的一 二 三 级
     */
    private Integer catLevel;

    /**
     * 店铺id
     */
    private Integer shopId;

}
