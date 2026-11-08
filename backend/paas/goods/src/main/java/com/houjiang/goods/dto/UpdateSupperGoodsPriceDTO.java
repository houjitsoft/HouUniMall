package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class UpdateSupperGoodsPriceDTO extends  UserTokenBeanDTO{
    /**
     * 商品信息
     */
    List<SupperGoodsPriceDTO> data;

    /**
     * 分类信息
     */
    Integer catId;
}
