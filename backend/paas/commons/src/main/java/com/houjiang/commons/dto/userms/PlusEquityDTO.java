package com.houjiang.commons.dto.userms;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.activity.ActivityRedpacketTemplateVO;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class PlusEquityDTO {
    /**
     * 权益名称名称
     */
    private Integer equityId;
    /**
     *权益名称
     */
    private String equityName;
    /**
     *1 PLUS专属折扣, 2 无门槛红包,3 红包券,4 送积分,5 积分倍率
     */
    private Integer equityType;
    /**
     *权益值
     */
    private String equityValue;
    /**
     *权益添加时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date equityAddtime;
    /**
     *权益状态 0 关闭  1 开启
     */
    private Integer equityStatus;
    /**
     *无门槛红包有效时间
     */
    private Integer redEffective;
    /**
     *红包发放时间  几号
     */
    private Integer redProvide;
    /**
     *每月最多可抵扣额度
     */
    private BigDecimal discountLimit;
    /**
     *
     */
    private String token;

    /**
     * 红包模板
     */
    private List<ActivityRedpacketTemplateVO> redList;
}
