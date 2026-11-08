package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * 商品品牌
 * @author Amos
 */
@Data
public class GoodsBrandDTO extends PageHelper {

    /**
     * token
     */
    private String token;

    /**
     * 品牌 ID
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 首字母
     */
    private String brandInitial;

    /**
     * 品牌图片
     */
    private String brandPic;


    /**
     *一级分类id
     */
    private Integer catId;

    /**
     * h5
     */
    private String brandHf;

    /**
     * 是否启用 ： 0. 否； 1.是
     */
    private Integer brandEnable;

    /**
     *是否推荐 ： 0. 否； 1.是
     */
    private Integer brandRecommend;

    /**
     *上传店铺的id
     */
    private Integer shopId;

}
