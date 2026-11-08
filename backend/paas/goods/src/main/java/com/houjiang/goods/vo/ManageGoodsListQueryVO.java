package com.houjiang.goods.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 * 商家管理后台商品列表页查询
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageGoodsListQueryVO extends PageListVO<T> {

    /**
     * 商品信息
     */
    private List<ManageGdsNameQueryVO> goods;

}
