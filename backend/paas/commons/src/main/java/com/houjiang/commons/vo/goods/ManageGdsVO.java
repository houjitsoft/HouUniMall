package com.houjiang.commons.vo.goods;


import com.houjiang.commons.vo.shop.ManageShopSelfCatMenuVO;
import com.houjiang.commons.vo.shop.TransportTemplateVO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 添加商品的需要的初始化数据
 *
 * @author Amos
 */
@Data
public class ManageGdsVO {

    /**
     * 商品属性
     */
    private List<ManagePropertyValueVO> propertyNames;

    /**
     * 规格名称
     */
    private List<ManageSpecNameVO> specValueName;

    /**
     * 店铺所属规格列表
     */
    private List<ManageGoodsSpecValueVO> specValues;

    /**
     * 获取运费模板
     */
    private List<TransportTemplateVO> transportTemplate;

    /**
     * 查询店铺自定义商品分类
     */
    private List<ManageShopSelfCatMenuVO> shopSelfCatMenu;

    /**
     * 规格名称集合
     */
    private Map specNameMap;

    /**
     * 规格值集合
     */
    private List<GoodBaseSpecValuesVO> specValueList;

}
