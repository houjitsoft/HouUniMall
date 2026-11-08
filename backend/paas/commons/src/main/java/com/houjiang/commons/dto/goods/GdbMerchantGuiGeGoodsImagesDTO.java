package com.houjiang.commons.dto.goods;

import com.houjiang.commons.vo.goods.GdbMerchantGoodsImagesVO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName GdbMerchantGuiGeGoodsImagesDTO
 * @Description 商品图片
 * @Author Amos
 * @Date 2023/6/26 15:12
 * @Version 1.0
 * @author Amos
 */
@Data
public class GdbMerchantGuiGeGoodsImagesDTO {

    /**
     * 商品图片
     */
    List<GdbMerchantGoodsImagesVO> merchantGoodsImages;

    /**
     * 图片id
     */
    private Integer imagesId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 店铺id
     */
    private Integer shopId;
    /**
     * 图片url
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
     * 主图类型 1图片2视频
     */
    private Integer type;
}
