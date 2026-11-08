package com.houjiang.goods.dto;


import com.houjiang.commons.dto.BaseEntityDTO;
import com.houjiang.goods.vo.GoodsFormatVO;
import lombok.Data;

import java.util.List;

/**
 * 商品详情
 * @author Amos
 */
@Data
public class GoodsCommonDetailDTO extends BaseEntityDTO {

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
    private List<GoodsPropertyDTO> propertys;

    /**
     * 顶部关联板式
     */
    private GoodsFormatVO commonFormatidTop;

    /**
     * 低部关联板式
     */
    private GoodsFormatVO commonFormatidBottom;

    public void setCommonBody(String commonBody) {
        this.commonBody = commonBody == null ? null : commonBody.trim();
    }
}
