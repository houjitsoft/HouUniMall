package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class PlusMemberEntityVO {

    /**
     * Thali类型ID
     */
    private Integer thaliType;

    /**
     * Thali的权益值
     */
    private String plusEquity;

    /**
     * Thali使用的金额
     */
    private BigDecimal usedAmount;

    /**
     * Thali开始时间，格式为"yyyy-MM-dd HH:mm:ss"，时区为"GMT+8"
     * 使用@JsonFormat注解在序列化时将java.util.Date转换为字符串，使用@DateTimeFormat注解在反序列化时将字符串转换为java.util.Date
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date thaliStarttime;
}
