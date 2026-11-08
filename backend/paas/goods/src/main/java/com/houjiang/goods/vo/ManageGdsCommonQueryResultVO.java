package com.houjiang.goods.vo;

import com.houjiang.commons.dto.goods.GdbMerchantGoodsCommonDetailDTO;
import com.houjiang.commons.dto.goods.GdbMerchantGuiGeGoodsImagesDTO;
import com.houjiang.commons.dto.goods.GoodsCategoryAttributesDTO;
import com.houjiang.commons.dto.goods.MerchantShopGoodsCatDTO;
import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import com.houjiang.commons.vo.goods.ManageGdsCommonResultVO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageGdsCommonQueryResultVO
 * @Description
 * @Author Amos
 * @Date 2023/8/11 13:34
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsCommonQueryResultVO {

    /** 公共信息 */
    private ManageGdsCommonResultVO commonResult;

    /**
     * 封装商品
     * 不同规格商品
     */
    private List<ManageGdsBaseAddResultVO> gdsBases;

    /** 商品详情 */
    private GdbMerchantGoodsCommonDetailDTO goodsCommonDetail;

    /** 图片 */
    private List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages;

    /** 批发商品配置 */
    private BaseGoodsConfigVO manageGdsGoodsConfig;

    /**
     * 商品店铺分类
     */
    private List<MerchantShopGoodsCatDTO> shopGoodsCatList;

    private List<Integer> goodsCatList;

}
