package com.houjiang.goods.dto;

import com.houjiang.commons.dto.yiyatong.PageBaseHelper;
import lombok.Data;

/**
 * @ClassName SpuPageQueryDTO
 * @Description
 * @Author Amos
 * @Date 2024/11/6 14:55
 * @Version 1.0
 * @author Amos
 */

@Data
public class JingDongSpuQueryDTO extends PageBaseHelper {
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 京东商品sk
     */
    private String skuId;

    /**
     * 是否已代发
     */
    private Integer dropShipping;

    /**
     * 销量 排序 0 不生效 1 升序  2 降序
     */
    private Integer salesVolumeSorting = 0;
    /**
     * 协议价 排序 0 不生效 1 升序  2 降序
     */
    private Integer agreementPriceSorting  = 0;
    /**
     * 用户收货的四级地址
     */
    private String addrsStr = "上海松江区新桥镇";

}
