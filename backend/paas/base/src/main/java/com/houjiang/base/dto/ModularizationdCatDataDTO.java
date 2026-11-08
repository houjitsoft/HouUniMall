package com.houjiang.base.dto;

import com.houjiang.commons.dto.goods.ModularizationdGoodsDataDTO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ModularizationdCatData
 * @Description TODO
 * @Author Amos
 * @Date 2023/6/8 16:23
 * @Version 1.0
 * @author Amos
 */
@Data
public class ModularizationdCatDataDTO {
    /**
     * 分类名称
     */
    private String catName;
    /**
     * 图片数据
     */
    private ModularizationdCatImageDataDTO modularizationdCatImageData;
    /**
     * 图片数据集合
     */
    private List<ModularizationdCatImageDataDTO> modularizationdCatImageDataList;
    /**
     * 商品数据
     */
    private List<ModularizationdGoodsDataDTO> goodsDataList;
    /**
     * 下级数据
     */
    private List<ModularizationdCatDataDTO> modularizationdCatDataList;
}
