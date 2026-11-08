package com.houjiang.goods.vo;


import com.houjiang.commons.dto.goods.ManageGdsBaseInfoDTO;
import com.houjiang.goods.dto.GoodsCommonDetailDTO;
import com.houjiang.goods.dto.GoodsImagesDTO;
import com.houjiang.goods.entity.GoodsCommonEntity;
import lombok.Data;

import java.util.List;

/**
 * 商品查看
 *
 * @author Amos
 */
@Data
public class ManageIntegraCommonVO {

    /**
     * 公共信息
     */
    private GoodsCommonEntity commonResult;

    /**
     * 不同规格商品
     */
    private List<ManageGdsBaseInfoDTO> gdsBases;

    /**
     * 商品详情
     */
    private GoodsCommonDetailDTO goodsCommonDetail;

    /**
     * 图片
     */
    private List<GoodsImagesDTO> goodsImages;


}
