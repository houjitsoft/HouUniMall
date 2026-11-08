package com.houjiang.goods.dto;

import com.houjiang.commons.dto.BaseEntityDTO;
import lombok.Data;

/**
 * 图片
 * @author Amos
 */
@Data
public class GoodsImagesDTO extends BaseEntityDTO {

    /**
     * 图片id
     */
    private Integer imagesId;

    /**
     * spuId
     */
    private Integer commonId;

    /**
     * 商品id
     */
    private Integer goodsId;

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
     * 默认主题
     */
    private Integer imagesIsDefault;

    /**
     * 是否主图
     */
    private Integer imagesIsZhu;

    /**
     * 主图类型 1图片2视频
     */
    private Integer type;


}
