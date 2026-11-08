package com.houjiang.commons.vo.base;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class PlusThaliEntityVO {

    private Integer thaliId;

    private Integer thaliType;

    private BigDecimal thaliPrice;

    private BigDecimal thaliOldPrice;

    private BigDecimal totalAmount;

    private BigDecimal earnPoints;

    private String thaliEquity;

    private String userAvatar;

    private String userName;

    private List thaliEquityList;

    private Integer thaliRecommend;

    private Integer equityTotal;

    private Integer thaliStatus;

    private Integer renewalDisplay;

    private Integer memberStatus;

    private List<PlusEquityEntityVO> plusEquitys;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date thaliEndtime;

    private Integer nextThaliType;

    /** 红包总数 */
    private Integer redTotal;

    /** 可用红包 */
    private Integer redAvailable;

    /** 卡logo */
    private String logoImg;

    /** 推荐标识 */
    private String recommend;

    /** 宣传语 */
    private String propaganda;
}
