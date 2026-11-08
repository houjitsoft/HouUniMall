package com.houjiang.goods.vo;

import com.houjiang.goods.dto.UserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName ManageSpecValueVO
 * @Author Amos
 * @Date 2023/6/27 15:50
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageSpecValueVO extends UserTokenBeanDTO {

    /**
     * 分类id
     */
    private String catIds;

    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 分类id
     */
    private Integer catId;
    /**
     * 类型id
     */
    private Integer typeId;
    /**
     * 规格id
     */
    private String specId;

    /**
     * 规格名称
     */
    private String specName;


    /**
     * 规格名称值id
     */
    private String specValueId;

    /**
     * 规格名称值名称
     */
    private String specValueName;



    /**
     * 商品id
     */
    private String goodsId;
}
