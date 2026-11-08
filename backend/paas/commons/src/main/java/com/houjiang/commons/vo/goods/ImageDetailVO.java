package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * 图片详情
 * @author Amos
 */
@Data
public class ImageDetailVO {

    /**
     * 商品图片
     */
    private String imagesImage;

    /**
     * 排序
     */
    private Integer imagesDisplayorder;

    /**
     * 图片是否默认
     */
    private Integer imagesIsDefault;

}
