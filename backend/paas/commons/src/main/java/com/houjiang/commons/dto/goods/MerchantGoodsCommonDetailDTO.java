package com.houjiang.commons.dto.goods;


import com.houjiang.commons.dto.BaseEntityDTO;
import com.houjiang.commons.vo.goods.ManageGoodsFormatVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品详情
 * @author Amos
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MerchantGoodsCommonDetailDTO extends BaseEntityDTO {

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品内容
     */
    private String commonBody;

    /**
     * 商品属性
     */
    private List<ManageGoodsPropertyDTO> propertys;

    /**
     * 顶部关联板式
     */
    private ManageGoodsFormatVO commonFormatidTop;

    /**
     * 低部关联板式
     */
    private ManageGoodsFormatVO commonFormatidBottom;

    /**
     * 商品文件
     */
    private String productFile;

    /**
     * 商品文件集合
     */
    private List<ProductFileDTO> productFileList;

    public void setCommonBody(String commonBody) {
        this.commonBody = commonBody == null ? null : commonBody.trim();
    }
}