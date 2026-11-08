package com.houjiang.goods.vo;

import com.houjiang.commons.vo.goods.*;
import com.houjiang.commons.vo.shop.ManageShopSelfCatMenuVO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ManageGdsAddVo
 * @Description ManageGdsAddVo
 * @Author Amos
 * @Date 2023/8/15 16:59
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsAddVo {

    /**
     * 商品属性
     */
    private List<ManagePropertyValueVO> propertyNames;
    /**
     *规格名称
     */
    private List<ManageSpecNameVO> specValueName ;
    /**
     *店铺所属规格列表
     */
    private List<ManageGoodsSpecValueVO> specValues;

   
    /**
     *获取运费模板
     */
    private List<ManageTransportTemplateResultVO> transportTemplate ;
    /**
     *查询店铺自定义商品分类
     */
    private List<ManageShopSelfCatMenuVO> shopSelfCatMenu;
    /**
     *查询关联板式
     */
    private List<ManageGoodsFormatResultVO> goodsFormats;
    /**
     *规格名称集合
     */
    private Map specNameMap;
    /**
     *规格值集合
     */
    private List<GoodBaseSpecValuesJsonBeanVO> specValueList;
}
