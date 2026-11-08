package com.houjiang.commons.vo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlusEquityEntityVO {

    private Integer equityId;

    private String equityName;

    private Integer equityType;

    private String equityValue;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date equityAddtime;

    private Integer equityStatus;

    private Integer redEffective;

    private Integer redProvide;

    private BigDecimal discountLimit;

    private List<RedTemplateEntityVO> redList;

    private String token;
}
