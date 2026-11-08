package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.houjiang.commons.dto.PageUserTokenBeanDTO;
import lombok.Data;

/**
 * @ClassName DiscountBaseListQueryDTO
 * @Author Amos
 * @Date 2023/7/3 15:08
 * @Version 1.0
 * @author Amos
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiscountBaseListQueryDTO extends PageUserTokenBeanDTO {

    /**
     * 限时编号
     */
    private Integer discountId;
    /**
     * 活动名称
     */
    private String discountName;
    /**
     *  状态， 1-已开始；2-已结束；3-已失效；0-未开始；4-预热中
     */
    private Integer discountState;

    /**
     * 店铺id
     */
    private String shopId;
    /**
     * 店铺名
     */
    private String shopName;


}
