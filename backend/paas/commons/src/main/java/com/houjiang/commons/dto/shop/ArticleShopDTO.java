package com.houjiang.commons.dto.shop;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author Amos
 */
@Data
public class ArticleShopDTO {

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺粉丝数
     */
    private Integer shopCollect;

    /**
     * 地区id
     */
    private Integer districtId;

    /**
     * 是否自营店铺 1 不是 2 是
     */
    private Boolean shopSelfSupport;

    /**
     * 审核时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date shopOperatorTime;
}
