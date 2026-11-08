package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class ImportGoodsDataBeanVO {
    /** spu */
    private GdbMerchantGoodsCommonVO gdbMerchantGoodsCommon;
    /** sku */
    private List<GdbMerchantGoodsBaseVO> gdbMerchantGoodsBases;
    /** 商品图片 excel导入的时候没有commonId 和 shopId，等保存数据的时候，需要设置 */
    private List<GdbMerchantGoodsImagesVO> goodsImages;
    /** 保存商品详情 */
    private GdbMerchantGoodsCommonDetailVO gdbMerchantGoodsCommonDetail;
    /** http 商详链接 */
    private String imageName;
    private String errorRow;
    /** spu去读的行号 */
    private String readRows;


    
}
