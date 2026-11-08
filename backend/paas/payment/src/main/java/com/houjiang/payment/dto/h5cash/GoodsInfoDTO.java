package com.houjiang.payment.dto.h5cash;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品信息DTO
 * 
 * @author Amos
 */
@Data
public class GoodsInfoDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品简称(必填)
     */
    private String goodsnm;
    
    /**
     * 商品金额(可选)
     * 单位:元
     */
    private String goodsamt;
    
    /**
     * 商品数量(可选)
     */
    private String goodsnum;
}
