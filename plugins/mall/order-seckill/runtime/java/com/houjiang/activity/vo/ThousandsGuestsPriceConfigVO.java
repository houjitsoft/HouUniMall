package com.houjiang.activity.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 千客千价价格策略配置 VO
 * @author Amos
 */
@Data
public class ThousandsGuestsPriceConfigVO implements Serializable {

    private Long id;

    private Long shopId;

    private Long goodsId;

    private Long skuId;

    private Integer segmentType;

    private String segmentValue;

    private Integer priceType;

    private BigDecimal priceValue;

    private Integer priority;

    private Integer status;

    private Date startTime;

    private Date endTime;

    private String createBy;

    private Date createTime;

    private String updateBy;

    private Date updateTime;

    private String remark;
}
