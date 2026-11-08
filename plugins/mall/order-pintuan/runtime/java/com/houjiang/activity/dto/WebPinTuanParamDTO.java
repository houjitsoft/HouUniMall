package com.houjiang.activity.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class WebPinTuanParamDTO extends PageHelper {
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 店铺id
     */
    private Integer shopId;
}
