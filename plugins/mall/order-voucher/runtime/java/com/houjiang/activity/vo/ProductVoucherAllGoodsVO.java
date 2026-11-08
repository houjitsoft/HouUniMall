package com.houjiang.activity.vo;

import com.houjiang.commons.vo.goods.ProductVoucherGoodsVO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ProductVoucherAllGoodsVO
 * @Description
 * @Author Amos
 * @Date 2024/9/9 13:29
 * @Version 1.0
 * @author Amos
 */
@Data
public class ProductVoucherAllGoodsVO {
    /**
     * 活动信息
     */
    private ProductVoucherVO productVoucherVO;
    /**
     * 商品信息
     */
    private List<ProductVoucherGoodsVO> productVoucherGoodsVOList;
    /**
     * 商品总条数
     */
    private Long productVoucherGoodsTotal;
    /**
     * 是否允许兑换
     */
    private Boolean isProductVoucherStart;


}
