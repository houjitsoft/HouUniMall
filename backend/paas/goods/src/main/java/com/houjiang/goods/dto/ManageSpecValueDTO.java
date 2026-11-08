package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageSpecValueDTO
 * @Author Amos
 * @Date 2023/6/27 17:29
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageSpecValueDTO extends UserTokenBeanDTO{

    /**
     * 规格值
     */
    private List<GdbMerchantGoodsSpecValueDTO> specValue;
}
