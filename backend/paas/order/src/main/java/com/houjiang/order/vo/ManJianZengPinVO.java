package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @ClassName ManJianZengPinVO
 * @Description 满减赠赠品信息
 * @Author Amos
 * @Date 2023/6/29 14:59
 * @Version 1.0
 * @author Amos
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManJianZengPinVO {
    /**
     * 图片
     */
    private String giveawayImage;
    /**
     * commoniD
     */
    private String giveawayId;
    /**
     * 商品名称
     */
    private String giveawayName;
}
