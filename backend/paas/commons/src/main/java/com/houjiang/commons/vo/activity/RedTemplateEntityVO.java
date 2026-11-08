package com.houjiang.commons.vo.activity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class RedTemplateEntityVO {
    private Integer redpacketTId;

    private String redpacketTTitle;

    private BigDecimal redpacketTPrice;

    private BigDecimal redpacketTOrderlimit;

    private Integer redTermofvalidity;

}
