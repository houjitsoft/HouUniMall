package com.houjiang.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @ClassName OrderReturnPlatformListDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/1 14:55
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderReturnPlatformListDTO extends PageHelper {

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 售后单号
     */
    private String returnCode;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 平台介入状态
     */
    private Integer interventionState;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date applicationTime;

    /**
     * 售后状态
     */
    private Integer returnState;

    /**
     * 售后类型
     */
    private Integer returnMethodType;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     *  "1"申请时间  "2"失效时间
     */
    private String isTime;

    /**
     * 失效时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expriationTime;
}
