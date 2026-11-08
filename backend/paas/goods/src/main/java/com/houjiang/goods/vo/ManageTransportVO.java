package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageTransportVO
 * @Author Amos
 * @Date 2023/7/3 10:32
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageTransportVO extends PageListVO{

    /**
     * 商品信息
     */
    private List<ManageTransportGoodsVO> goodsBase;
}
