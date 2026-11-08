package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 满减赠品
 * @author Amos
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class FullReductionGiftsVO {

    /**
     * 图片
     */
    private String giveawayImage;

    /**
     * 赠品ID
     */
    private String giveawayId;

    /**
     * 商品名称
     */
    private String giveawayName;


}
