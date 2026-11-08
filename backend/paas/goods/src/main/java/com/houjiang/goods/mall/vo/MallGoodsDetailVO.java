package com.houjiang.goods.mall.vo;

import com.houjiang.goods.mall.entity.MallGoods;
import com.houjiang.goods.mall.entity.MallGoodsSku;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品详情 VO
 * @author Amos
 */
@Data
public class MallGoodsDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private MallGoods goods;
    /** 分类 ID 数组 */
    private List<Integer> categoryIds;
    /** 轮播图 URL */
    private List<String> imageList;
    /** SKU 规格名 */
    private List<String> skuNameList;
    /** SKU 列表 */
    private List<MallGoodsSku> skuList;
}
