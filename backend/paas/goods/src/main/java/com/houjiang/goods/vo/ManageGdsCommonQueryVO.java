package com.houjiang.goods.vo;

import com.houjiang.commons.vo.base.BaseGoodsConfigVO;
import com.houjiang.commons.vo.goods.GdbAttributeAssionParamVO;
import com.houjiang.commons.vo.goods.GdbMerchantGoodsCommonDetailVO;
import com.houjiang.commons.dto.goods.GdbMerchantGuiGeGoodsImagesDTO;
import com.houjiang.commons.dto.goods.MerchantShopGoodsCatDTO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ManageGdsCommonQueryVO
 * @Description 商品查看
 * @Author Amos
 * @Date 2023/6/27 14:16
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGdsCommonQueryVO {

    /**
     * 公共信息
     */
    private ManageGdsCommonVO commonResult;

    /**
     * 不同规格商品
     */
    private List<ManageGdsBaseAddResultVO> gdsBases;

    /**
     * 商品详情
     */
    private GdbMerchantGoodsCommonDetailVO goodsCommonDetail;

    //private List<GdbMerchantGoodsImages> goodsImages; // 图片
    /**
     * 图片
     */
    private List<GdbMerchantGuiGeGoodsImagesDTO> goodsImages;

    /**
     * 批发商品配置
     */
    private BaseGoodsConfigVO manageGdsGoodsConfig;

    /**
     * 商品店铺分类
     */
    private List<MerchantShopGoodsCatDTO> shopGoodsCatList;

    private List<Integer> goodsCatList;


    private List<GdbAttributeAssionParamVO> gdbAttributeAssionParams;
}
