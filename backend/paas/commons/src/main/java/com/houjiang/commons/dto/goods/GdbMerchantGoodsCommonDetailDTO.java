package com.houjiang.commons.dto.goods;

import com.houjiang.commons.dto.BaseEntityDTO;
import com.houjiang.commons.dto.goods.GdbAttributeAssionParamDTO;
import com.houjiang.commons.dto.goods.ManageGoodsFormatResultDTO;
import com.houjiang.commons.dto.goods.ManageGoodsPropertyAddDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * @ClassName GdbMerchantGoodsCommonDetailDTO
 * @Description 商品详情
 * @Author Amos
 * @Date 2023/6/26 15:04
 * @Version 1.0
 * @author Amos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GdbMerchantGoodsCommonDetailDTO extends BaseEntityDTO {
    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 通用商品描述
     */
    private String commonBody;

    /**
     * 商品属性
     */
    private List<ManageGoodsPropertyAddDTO> propertys;
    /**
     * 关联板式
     */
    private ManageGoodsFormatResultDTO commonFormatidTop;

    /**
     * 底部关联板式
     */
    private ManageGoodsFormatResultDTO commonFormatidBottom;

    /**
     * 品牌名
     */
    private String brandName;

    /**
     * 属性信息
     */
    private List<GdbAttributeAssionParamDTO> attributeParam;
}
