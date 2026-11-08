package com.houjiang.goods.dto;

import com.houjiang.commons.dto.BaseEntityDTO;
import lombok.Data;

/**
 * @ClassName GdbMerchantGoodsImagesDTO
 * @Description 商品图片
 * @Author Amos
 * @Date 2023/6/26 15:12
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbMerchantGoodsImagesDTO extends BaseEntityDTO {

    /**
     * 商品图片id
     */
    private Integer imagesId;

    /**
     * 商品公共内容id
     */
    private Integer commonId;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 商品图片
     */
    private String imagesImage;

    /**
     * 排序
     */
    private Integer imagesDisplayorder;

    /**
     * 默认主题，1是，0否
     */
    private Integer imagesIsDefault;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 是否主图
     */
    private Integer imagesIsZhu;
}
